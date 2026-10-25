package app.neon.com

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.neon.com.data.ChatRepository
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.FilebaseStorage
import app.neon.com.data.model.*
import app.neon.com.ui.screens.ChatDetailScreen
import app.neon.com.ui.screens.MainScreen
import app.neon.com.ui.theme.NeontTheme
import app.neon.com.ui.theme.TelegramDarkBG
import app.neon.com.utils.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fileDb: FileDatabaseManager
    private lateinit var chatRepo: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileDb = FileDatabaseManager(applicationContext)
        chatRepo = ChatRepository(fileDb)

        setContent {
            NeontTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = TelegramDarkBG) {
                    NeontApp(fileDb, chatRepo, applicationContext)
                }
            }
        }
    }
}

@Composable
fun NeontApp(fileDb: FileDatabaseManager, chatRepo: ChatRepository, context: Context) {
    val scope = rememberCoroutineScope()
    val filebase = remember { FilebaseStorage(context) }
    
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var contacts by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    var chatMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    // Picker buat milih foto/file dari HP
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && selectedChatId != null) {
            scope.launch {
                isUploading = true
                try {
                    val contentResolver = context.contentResolver
                    var fileName = "file_${System.currentTimeMillis()}"
                    var fileSize = 0L
                    
                    // Ambil nama asli file
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                        }
                    }
                    
                    val inputStream = contentResolver.openInputStream(uri)
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                    
                    if (inputStream != null) {
                        val fileUrl = filebase.uploadFile(fileName, mimeType, inputStream)
                        inputStream.close()
                        
                        // Kirim URL file ke Firebase sebagai pesan teks
                        val type = if (mimeType.startsWith("image/")) MessageType.IMAGE else if (mimeType.startsWith("video/")) MessageType.VIDEO else MessageType.FILE
                        chatRepo.sendMessage(selectedChatId!!, "me", fileUrl, type, fileUrl)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    // Izin Notifikasi Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            chats = fileDb.readChats()
            contacts = fileDb.readUsers()
            currentUser = contacts.firstOrNull { it.uid == "me" }
        }
    }

    LaunchedEffect(selectedChatId) {
        if (selectedChatId != null) {
            selectedChat = chats.firstOrNull { it.chatId == selectedChatId }
            chatRepo.listenToMessages(selectedChatId!!).collect { msgs ->
                chatMessages = msgs
                if (msgs.isNotEmpty() && msgs.last().senderId != "me") {
                    NotificationHelper.showNotification(context, selectedChat?.title ?: "New Message", msgs.last().text)
                }
            }
        } else {
            chatMessages = emptyList()
        }
    }

    if (selectedChatId == null) {
        MainScreen(
            currentUser = currentUser,
            chats = chats,
            contacts = contacts,
            stories = emptyList(),
            calls = emptyList(),
            settings = AppSettings(),
            fileDb = fileDb,
            onChatClick = { chatId -> selectedChatId = chatId }
        )
    } else {
        ChatDetailScreen(
            chatId = selectedChatId,
            chat = selectedChat,
            messages = chatMessages,
            currentUserId = "me",
            fileDb = fileDb,
            chatRepo = chatRepo,
            onBackClicked = { selectedChatId = null },
            onSendMessage = { text ->
                scope.launch {
                    selectedChatId?.let { chatId ->
                        chatRepo.sendMessage(chatId, "me", text)
                    }
                }
            },
            onAttachClick = { option ->
                // Buka file picker sesuai pilihan
                if (option == "Gallery") {
                    filePicker.launch("image/*")
                } else if (option == "File") {
                    filePicker.launch("*/*")
                } else {
                    // Fallback buat VN / Music
                    filePicker.launch("*/*")
                }
            }
        )
    }
}
