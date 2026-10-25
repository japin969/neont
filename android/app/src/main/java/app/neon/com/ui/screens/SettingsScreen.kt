// Neont Messenger - SettingsScreen.kt
// Pixel-Perfect Telegram Android Settings Screen
package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.User
import app.neon.com.ui.components.TelegramAvatar
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User? = null,
    settings: AppSettings = AppSettings(),
    onEditProfileClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSaveSettings: (AppSettings) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDataStorageClick: () -> Unit = {},
    onChatSettingsClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onDevicesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user = currentUser ?: User(
        uid = "me",
        firstName = "You",
        phoneNumber = "+62 812 3456 7890",
        username = "neont_user"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                )
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header User Profile Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramSurface)
                        .clickable { onEditProfileClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelegramAvatar(
                        name = "${user.firstName} ${user.lastName}".trim().ifEmpty { "You" },
                        avatarUrl = user.avatarUrl,
                        size = 64
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "You" },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = user.phoneNumber.ifEmpty { "+62 812 3456 7890" },
                            color = TelegramTextSecondary, // #8B9DAE
                            fontSize = 13.sp
                        )
                        Text(
                            text = "@${user.username.ifEmpty { "user" }}",
                            color = TelegramPrimary,
                            fontSize = 13.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = TelegramPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // Settings Items Section
            item {
                Column(modifier = Modifier.background(TelegramSurface)) {
                    Text(
                        text = "SETTINGS",
                        color = TelegramPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications and Sounds",
                        onClick = onNotificationClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy and Security",
                        onClick = onPrivacyClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.PieChart,
                        title = "Data and Storage",
                        onClick = onDataStorageClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Chat,
                        title = "Chat Settings",
                        onClick = onChatSettingsClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    SettingsMenuItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English",
                        onClick = onLanguageClick
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Logout Item
                Column(modifier = Modifier.background(TelegramSurface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogoutClick() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = TelegramRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Log Out",
                            color = TelegramRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TelegramPrimary,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
