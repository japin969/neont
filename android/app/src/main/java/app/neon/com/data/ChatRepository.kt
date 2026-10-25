package app.neon.com.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import app.neon.com.data.model.Chat
import app.neon.com.data.model.LastMessageInfo
import app.neon.com.data.model.Message
import app.neon.com.data.model.MessageStatus
import app.neon.com.data.model.MessageType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(private val fileDb: FileDatabaseManager) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // 1. Realtime List Chat (Halaman Depan)
    fun listenToChats(): Flow<List<Chat>> = callbackFlow {
        trySend(fileDb.readChats())
        
        val ref = database.getReference("chats")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java) ?: return
                launch {
                    fileDb.upsertChat(chat)
                    trySend(fileDb.readChats())
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(Chat::class.java) ?: return
                launch {
                    fileDb.upsertChat(chat)
                    trySend(fileDb.readChats())
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // 2. Realtime Presence (Status Online)
    fun setupPresence(userId: String) {
        val presenceRef = database.getReference("presence").child(userId)
        val connectedRef = database.getReference(".info/connected")
        
        connectedRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    presenceRef.onDisconnect().setValue(false)
                    presenceRef.setValue(true)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun listenToUserPresence(userId: String): Flow<Boolean> = callbackFlow {
        val ref = database.getReference("presence").child(userId)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // 3. Kirim Pesan
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null
    ): Message {
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val pendingMessage = Message(
            msgId = msgId, chatId = chatId, senderId = senderId, text = text,
            type = type, mediaUrl = mediaUrl, status = MessageStatus.PENDING, timestamp = timestamp
        )

        fileDb.appendMessage(chatId, pendingMessage)

        val chats = fileDb.readChats().toMutableList()
        val index = chats.indexOfFirst { it.chatId == chatId }
        if (index >= 0) {
            chats[index] = chats[index].copy(
                lastMessage = LastMessageInfo(
                    text = if (type == MessageType.TEXT) text else "[${type.name}]",
                    senderId = senderId, timestamp = timestamp, status = MessageStatus.PENDING, type = type
                )
            )
            fileDb.writeChats(chats)
        }

        val payload = mapOf(
            "msgId" to msgId, "chatId" to chatId, "senderId" to senderId,
            "text" to text, "type" to type.name, "mediaUrl" to (mediaUrl ?: ""),
            "timestamp" to ServerValue.TIMESTAMP, "status" to MessageStatus.SENT.name
        )

        database.getReference("messages").child(chatId).child(msgId).setValue(payload)
            .addOnSuccessListener {
                kotlinx.coroutines.GlobalScope.launch { fileDb.appendMessage(chatId, pendingMessage.copy(status = MessageStatus.SENT)) }
            }

        return pendingMessage
    }

    // 4. Listener Pesan per Chat
    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        trySend(fileDb.readMessages(chatId))
        val ref = database.getReference("messages").child(chatId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val msg = snapshot.getValue(Message::class.java) ?: return
                    launch {
                        fileDb.appendMessage(chatId, msg)
                        trySend(fileDb.readMessages(chatId))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val msg = snapshot.getValue(Message::class.java) ?: return
                    launch {
                        fileDb.appendMessage(chatId, msg)
                        trySend(fileDb.readMessages(chatId))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
