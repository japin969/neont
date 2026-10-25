package com.neont.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neont.messenger.data.FileDatabaseManager
import com.neont.messenger.data.ChatRepository
import com.neont.messenger.ui.screens.ChatDetailScreen
import com.neont.messenger.ui.screens.ChatListScreen
import com.neont.messenger.ui.theme.NeontTheme
import com.neont.messenger.ui.theme.TelegramDarkBG

class MainActivity : ComponentActivity() {

    private lateinit var fileDatabaseManager: FileDatabaseManager
    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Hybrid Architecture
        fileDatabaseManager = FileDatabaseManager(applicationContext)
        chatRepository = ChatRepository(fileDatabaseManager)

        setContent {
            NeontTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TelegramDarkBG
                ) {
                    NeontNavigation(
                        fileDb = fileDatabaseManager,
                        chatRepo = chatRepository
                    )
                }
            }
        }
    }
}

@Composable
fun NeontNavigation(
    fileDb: FileDatabaseManager,
    chatRepo: ChatRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "chat_list"
    ) {
        composable("chat_list") {
            ChatListScreen(
                fileDb = fileDb,
                onChatSelected = { chatId ->
                    navController.navigate("chat_detail/$chatId")
                }
            )
        }

        composable(
            route = "chat_detail/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatDetailScreen(
                chatId = chatId,
                fileDb = fileDb,
                chatRepo = chatRepo,
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}
