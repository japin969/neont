// Neont Messenger - MainScreen.kt
// Layar Utama dengan Telegram Drawer dan 4 Tab Bottom Navigation Terhubung Penuh
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.*
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.MainTab
import com.neont.messenger.ui.components.NeontBottomNav
import com.neont.messenger.ui.components.TelegramAvatar
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    currentUser: User?,
    chats: List<Chat>,
    contacts: List<User>,
    calls: List<CallLog>,
    stories: List<Story>,
    settings: AppSettings,
    onChatClick: (String) -> Unit,
    onContactClick: (User) -> Unit,
    onCallClick: (CallLog) -> Unit,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.CHATS) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF17212B),
                modifier = Modifier.width(300.dp)
            ) {
                // Header Profil Pengguna
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramDarkHeader)
                        .padding(16.dp)
                ) {
                    TelegramAvatar(
                        name = "${currentUser?.firstName ?: "User"} ${currentUser?.lastName ?: ""}",
                        avatarUrl = currentUser?.avatarUrl,
                        size = 64
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${currentUser?.firstName ?: "User"} ${currentUser?.lastName ?: ""}".trim(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = currentUser?.phoneNumber ?: "+1 415 555 0199",
                        color = Color(0xFF7F92A2),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "@${currentUser?.username ?: "neont"}",
                        color = TelegramLightBlue,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Menu Navigasi Drawer Khas Telegram
                DrawerMenuItem(icon = Icons.Default.Group, title = "New Group") {
                    coroutineScope.launch { drawerState.close() }
                    onNewGroupClick()
                }
                DrawerMenuItem(icon = Icons.Default.Person, title = "Contacts") {
                    coroutineScope.launch { drawerState.close() }
                    selectedTab = MainTab.CONTACTS
                }
                DrawerMenuItem(icon = Icons.Default.Call, title = "Calls") {
                    coroutineScope.launch { drawerState.close() }
                    selectedTab = MainTab.CALLS
                }
                DrawerMenuItem(icon = Icons.Default.Bookmark, title = "Saved Messages") {
                    coroutineScope.launch { drawerState.close() }
                    onChatClick("c_saved")
                }
                DrawerMenuItem(icon = Icons.Default.Settings, title = "Settings") {
                    coroutineScope.launch { drawerState.close() }
                    selectedTab = MainTab.SETTINGS
                }

                Divider(color = Color(0xFF101419), modifier = Modifier.padding(vertical = 8.dp))

                DrawerMenuItem(icon = Icons.Default.ExitToApp, title = "Log Out") {
                    coroutineScope.launch { drawerState.close() }
                    onLogoutClick()
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                val totalUnread = remember(chats) { chats.sumOf { it.unreadCount } }
                NeontBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    unreadMessagesCount = totalUnread
                )
            },
            containerColor = TelegramDarkBackground,
            modifier = modifier
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab) {
                    MainTab.CHATS -> {
                        ChatListScreen(
                            chats = chats,
                            stories = stories,
                            onChatClick = onChatClick,
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onFabClick = onNewGroupClick,
                            onStoryClick = onStoryClick,
                            onAddStoryClick = onAddStoryClick,
                            onSearchClick = onSearchClick,
                            onTogglePin = onTogglePin,
                            onToggleMute = onToggleMute,
                            onDeleteChat = onDeleteChat
                        )
                    }
                    MainTab.CONTACTS -> {
                        ContactsScreen(
                            contacts = contacts,
                            onContactClick = onContactClick,
                            onBackClick = { selectedTab = MainTab.CHATS },
                            onAddContactClick = onSearchClick
                        )
                    }
                    MainTab.CALLS -> {
                        CallsScreen(
                            calls = calls,
                            onCallClick = onCallClick,
                            onNewCallClick = { selectedTab = MainTab.CONTACTS }
                        )
                    }
                    MainTab.SETTINGS -> {
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
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF7F92A2),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
