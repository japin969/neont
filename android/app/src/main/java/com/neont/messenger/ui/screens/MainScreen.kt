package com.neont.messenger.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neont.messenger.data.FileDatabaseManager
import com.neont.messenger.data.model.*
import com.neont.messenger.ui.components.BottomNavBar
import com.neont.messenger.ui.components.MainTab
import com.neont.messenger.ui.theme.TelegramDarkBG

@Composable
fun MainScreen(
    currentUser: User? = null,
    chats: List<Chat>? = null,
    contacts: List<User>? = null,
    calls: List<CallLog>? = null,
    stories: List<Story>? = null,
    settings: AppSettings = AppSettings(),
    fileDb: FileDatabaseManager? = null,
    onChatClick: (chatId: String) -> Unit = {},
    onChatSelected: (chatId: String) -> Unit = onChatClick,
    onContactClick: (User) -> Unit = {},
    onCallClick: (CallLog) -> Unit = {},
    onStoryClick: (Story) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
    onStoryCameraClick: () -> Unit = onAddStoryClick,
    onSearchClick: () -> Unit = {},
    onNewGroupClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onTogglePin: (chatId: String) -> Unit = {},
    onToggleMute: (chatId: String) -> Unit = {},
    onDeleteChat: (chatId: String) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainTab.CHATS) }
    val tabNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    tabNavController.navigate(tab.name) {
                        popUpTo(tabNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = tabNavController,
                startDestination = MainTab.CHATS.name
            ) {
                composable(MainTab.CHATS.name) {
                    ChatListScreen(
                        chats = chats,
                        stories = stories,
                        fileDb = fileDb,
                        onChatSelected = onChatSelected,
                        onChatClick = onChatClick,
                        onStoryClick = onStoryClick,
                        onAddStoryClick = onAddStoryClick,
                        onSearchClick = onSearchClick,
                        onNewMessageClick = onNewGroupClick,
                        onTogglePin = onTogglePin,
                        onToggleMute = onToggleMute,
                        onDeleteChat = onDeleteChat
                    )
                }
                composable(MainTab.CONTACTS.name) {
                    ContactsScreen(
                        contacts = contacts,
                        fileDb = fileDb,
                        onContactClick = onContactClick
                    )
                }
                composable(MainTab.CALLS.name) {
                    CallsScreen(
                        calls = calls,
                        fileDb = fileDb,
                        onCallClick = onCallClick
                    )
                }
                composable(MainTab.SETTINGS.name) {
                    SettingsScreen(
                        currentUser = currentUser,
                        settings = settings,
                        onEditProfileClick = onEditProfileClick,
                        onPrivacyClick = onPrivacyClick,
                        onNotificationClick = onNotificationClick,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}
