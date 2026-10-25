// Neont Messenger - NotificationSettingsScreen.kt
// Layar Notifikasi dan Suara Telegram Android
package app.neon.com.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.theme.TelegramDarkBackground
import app.neon.com.theme.TelegramDarkHeader
import app.neon.com.theme.TelegramLightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit
) {
    var privateChatsEnabled by remember { mutableStateOf(true) }
    var groupsEnabled by remember { mutableStateOf(true) }
    var channelsEnabled by remember { mutableStateOf(false) }
    var vibrateEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Notifications and Sounds", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Message notifications", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp))

            SwitchSettingsRow("Private Chats", "Tap to change preview and sound", privateChatsEnabled) { privateChatsEnabled = it }
            SwitchSettingsRow("Groups", "Tap to change preview and sound", groupsEnabled) { groupsEnabled = it }
            SwitchSettingsRow("Channels", "Silent by default", channelsEnabled) { channelsEnabled = it }

            Divider(color = Color(0xFF101419), thickness = 8.dp)

            Text("In-app notifications", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp))

            SwitchSettingsRow("In-App Sounds", "Play sound on incoming message", true) {}
            SwitchSettingsRow("In-App Vibrate", "Vibrate device", vibrateEnabled) { vibrateEnabled = it }
        }
    }
}

@Composable
fun SwitchSettingsRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF7F92A2), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TelegramLightBlue)
        )
    }
}
