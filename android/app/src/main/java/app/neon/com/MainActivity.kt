package app.neon.com

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import app.neon.com.utils.UpdateChecker
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
                    AppRoot(fileDb, chatRepo, applicationContext)
                }
            }
        }
    }
}

@Composable
fun AppRoot(fileDb: FileDatabaseManager, chatRepo: ChatRepository, context: Context) {
    // State buat update
    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var isChecking by remember { mutableStateOf(true) }

    // Cek update pas aplikasi dibuka
    LaunchedEffect(Unit) {
        val info = UpdateChecker.checkForUpdate()
        val currentVersionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        
        // Kalau dapet info versi baru, dan versi server > versi HP, GEMBOK!
        if (info != null && info.latestVersionCode > currentVersionCode) {
            updateInfo = info
        }
        isChecking = false
    }

    if (isChecking) {
        // SPLASH SCREEN (Loading)
        Box(modifier = Modifier.fillMaxSize().background(TelegramDarkBG), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        } else if (updateInfo != null) {
        // LAYAR GEMBOK (Gak bisa di-back, gak bisa ditutup)
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).background(TelegramDarkBG),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Update Tersedia", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(updateInfo!!.message, color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Cek link download di bio TikTok atau WhatsApp kami.", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    } else {
        // JALANIN APLIKASI NORMAL (Bisa offline, bisa kirim pesan kalau online)
        NeontApp(fileDb, chatRepo, context)
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

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && selectedChatId != null) {
            scope.launch {
                isUploading = true
                try {
                    val contentResolver = context.contentResolver
                    var fileName = "file_${System.currentTimeMillis()}"
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
                if (option == "Gallery") filePicker.launch("image/*")
                else filePicker.launch("*/*")
            }
        )
    }
}
