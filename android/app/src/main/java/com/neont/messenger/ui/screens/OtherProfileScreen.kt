// Neont Messenger - OtherProfileScreen.kt
// Layar Profil Pengguna Lain / Kontak dengan tombol Chat, Voice Call, Video Call
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.User
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkDivider
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.TelegramAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onSendMessageClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text(user.firstName, color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onVoiceCallClick) { Icon(Icons.Default.Call, "Call", tint = Color.White) }
                    IconButton(onClick = onVideoCallClick) { Icon(Icons.Default.Videocam, "Video", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkHeader)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TelegramAvatar(name = "${user.firstName} ${user.lastName}", avatarUrl = user.avatarUrl, size = 96, isOnline = user.isOnline)
                Spacer(Modifier.height(14.dp))
                Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(if (user.isOnline) "online" else "last seen recently", color = if (user.isOnline) TelegramLightBlue else Color(0xFF7F92A2), fontSize = 14.sp)

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onSendMessageClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramLightBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send Message")
                    }

                    Button(
                        onClick = onVoiceCallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242F3D)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Info", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

                InfoBlock(label = user.phoneNumber.ifBlank { "Hidden" }, sublabel = "Mobile")
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp)

                InfoBlock(label = "@${user.username}", sublabel = "Username")
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp)

                InfoBlock(label = user.bio.ifBlank { "Using Neont Messenger" }, sublabel = "Bio")
            }
        }
    }
}
