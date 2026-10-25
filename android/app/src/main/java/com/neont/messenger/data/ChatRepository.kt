package com.neont.messenger.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.neont.messenger.data.model.LastMessageInfo
import com.neont.messenger.data.model.Message
import com.neont.messenger.data.model.MessageStatus
import com.neont.messenger.data.model.MessageType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(private val fileDb: FileDatabaseManager) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT
    ): Message {
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val pendingMessage = Message(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            type = type,
            status = MessageStatus.PENDING,
            timestamp = timestamp
        )

        fileDb.appendMessage(chatId, pendingMessage)

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

        val payload = mapOf(
            "msgId" to msgId,
            "chatId" to chatId,
            "senderId" to senderId,
            "text" to text,
            "type" to type.name,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT.name
        )

        val ref = database.getReference("messages").child(chatId).child(msgId)
        ref.setValue(payload)
            .addOnSuccessListener {
                fileDb.appendMessage(chatId, pendingMessage.copy(status = MessageStatus.SENT))
            }

        return pendingMessage
    }

    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        trySend(fileDb.readMessages(chatId))

        val ref = database.getReference("messages").child(chatId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                launch {
                    fileDb.appendMessage(chatId, msg)
                    trySend(fileDb.readMessages(chatId))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                launch {
                    fileDb.appendMessage(chatId, msg)
                    trySend(fileDb.readMessages(chatId))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
