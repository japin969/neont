// Neont Messenger - NeontAppViewModel.kt
// ViewModel Terpusat: Hubungan Antara UI Compose, FileDatabaseManager (JSON lokal), dan Firebase Realtime Database
package com.neont.messenger.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.*
import com.neont.messenger.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NeontAppViewModel @Inject constructor(
    val authRepo: AuthRepository,
    val chatRepo: ChatRepository,
    val userRepo: UserRepository,
    val presenceRepo: PresenceRepository,
    val fileDb: FileDatabaseManager
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    private val _calls = MutableStateFlow<List<CallLog>>(emptyList())
    val calls: StateFlow<List<CallLog>> = _calls.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    var isAuthLoading by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)
        private set

    var isProfileUpdating by mutableStateOf(false)
        private set

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            // Baca data dari file JSON lokal secara offline-first
            val localChats = fileDb.readChats()
            _chats.value = localChats.sortedByDescending { it.isPinned }

            val localUsers = fileDb.readUsers()
            if (localUsers.isEmpty()) {
                val seedContacts = listOf(
                    User("u_pavel", "Pavel", "Durov", "pavel", "+1 234 567 890", "Founder of Telegram & Neont", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80", true, System.currentTimeMillis()),
                    User("u_elena", "Elena", "Rostova", "elena_r", "+33 612 345 678", "Product Designer", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80", false, System.currentTimeMillis() - 3600000),
                    User("u_alex", "Alex", "Volkov", "alex_v", "+44 789 012 345", "Senior Android Engineer", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=256&q=80", true, System.currentTimeMillis()),
                    User("u_sophia", "Sophia", "Chen", "sophia_c", "+65 9123 4567", "Security Researcher", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=256&q=80", false, System.currentTimeMillis() - 86400000)
                )
                fileDb.writeUsers(seedContacts)
                _contacts.value = seedContacts
            } else {
                _contacts.value = localUsers
            }

            _stories.value = fileDb.readStories()
            _calls.value = fileDb.readCalls()
            _settings.value = fileDb.readSettings()

            val uid = authRepo.currentUser?.uid ?: "me"
            val user = fileDb.readUsers().find { it.uid == uid } ?: User(
                uid = uid,
                firstName = "You",
                lastName = "",
                username = "me",
                bio = "Using Neont Messenger",
                phoneNumber = "+1 000 000 0000",
                isOnline = true
            )
            _currentUser.value = user
            chatRepo.setupPresence(uid)
        }
    }

    // ========================================================
    // CHAT MANAGEMENT (SWIPE PIN / MUTE / DELETE)
    // ========================================================
    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            val updated = fileDb.togglePinChat(chatId)
            _chats.value = updated
        }
    }

    fun toggleMuteChat(chatId: String) {
        viewModelScope.launch {
            val updated = fileDb.toggleMuteChat(chatId)
            _chats.value = updated
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            val updated = fileDb.deleteChat(chatId)
            _chats.value = updated
        }
    }

    fun createGroup(name: String, memberUids: List<String>) {
        viewModelScope.launch {
            val newGroup = Chat(
                chatId = "c_group_${UUID.randomUUID().toString().take(8)}",
                type = "group",
                title = name,
                avatarUrl = "",
                isPinned = false,
                isMuted = false,
                unreadCount = 0,
                memberUids = memberUids + listOf(_currentUser.value?.uid ?: "me"),
                lastMessage = LastMessageInfo("Group created", "me", System.currentTimeMillis(), MessageStatus.SENT, MessageType.TEXT)
            )
            fileDb.upsertChat(newGroup)
            val updated = fileDb.readChats().sortedByDescending { it.isPinned }
            _chats.value = updated
        }
    }

    // ========================================================
    // MESSAGING (OPTIMISTIC UI + FIREBASE)
    // ========================================================
    fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        mediaDurationSec: Int? = null
    ) {
        viewModelScope.launch {
            val senderId = _currentUser.value?.uid ?: "me"
            chatRepo.sendMessage(chatId, senderId, text, type, mediaUrl, mediaDurationSec)
            // Reload preview dari chats.json
            _chats.value = fileDb.readChats().sortedByDescending { it.isPinned }
        }
    }

    fun deleteMessage(chatId: String, msgId: String) {
        viewModelScope.launch {
            fileDb.deleteMessage(chatId, msgId)
        }
    }

    fun setTyping(chatId: String, isTyping: Boolean) {
        val uid = _currentUser.value?.uid ?: "me"
        chatRepo.setTypingStatus(chatId, uid, isTyping)
    }

    // ========================================================
    // STORIES (CameraX Photo Capture -> stories.json)
    // ========================================================
    fun addStory(photoUri: String, caption: String = "My Story") {
        viewModelScope.launch {
            val user = _currentUser.value
            val newStory = Story(
                storyId = "s_${UUID.randomUUID().toString().take(8)}",
                userId = user?.uid ?: "me",
                userName = "${user?.firstName ?: "You"} ${user?.lastName ?: ""}".trim(),
                mediaUrl = photoUri,
                caption = caption,
                timestamp = System.currentTimeMillis(),
                isViewed = false
            )
            val updated = fileDb.appendStory(newStory)
            _stories.value = updated
        }
    }

    // ========================================================
    // CALLS (Voice / Video -> calls.json)
    // ========================================================
    fun logCall(contactName: String, durationSec: Int, isVideo: Boolean) {
        viewModelScope.launch {
            val newCall = CallLog(
                callId = "call_${UUID.randomUUID().toString().take(8)}",
                contactName = contactName,
                contactAvatarUrl = "",
                callType = CallType.OUTGOING,
                isVideo = isVideo,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSec
            )
            val updated = fileDb.addCallLog(newCall)
            _calls.value = updated
        }
    }

    // ========================================================
    // AUTHENTICATION & PROFILE
    // ========================================================
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading = true
            authError = null
            authRepo.loginUser(email, pass)
                .onSuccess { firebaseUser ->
                    isAuthLoading = false
                    val u = User(
                        uid = firebaseUser.uid,
                        firstName = firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                        lastName = firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                        username = email.substringBefore("@"),
                        phoneNumber = "",
                        isOnline = true
                    )
                    fileDb.upsertUser(u)
                    _currentUser.value = u
                    chatRepo.setupPresence(firebaseUser.uid)
                    onSuccess()
                }
                .onFailure {
                    isAuthLoading = false
                    authError = it.localizedMessage ?: "Login failed"
                }
        }
    }

    fun register(email: String, pass: String, name: String, username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading = true
            authError = null
            authRepo.registerUser(email, pass, name, username)
                .onSuccess { firebaseUser ->
                    isAuthLoading = false
                    val parts = name.split(" ")
                    val u = User(
                        uid = firebaseUser.uid,
                        firstName = parts.firstOrNull() ?: name,
                        lastName = parts.drop(1).joinToString(" "),
                        username = username,
                        phoneNumber = "",
                        isOnline = true
                    )
                    fileDb.upsertUser(u)
                    _currentUser.value = u
                    chatRepo.setupPresence(firebaseUser.uid)
                    onSuccess()
                }
                .onFailure {
                    isAuthLoading = false
                    authError = it.localizedMessage ?: "Registration failed"
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepo.logoutUser()
            _currentUser.value = null
            onSuccess()
        }
    }

    fun updateProfile(firstName: String, lastName: String, bio: String, avatarUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProfileUpdating = true
            val current = _currentUser.value ?: User("me", firstName, lastName)
            val updated = current.copy(
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else current.avatarUrl
            )
            fileDb.upsertUser(updated)
            _currentUser.value = updated
            authRepo.updateUserProfile(firstName, lastName, bio, updated.avatarUrl)
            isProfileUpdating = false
            onSuccess()
        }
    }
}
