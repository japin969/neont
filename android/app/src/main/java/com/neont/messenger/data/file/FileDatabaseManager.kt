// Neont Messenger - FileDatabaseManager.kt
// File-based local persistence storage di context.filesDir/neont_db/
// Mutex per-file + Coroutines (Dispatchers.IO) + Atomic write (.tmp -> rename)
package com.neont.messenger.data.file

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neont.messenger.data.crypto.RealMessageCrypto
import com.neont.messenger.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDatabaseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dbDir = File(context.filesDir, "neont_db").apply { mkdirs() }
    private val mutexMap = mutableMapOf<String, Mutex>()
    private val globalLock = Any()

    init {
        seedInitialDataIfEmpty()
    }

    private fun mutexFor(fileName: String): Mutex = synchronized(globalLock) {
        mutexMap.getOrPut(fileName) { Mutex() }
    }

    // ========================================================
    // USERS (users.json)
    // ========================================================
    suspend fun readUsers(): List<User> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            readJson(file, object : TypeToken<List<User>>() {}) ?: emptyList()
        }
    }

    suspend fun writeUsers(users: List<User>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            writeJson(file, users)
        }
    }

    suspend fun upsertUser(user: User) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            val current = (readJson<List<User>>(file, object : TypeToken<List<User>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.uid == user.uid }
            if (index >= 0) {
                current[index] = user
            } else {
                current.add(user)
            }
            writeJson(file, current)
        }
    }

    // ========================================================
    // CHATS (chats.json)
    // ========================================================
    suspend fun readChats(): List<Chat> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            readJson(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()
        }
    }

    suspend fun writeChats(chats: List<Chat>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            writeJson(file, chats)
        }
    }

    suspend fun upsertChat(chat: Chat) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.chatId == chat.chatId }
            if (index >= 0) {
                current[index] = chat
            } else {
                current.add(0, chat)
            }
            writeJson(file, current)
        }
    }

    suspend fun togglePinChat(chatId: String): List<Chat> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.chatId == chatId }
            if (index >= 0) {
                val updated = current[index].copy(isPinned = !current[index].isPinned)
                current[index] = updated
                // Sort pinned first
                val sorted = current.sortedByDescending { it.isPinned }
                writeJson(file, sorted)
                sorted
            } else current
        }
    }

    suspend fun toggleMuteChat(chatId: String): List<Chat> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.chatId == chatId }
            if (index >= 0) {
                val updated = current[index].copy(isMuted = !current[index].isMuted)
                current[index] = updated
                writeJson(file, current)
                current
            } else current
        }
    }

    suspend fun deleteChat(chatId: String): List<Chat> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        val updated = mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()).filterNot { it.chatId == chatId }
            writeJson(file, current)
            current
        }
        val msgFile = File(dbDir, "messages_${chatId}.json")
        mutexFor("messages_${chatId}.json").withLock {
            if (msgFile.exists()) msgFile.delete()
        }
        updated
    }

    // ========================================================
    // MESSAGES (messages_{chatId}.json) - E2EE ON-DISK STORAGE
    // ========================================================
    suspend fun readMessages(chatId: String): List<Message> = withContext(Dispatchers.IO) {
        val fileName = "messages_${chatId}.json"
        val file = File(dbDir, fileName)
        val encryptedList: List<Message> = mutexFor(fileName).withLock {
            readJson(file, object : TypeToken<List<Message>>() {}) ?: emptyList()
        }
        // Dekripsi in-memory saat dibaca untuk UI
        encryptedList.map { msg ->
            if (!msg.cipherText.isNullOrBlank() && !msg.iv.isNullOrBlank()) {
                val plain = RealMessageCrypto.decrypt(msg.cipherText, msg.iv, chatId, msg.text)
                msg.copy(text = plain)
            } else {
                msg
            }
        }
    }

    suspend fun writeMessages(chatId: String, messages: List<Message>) = withContext(Dispatchers.IO) {
        val fileName = "messages_${chatId}.json"
        val file = File(dbDir, fileName)
        val encryptedMessages = messages.map { msg ->
            if (msg.cipherText.isNullOrBlank() && msg.text.isNotBlank()) {
                val encrypted = RealMessageCrypto.encrypt(msg.text, chatId)
                msg.copy(text = "", cipherText = encrypted.cipherText, iv = encrypted.iv, encryptedKey = encrypted.encryptedKey)
            } else {
                msg
            }
        }
        mutexFor(fileName).withLock {
            writeJson(file, encryptedMessages)
        }
    }

    suspend fun appendMessage(chatId: String, message: Message): List<Message> = withContext(Dispatchers.IO) {
        val fileName = "messages_${chatId}.json"
        val file = File(dbDir, fileName)

        // Simpan ke disk dalam bentuk terenkripsi (CipherText & IV)
        val diskMsg = if (message.cipherText.isNullOrBlank() && message.text.isNotBlank()) {
            val encrypted = RealMessageCrypto.encrypt(message.text, chatId)
            message.copy(text = "", cipherText = encrypted.cipherText, iv = encrypted.iv, encryptedKey = encrypted.encryptedKey)
        } else {
            message
        }

        mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).toMutableList()
            val existingIndex = current.indexOfFirst { it.msgId == diskMsg.msgId }
            if (existingIndex >= 0) {
                current[existingIndex] = diskMsg
            } else {
                current.add(diskMsg)
            }
            writeJson(file, current)

            // Kembalikan in-memory list terdekripsi untuk UI
            current.map { msg ->
                if (!msg.cipherText.isNullOrBlank() && !msg.iv.isNullOrBlank()) {
                    msg.copy(text = RealMessageCrypto.decrypt(msg.cipherText, msg.iv, chatId, msg.text))
                } else msg
            }
        }
    }

    suspend fun deleteMessage(chatId: String, msgId: String): List<Message> = withContext(Dispatchers.IO) {
        val fileName = "messages_${chatId}.json"
        val file = File(dbDir, fileName)
        val updated = mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).filterNot { it.msgId == msgId }
            writeJson(file, current)
            current
        }
        updated.map { msg ->
            if (!msg.cipherText.isNullOrBlank() && !msg.iv.isNullOrBlank()) {
                msg.copy(text = RealMessageCrypto.decrypt(msg.cipherText, msg.iv, chatId, msg.text))
            } else msg
        }
    }

    suspend fun updateMessageStatus(chatId: String, msgId: String, status: MessageStatus) = withContext(Dispatchers.IO) {
        val fileName = "messages_${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.msgId == msgId }
            if (index >= 0) {
                current[index] = current[index].copy(status = status)
                writeJson(file, current)
            }
        }
    }

    // ========================================================
    // STORIES (stories.json)
    // ========================================================
    suspend fun readStories(): List<Story> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "stories.json")
        mutexFor("stories.json").withLock {
            readJson(file, object : TypeToken<List<Story>>() {}) ?: emptyList()
        }
    }

    suspend fun writeStories(stories: List<Story>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "stories.json")
        mutexFor("stories.json").withLock {
            writeJson(file, stories)
        }
    }

    suspend fun appendStory(story: Story): List<Story> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "stories.json")
        mutexFor("stories.json").withLock {
            val current = (readJson<List<Story>>(file, object : TypeToken<List<Story>>() {}) ?: emptyList()).toMutableList()
            current.add(0, story) // Newest story first
            writeJson(file, current)
            current
        }
    }

    // ========================================================
    // CALLS (calls.json)
    // ========================================================
    suspend fun readCalls(): List<CallLog> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "calls.json")
        mutexFor("calls.json").withLock {
            readJson(file, object : TypeToken<List<CallLog>>() {}) ?: emptyList()
        }
    }

    suspend fun writeCalls(calls: List<CallLog>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "calls.json")
        mutexFor("calls.json").withLock {
            writeJson(file, calls)
        }
    }

    suspend fun addCallLog(call: CallLog): List<CallLog> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "calls.json")
        mutexFor("calls.json").withLock {
            val current = (readJson<List<CallLog>>(file, object : TypeToken<List<CallLog>>() {}) ?: emptyList()).toMutableList()
            current.add(0, call)
            writeJson(file, current)
            current
        }
    }

    // ========================================================
    // SETTINGS (settings.json)
    // ========================================================
    suspend fun readSettings(): AppSettings = withContext(Dispatchers.IO) {
        val file = File(dbDir, "settings.json")
        mutexFor("settings.json").withLock {
            readJson(file, object : TypeToken<AppSettings>() {}) ?: AppSettings()
        }
    }

    suspend fun writeSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "settings.json")
        mutexFor("settings.json").withLock {
            writeJson(file, settings)
        }
    }

    // ========================================================
    // ATOMIC WRITE (.tmp -> rename) & READ HELPERS
    // ========================================================
    private fun <T> readJson(file: File, type: TypeToken<T>): T? {
        if (!file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                gson.fromJson<T>(reader, type.type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> writeJson(file: File, data: T) {
        val tempFile = File(file.parentFile, "${file.name}.tmp")
        try {
            FileWriter(tempFile).use { writer ->
                gson.toJson(data, writer)
            }
            if (tempFile.exists()) {
                if (file.exists()) file.delete()
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun <T> writeJsonDirect(file: File, data: T) {
        try {
            FileWriter(file).use { writer ->
                gson.toJson(data, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seedInitialDataIfEmpty() {
        val chatsFile = File(dbDir, "chats.json")
        if (!chatsFile.exists()) {
            val initialChats = listOf(
                Chat(
                    chatId = "c_saved",
                    type = "direct",
                    title = "Saved Messages",
                    avatarUrl = "",
                    isPinned = true,
                    unreadCount = 0,
                    memberUids = listOf("me"),
                    lastMessage = LastMessageInfo("Architecture notes & FileDatabaseManager.kt", "me", System.currentTimeMillis() - 900000, MessageStatus.READ, MessageType.TEXT)
                ),
                Chat(
                    chatId = "c_pavel",
                    type = "direct",
                    title = "Pavel Durov",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
                    isPinned = true,
                    unreadCount = 2,
                    memberUids = listOf("me", "u_pavel"),
                    lastMessage = LastMessageInfo("File persistence ensures cold boot without SQLite lock contention.", "u_pavel", System.currentTimeMillis() - 240000, MessageStatus.READ, MessageType.TEXT)
                ),
                Chat(
                    chatId = "c_elena",
                    type = "direct",
                    title = "Elena Rostova",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80",
                    isPinned = false,
                    unreadCount = 0,
                    memberUids = listOf("me", "u_elena"),
                    lastMessage = LastMessageInfo("Voice message (0:14)", "u_elena", System.currentTimeMillis() - 2700000, MessageStatus.READ, MessageType.VOICE)
                )
            )
            writeJsonDirect(chatsFile, initialChats)
        }

        val pavelFile = File(dbDir, "messages_c_pavel.json")
        if (!pavelFile.exists()) {
            val msgs = listOf(
                Message("m1", "c_pavel", "u_pavel", "Hi! Welcome to Neont. How is the reverse-engineered Android UI looking?", MessageType.TEXT, null, null, null, MessageStatus.READ, System.currentTimeMillis() - 1800000),
                Message("m2", "c_pavel", "me", "Working seamlessly! We adopted the hybrid architecture: local internal JSON storage + Firebase Realtime sync.", MessageType.TEXT, null, null, null, MessageStatus.READ, System.currentTimeMillis() - 1200000),
                Message("m3", "c_pavel", "u_pavel", "File persistence ensures cold boot without SQLite lock contention.", MessageType.TEXT, null, null, null, MessageStatus.READ, System.currentTimeMillis() - 240000)
            )
            writeJsonDirect(pavelFile, msgs)
        }

        val storiesFile = File(dbDir, "stories.json")
        if (!storiesFile.exists()) {
            val initialStories = listOf(
                Story("s1", "u_pavel", "Pavel Durov", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80", "Dubai HQ Building update", System.currentTimeMillis() - 7200000, false),
                Story("s2", "u_elena", "Elena Rostova", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=600&q=80", "Coffee time in Paris", System.currentTimeMillis() - 14400000, false)
            )
            writeJsonDirect(storiesFile, initialStories)
        }
    }
}
