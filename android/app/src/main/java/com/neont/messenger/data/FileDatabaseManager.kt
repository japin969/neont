package com.neont.messenger.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.LastMessageInfo
import com.neont.messenger.data.model.Message
import com.neont.messenger.data.model.MessageStatus
import com.neont.messenger.data.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class FileDatabaseManager(private val context: Context) {

    private val gson = Gson()
    private val dbDir = File(context.filesDir, "neont_db").apply {
        if (!exists()) mkdirs()
    }

    private val mutexMap = mutableMapOf<String, Mutex>()
    private val globalLock = Any()

    init {
        seedInitialData()
    }

    private fun getMutex(filename: String): Mutex = synchronized(globalLock) {
        mutexMap.getOrPut(filename) { Mutex() }
    }

    suspend fun readChats(): List<Chat> = withContext(Dispatchers.IO) {
        val filename = "chats.json"
        val file = File(dbDir, filename)
        getMutex(filename).withLock {
            if (!file.exists()) return@withContext emptyList()
            try {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, object : TypeToken<List<Chat>>() {}.type) ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun writeChats(chats: List<Chat>) = withContext(Dispatchers.IO) {
        val filename = "chats.json"
        val file = File(dbDir, filename)
        getMutex(filename).withLock {
            atomicWrite(file, chats)
        }
    }

    suspend fun readMessages(chatId: String): List<Message> = withContext(Dispatchers.IO) {
        val filename = "messages_${chatId}.json"
        val file = File(dbDir, filename)
        getMutex(filename).withLock {
            if (!file.exists()) return@withContext emptyList()
            try {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, object : TypeToken<List<Message>>() {}.type) ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun appendMessage(chatId: String, message: Message): List<Message> = withContext(Dispatchers.IO) {
        val filename = "messages_${chatId}.json"
        val file = File(dbDir, filename)
        getMutex(filename).withLock {
            val currentList = if (file.exists()) {
                try {
                    FileReader(file).use { reader ->
                        val data: List<Message>? = gson.fromJson(reader, object : TypeToken<List<Message>>() {}.type)
                        data?.toMutableList() ?: mutableListOf()
                    }
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            val existingIndex = currentList.indexOfFirst { it.msgId == message.msgId }
            if (existingIndex >= 0) {
                currentList[existingIndex] = message
            } else {
                currentList.add(message)
            }

            atomicWrite(file, currentList)
            currentList
        }
    }

    private fun <T> atomicWrite(targetFile: File, data: T) {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        try {
            FileWriter(tempFile).use { writer ->
                gson.toJson(data, writer)
            }
            if (tempFile.exists()) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun seedInitialData() {
        val chatsFile = File(dbDir, "chats.json")
        if (!chatsFile.exists()) {
            val initialChats = listOf(
                Chat(
                    chatId = "c_pavel",
                    title = "Pavel Durov",
                    avatarUrl = "",
                    isPinned = true,
                    unreadCount = 2,
                    lastMessage = LastMessageInfo(
                        text = "Native Android Kotlin + Jetpack Compose ready.",
                        senderId = "u_pavel",
                        timestamp = System.currentTimeMillis() - 120000,
                        status = MessageStatus.READ,
                        type = MessageType.TEXT
                    )
                ),
                Chat(
                    chatId = "c_elena",
                    title = "Elena Rostova",
                    avatarUrl = "",
                    isPinned = false,
                    unreadCount = 0,
                    lastMessage = LastMessageInfo(
                        text = "Voice message (0:14)",
                        senderId = "u_elena",
                        timestamp = System.currentTimeMillis() - 2400000,
                        status = MessageStatus.READ,
                        type = MessageType.VOICE
                    )
                ),
                Chat(
                    chatId = "c_saved",
                    title = "Saved Messages",
                    avatarUrl = "",
                    isPinned = true,
                    unreadCount = 0,
                    lastMessage = LastMessageInfo(
                        text = "JSON file persistence at context.filesDir/neont_db/",
                        senderId = "me",
                        timestamp = System.currentTimeMillis() - 3600000,
                        status = MessageStatus.SENT,
                        type = MessageType.TEXT
                    )
                )
            )
            atomicWrite(chatsFile, initialChats)
        }

        val pavelFile = File(dbDir, "messages_c_pavel.json")
        if (!pavelFile.exists()) {
            val msgs = listOf(
                Message(
                    msgId = "m_1",
                    chatId = "c_pavel",
                    senderId = "u_pavel",
                    text = "Hello! Neont native Android code is running completely offline-first with JSON files.",
                    type = MessageType.TEXT,
                    status = MessageStatus.READ,
                    timestamp = System.currentTimeMillis() - 600000
                ),
                Message(
                    msgId = "m_2",
                    chatId = "c_pavel",
                    senderId = "me",
                    text = "Confirmed! All UI components are rendered using 100% Jetpack Compose.",
                    type = MessageType.TEXT,
                    status = MessageStatus.READ,
                    timestamp = System.currentTimeMillis() - 300000
                ),
                Message(
                    msgId = "m_3",
                    chatId = "c_pavel",
                    senderId = "u_pavel",
                    text = "Native Android Kotlin + Jetpack Compose ready.",
                    type = MessageType.TEXT,
                    status = MessageStatus.READ,
                    timestamp = System.currentTimeMillis() - 120000
                )
            )
            atomicWrite(pavelFile, msgs)
        }
    }
}
