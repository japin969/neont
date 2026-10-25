// Neont Messenger - Models.kt
// Data Model Kotlin untuk File-based JSON Persistence & Firebase Realtime Database
package app.neon.com.data.model

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeenTimestamp: Long = 0L,
    val lastSeen: Long = lastSeenTimestamp
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, VOICE, FILE, LOCATION, CONTACT
}

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ, FAILED
}

data class LastMessageInfo(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT,
    val type: MessageType = MessageType.TEXT
)

data class Chat(
    val chatId: String = "",
    val type: String = "direct", // "direct", "group", "channel"
    val title: String = "",
    val avatarUrl: String = "",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val unreadCount: Int = 0,
    val memberUids: List<String> = emptyList(),
    val lastMessage: LastMessageInfo? = null
)

data class Message(
    val msgId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val cipherText: String? = null,
    val iv: String? = null,
    val encryptedKey: String? = null,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val mediaDurationSec: Int? = null,
    val replyToMsgId: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = 0L,
    val waveform: List<Int>? = null
)

data class Story(
    val storyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0L,
    val isViewed: Boolean = false
)

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

data class CallLog(
    val callId: String = "",
    val contactName: String = "",
    val contactAvatarUrl: String = "",
    val callType: CallType = CallType.OUTGOING,
    val isVideo: Boolean = false,
    val timestamp: Long = 0L,
    val durationSeconds: Int = 0
) {
    // Helper compatibility properties
    val userName: String get() = contactName
    val userAvatar: String get() = contactAvatarUrl
    val type: String get() = when (callType) {
        CallType.INCOMING -> "incoming"
        CallType.MISSED -> "missed"
        CallType.OUTGOING -> "outgoing"
    }
}

data class PrivacySettings(
    val phoneNumberVisibility: String = "My Contacts",
    val lastSeenVisibility: String = "My Contacts",
    val profilePhotoVisibility: String = "Everybody",
    val callsP2P: String = "Everybody",
    val passcodeLockEnabled: Boolean = false,
    val twoStepVerificationEnabled: Boolean = true,
    val twoStepPassword: String = "",
    val autoDeleteTimer: String = "Off" // "Off", "24 Hours", "7 Days", "1 Month"
)

data class NotificationSettings(
    val privateChatsEnabled: Boolean = true,
    val groupsEnabled: Boolean = true,
    val channelsEnabled: Boolean = false,
    val inAppSounds: Boolean = true,
    val inAppVibrate: Boolean = true,
    val inAppPreview: Boolean = true
)

data class AppSettings(
    val theme: String = "dark", // "dark", "light", "system"
    val accentColor: String = "#5288C1",
    val textSize: Int = 16,
    val privacy: PrivacySettings = PrivacySettings(),
    val notifications: NotificationSettings = NotificationSettings()
)
