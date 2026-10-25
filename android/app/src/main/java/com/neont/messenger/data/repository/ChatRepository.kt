// Neont Messenger - ChatRepository.kt
// Mengatur pengiriman pesan, penerimaan real-time via ChildEventListener, presence, dan typing status
package com.neont.messenger.data.repository

import com.google.firebase.database.*
import com.neont.messenger.data.crypto.RealMessageCrypto
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val fileDb: FileDatabaseManager
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val chatMessageFlows = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.flow.MutableStateFlow<List<Message>>>()

    init {
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {}
        observeConnectionState()
    }

    private fun observeConnectionState() {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    repositoryScope.launch { syncPendingMessages() }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    suspend fun getLocalChats(): List<Chat> = fileDb.readChats()

    suspend fun getLocalMessages(chatId: String): List<Message> = fileDb.readMessages(chatId)

    /**
     * Mengirim pesan dengan Real End-to-End Encryption (AES-256-GCM + IV)
     * Plaintext didekripsi hanya di memori, disimpan dalam bentuk CipherText di Firebase & berkas lokal
     */
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        mediaDurationSec: Int? = null
    ): Message {
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Enkripsi E2EE AES-256-GCM
        val encrypted = RealMessageCrypto.encrypt(text, chatId)

        val pendingMessage = Message(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            text = text, // In-memory plaintext untuk rendering instan
            cipherText = encrypted.cipherText,
            iv = encrypted.iv,
            encryptedKey = encrypted.encryptedKey,
            type = type,
            mediaUrl = mediaUrl,
            mediaDurationSec = mediaDurationSec,
            status = MessageStatus.PENDING,
            timestamp = timestamp
        )

        // Update in-memory state flow secara instan (zero lag)
        chatMessageFlows[chatId]?.let { flow ->
            val updated = flow.value.toMutableList()
            updated.add(pendingMessage)
            flow.value = updated
        }

        // 1. Simpan ke berkas JSON lokal (tersimpan terenkripsi di disk) pada Dispatchers.IO
        repositoryScope.launch(Dispatchers.IO) {
            fileDb.appendMessage(chatId, pendingMessage)
        }

        // 2. Update preview di chats.json
        val lastMsg = LastMessageInfo(
            text = text,
            senderId = senderId,
            timestamp = timestamp,
            status = MessageStatus.PENDING,
            type = type
        )
        val chats = fileDb.readChats().toMutableList()
        val cIdx = chats.indexOfFirst { it.chatId == chatId }
        if (cIdx >= 0) {
            chats[cIdx] = chats[cIdx].copy(lastMessage = lastMsg)
            fileDb.writeChats(chats)
        }

        // 3. Push CipherText dan IV ke Firebase Realtime Database
        val payload = mutableMapOf<String, Any>(
            "msgId" to msgId,
            "chatId" to chatId,
            "senderId" to senderId,
            "cipherText" to encrypted.cipherText,
            "iv" to encrypted.iv,
            "text" to "[E2EE Encrypted]", // Tidak pernah mengekspos plaintext di jaringan
            "type" to type.name,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT.name
        )
        if (encrypted.encryptedKey != null) {
            payload["encryptedKey"] = encrypted.encryptedKey
        }
        if (mediaUrl != null) payload["mediaUrl"] = mediaUrl
        if (mediaDurationSec != null) payload["mediaDurationSec"] = mediaDurationSec

        val msgRef = database.getReference("messages").child(chatId).child(msgId)
        msgRef.setValue(payload)
            .addOnSuccessListener {
                repositoryScope.launch(Dispatchers.IO) {
                    fileDb.updateMessageStatus(chatId, msgId, MessageStatus.SENT)

                    // Update item yang berubah di state flow
                    chatMessageFlows[chatId]?.let { flow ->
                        val current = flow.value.toMutableList()
                        val mIdx = current.indexOfFirst { it.msgId == msgId }
                        if (mIdx >= 0) {
                            current[mIdx] = current[mIdx].copy(status = MessageStatus.SENT)
                            flow.value = current
                        }
                    }

                    val updatedChats = fileDb.readChats().toMutableList()
                    val idx = updatedChats.indexOfFirst { it.chatId == chatId }
                    if (idx >= 0) {
                        updatedChats[idx] = updatedChats[idx].copy(
                            lastMessage = updatedChats[idx].lastMessage?.copy(status = MessageStatus.SENT)
                        )
                        fileDb.writeChats(updatedChats)
                    }
                }
            }

        return pendingMessage
    }

    /**
     * Listener pesan masuk real-time: menggunakan MutableStateFlow dan hanya mengupdate item yang berubah
     */
    fun listenToMessages(chatId: String): Flow<List<Message>> {
        val stateFlow = chatMessageFlows.getOrPut(chatId) {
            val initial = kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                fileDb.readMessages(chatId)
            }
            kotlinx.coroutines.flow.MutableStateFlow(initial)
        }

        val ref = database.getReference("messages").child(chatId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val rawMsg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch(Dispatchers.IO) {
                    val status = if (rawMsg.status == MessageStatus.PENDING) MessageStatus.DELIVERED else rawMsg.status
                    // Dekripsi teks secara in-memory
                    val decryptedText = if (!rawMsg.cipherText.isNullOrBlank() && !rawMsg.iv.isNullOrBlank()) {
                        RealMessageCrypto.decrypt(rawMsg.cipherText, rawMsg.iv, chatId, rawMsg.text)
                    } else {
                        rawMsg.text
                    }
                    val memoryMsg = rawMsg.copy(text = decryptedText, status = status)

                    // Update HANYA item yang berubah pada MutableStateFlow
                    val current = stateFlow.value.toMutableList()
                    val existingIdx = current.indexOfFirst { it.msgId == memoryMsg.msgId }
                    if (existingIdx >= 0) {
                        current[existingIdx] = memoryMsg
                    } else {
                        current.add(memoryMsg)
                    }
                    stateFlow.value = current

                    // Simpan ke disk (Dispatchers.IO)
                    fileDb.appendMessage(chatId, rawMsg.copy(status = status))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val rawMsg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch(Dispatchers.IO) {
                    val decryptedText = if (!rawMsg.cipherText.isNullOrBlank() && !rawMsg.iv.isNullOrBlank()) {
                        RealMessageCrypto.decrypt(rawMsg.cipherText, rawMsg.iv, chatId, rawMsg.text)
                    } else {
                        rawMsg.text
                    }
                    val memoryMsg = rawMsg.copy(text = decryptedText)

                    val current = stateFlow.value.toMutableList()
                    val idx = current.indexOfFirst { it.msgId == memoryMsg.msgId }
                    if (idx >= 0) {
                        current[idx] = memoryMsg
                        stateFlow.value = current
                    }

                    fileDb.appendMessage(chatId, rawMsg)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val msgId = snapshot.key ?: return
                repositoryScope.launch(Dispatchers.IO) {
                    val current = stateFlow.value.toMutableList()
                    if (current.removeAll { it.msgId == msgId }) {
                        stateFlow.value = current
                    }
                    fileDb.deleteMessage(chatId, msgId)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addChildEventListener(listener)
        return stateFlow.asStateFlow()
    }

    /**
     * Setup Presence dengan onDisconnect()
     */
    fun setupPresence(userId: String) {
        if (userId.isBlank()) return
        val presenceRef = database.getReference("presence").child(userId)
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    presenceRef.setValue(mapOf("isOnline" to true, "lastSeen" to ServerValue.TIMESTAMP))
                    presenceRef.onDisconnect().setValue(mapOf("isOnline" to false, "lastSeen" to ServerValue.TIMESTAMP))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observePresence(userId: String): Flow<Pair<Boolean, Long>> = callbackFlow {
        val ref = database.getReference("presence").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                trySend(Pair(isOnline, lastSeen))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        val ref = database.getReference("typing").child(chatId).child(userId)
        if (isTyping) {
            ref.setValue(true)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun observeTyping(chatId: String, currentUserId: String): Flow<List<String>> = callbackFlow {
        val ref = database.getReference("typing").child(chatId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typers = mutableListOf<String>()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid != currentUserId && child.getValue(Boolean::class.java) == true) {
                        typers.add(uid)
                    }
                }
                trySend(typers)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun syncPendingMessages() {
        val chats = fileDb.readChats()
        for (c in chats) {
            val msgs = fileDb.readMessages(c.chatId)
            for (p in msgs.filter { it.status == MessageStatus.PENDING }) {
                try {
                    val payload = mapOf(
                        "msgId" to p.msgId,
                        "chatId" to p.chatId,
                        "senderId" to p.senderId,
                        "text" to p.text,
                        "type" to p.type.name,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "status" to MessageStatus.SENT.name
                    )
                    database.getReference("messages").child(c.chatId).child(p.msgId).setValue(payload).await()
                    fileDb.updateMessageStatus(c.chatId, p.msgId, MessageStatus.SENT)
                } catch (_: Exception) {}
            }
        }
    }
}
