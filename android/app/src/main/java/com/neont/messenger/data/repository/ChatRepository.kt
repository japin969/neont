// Neont Messenger - ChatRepository.kt
// Mengatur pengiriman pesan, penerimaan real-time via ChildEventListener, presence, dan typing status
package com.neont.messenger.data.repository

import com.google.firebase.database.*
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
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
     * Mengirim pesan dengan optimistic local store + Firebase Realtime Database
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

        val pendingMessage = Message(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            mediaDurationSec = mediaDurationSec,
            status = MessageStatus.PENDING,
            timestamp = timestamp
        )

        // 1. Simpan ke file JSON lokal (messages_{chatId}.json) secara instan
        fileDb.appendMessage(chatId, pendingMessage)

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

        // 3. Push ke Firebase /messages/{chatId}/{msgId}
        val payload = mapOf(
            "msgId" to msgId,
            "chatId" to chatId,
            "senderId" to senderId,
            "text" to text,
            "type" to type.name,
            "mediaUrl" to mediaUrl,
            "mediaDurationSec" to mediaDurationSec,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT.name
        )

        val msgRef = database.getReference("messages").child(chatId).child(msgId)
        msgRef.setValue(payload)
            .addOnSuccessListener {
                repositoryScope.launch {
                    fileDb.updateMessageStatus(chatId, msgId, MessageStatus.SENT)
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
            .addOnFailureListener {
                // Biarkan status tetap PENDING di JSON untuk dikirim saat online
            }

        return pendingMessage
    }

    /**
     * Listener pesan masuk real-time: addChildEventListener -> simpan ke file JSON lokal -> emit ke Flow
     */
    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        // Emit data cache lokal terlebih dahulu
        trySend(fileDb.readMessages(chatId))

        val ref = database.getReference("messages").child(chatId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch {
                    val status = if (msg.status == MessageStatus.PENDING) MessageStatus.DELIVERED else msg.status
                    fileDb.appendMessage(chatId, msg.copy(status = status))
                    trySend(fileDb.readMessages(chatId))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch {
                    fileDb.appendMessage(chatId, msg)
                    trySend(fileDb.readMessages(chatId))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val msgId = snapshot.key ?: return
                repositoryScope.launch {
                    fileDb.deleteMessage(chatId, msgId)
                    trySend(fileDb.readMessages(chatId))
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
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
