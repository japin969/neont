package app.neon.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.*
import app.neon.com.ui.screens.MainScreen
import app.neon.com.ui.theme.NeontTheme
import app.neon.com.ui.theme.TelegramDarkBG
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fileDb: FileDatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileDb = FileDatabaseManager(applicationContext)

        setContent {
            NeontTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = TelegramDarkBG) {
                    NeontApp(fileDb)
                }
            }
        }
    }
}

@Composable
fun NeontApp(fileDb: FileDatabaseManager) {
    val scope = rememberCoroutineScope()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var contacts by remember { mutableStateOf<List<User>>(emptyList()) }
    var calls by remember { mutableStateOf<List<CallLog>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var settings by remember { mutableStateOf(AppSettings()) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Ambil data dari JSON pas aplikasi dibuka
    LaunchedEffect(Unit) {
        scope.launch {
            chats = fileDb.readChats()
            contacts = fileDb.readUsers()
            calls = fileDb.readCalls()
            stories = fileDb.readStories()
            settings = fileDb.readSettings()
            currentUser = contacts.firstOrNull { it.uid == "me" }
        }
    }

    // Lempar datanya ke UI Telegram lu
    MainScreen(
        currentUser = currentUser,
        chats = chats,
        contacts = contacts,
        calls = calls,
        stories = stories,
        settings = settings,
        fileDb = fileDb,
        // Logika tombol (sementara di-kosongin biar gak error, UI tetep jalan)
        onChatClick = {},
        onContactClick = {},
        onCallClick = {},
        onStoryClick = {},
        onSearchClick = {},
        onNewGroupClick = {},
        onEditProfileClick = {},
        onPrivacyClick = {},
        onNotificationClick = {},
        onTogglePin = {},
        onToggleMute = {},
        onDeleteChat = {},
        onLogoutClick = {}
    )
}
