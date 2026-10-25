package com.neont.messenger.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.neont.messenger.data.crypto.RealMessageCrypto
import com.neont.messenger.data.model.LastMessageInfo
import com.neont.messenger.data.model.Message
import com.neont.messenger.data.model.MessageStatus
import com.neont.messenger.data.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(private val fileDb: FileDatabaseManager) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT
    ): Message {
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Enkripsi E2EE AES-256-GCM
        val encrypted = RealMessageCrypto.encrypt(text, chatId)

        val pendingMessage = Message(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            cipherText = encrypted.cipherText,
            iv = encrypted.iv,
            encryptedKey = encrypted.encryptedKey,
            type = type,
            status = MessageStatus.PENDING,
            timestamp = timestamp
        )

        // 1. Simpan instan ke berkas JSON lokal (terenkripsi di disk)
        fileDb.appendMessage(chatId, pendingMessage)

        // 2. Perbarui lastMessage di chats.json
        val chats = fileDb.readChats().toMutableList()
        val index = chats.indexOfFirst { it.chatId == chatId }
        if (index >= 0) {
            chats[index] = chats[index].copy(
                lastMessage = LastMessageInfo(
                    text = text,
                    senderId = senderId,
                    timestamp = timestamp,
                    status = MessageStatus.PENDING,
                    type = type
                )
            )
            fileDb.writeChats(chats)
        }

        // 3. Push ke Firebase Realtime Database
        val payload = mapOf(
            "msgId" to msgId,
            "chatId" to chatId,
            "senderId" to senderId,
            "cipherText" to encrypted.cipherText,
            "iv" to encrypted.iv,
            "text" to "[E2EE Encrypted]",
            "type" to type.name,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT.name
        )

        val ref = database.getReference("messages").child(chatId).child(msgId)
        ref.setValue(payload)
            .addOnSuccessListener {
                scope.launch(Dispatchers.IO) {
                    val updatedMsg = pendingMessage.copy(status = MessageStatus.SENT)
                    fileDb.appendMessage(chatId, updatedMsg)
                    chatMessageFlows[chatId]?.let { flow ->
                        val current = flow.value.toMutableList()
                        val mIdx = current.indexOfFirst { it.msgId == msgId }
                        if (mIdx >= 0) {
                            current[mIdx] = updatedMsg
                            flow.value = current
                        }
                    }
                }
            }

        return pendingMessage
    }

    private val chatMessageFlows = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.flow.MutableStateFlow<List<Message>>>()

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
                scope.launch(Dispatchers.IO) {
                    val decryptedText = if (!rawMsg.cipherText.isNullOrBlank() && !rawMsg.iv.isNullOrBlank()) {
                        RealMessageCrypto.decrypt(rawMsg.cipherText, rawMsg.iv, chatId, rawMsg.text)
                    } else rawMsg.text
                    val status = if (rawMsg.status == MessageStatus.PENDING) MessageStatus.DELIVERED else rawMsg.status
                    val memoryMsg = rawMsg.copy(text = decryptedText, status = status)

                    val current = stateFlow.value.toMutableList()
                    val idx = current.indexOfFirst { it.msgId == memoryMsg.msgId }
                    if (idx >= 0) {
                        current[idx] = memoryMsg
                    } else {
                        current.add(memoryMsg)
                    }
                    stateFlow.value = current

                    fileDb.appendMessage(chatId, rawMsg.copy(status = status))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val rawMsg = snapshot.getValue(Message::class.java) ?: return
                scope.launch(Dispatchers.IO) {
                    val decryptedText = if (!rawMsg.cipherText.isNullOrBlank() && !rawMsg.iv.isNullOrBlank()) {
                        RealMessageCrypto.decrypt(rawMsg.cipherText, rawMsg.iv, chatId, rawMsg.text)
                    } else rawMsg.text
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
                scope.launch(Dispatchers.IO) {
                    val current = stateFlow.value.toMutableList()
                    if (current.removeAll { it.msgId == msgId }) {
                        stateFlow.value = current
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addChildEventListener(listener)
        return stateFlow.asStateFlow()
    }
}
