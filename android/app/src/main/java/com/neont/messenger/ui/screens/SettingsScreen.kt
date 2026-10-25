// Neont Messenger - SettingsScreen.kt
// Layar Pengaturan Utama Telegram Android
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.AppSettings
import com.neont.messenger.data.model.User
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkDivider
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.TelegramAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    settings: AppSettings,
    onEditProfileClick: () -> Unit,
    onPrivacyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onEditProfileClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Profile Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkHeader)
                    .clickable { onEditProfileClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelegramAvatar(name = currentUser?.firstName ?: "User", avatarUrl = currentUser?.avatarUrl, size = 64)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("${currentUser?.firstName ?: "User"} ${currentUser?.lastName ?: ""}".trim(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(currentUser?.phoneNumber ?: "+1 415 555 0199", color = Color(0xFF7F92A2), fontSize = 14.sp)
                    Text("@${currentUser?.username ?: "neont"}", color = TelegramLightBlue, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Settings", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsItem(icon = Icons.Default.Notifications, title = "Notifications and Sounds", subtitle = if (settings.notificationsEnabled) "Enabled" else "Disabled", onClick = onNotificationClick)
            SettingsItem(icon = Icons.Default.Lock, title = "Privacy and Security", subtitle = "Passcode, Two-Step Verification", onClick = onPrivacyClick)
            SettingsItem(icon = Icons.Default.Storage, title = "Data and Storage", subtitle = "${settings.cacheSizeMb} MB cache used")
            SettingsItem(icon = Icons.Default.ColorLens, title = "Chat Settings", subtitle = "Theme: ${settings.theme}")

            Divider(color = TelegramDarkDivider, thickness = 8.dp)

            SettingsItem(icon = Icons.Default.ExitToApp, title = "Log Out", subtitle = null, titleColor = Color.Red, onClick = onLogoutClick)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (titleColor == Color.Red) Color.Red else Color(0xFF7F92A2), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = Color(0xFF7F92A2), fontSize = 13.sp)
            }
        }
    }
}
