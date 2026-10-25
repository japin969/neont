// Neont Messenger - PrivacySettingsScreen.kt
// Layar Privasi dan Keamanan Telegram Android
package com.neont.messenger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Privacy and Security", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Privacy", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp))

            SettingsItem(icon = Icons.Default.Block, title = "Blocked Users", subtitle = "0 users")
            SettingsItem(icon = Icons.Default.Security, title = "Phone Number", subtitle = "My Contacts")
            SettingsItem(icon = Icons.Default.Security, title = "Last Seen & Online", subtitle = "Everybody")
            SettingsItem(icon = Icons.Default.Security, title = "Profile Photos", subtitle = "Everybody")

            Divider(color = Color(0xFF101419), thickness = 8.dp)

            Text("Security", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp))

            SettingsItem(icon = Icons.Default.Lock, title = "Passcode Lock", subtitle = "Disabled")
            SettingsItem(icon = Icons.Default.Lock, title = "Two-Step Verification", subtitle = "Enabled")
        }
    }
}
