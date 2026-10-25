// Neont Messenger - ProfileScreen.kt
// Layar Profil Pengguna Sendiri
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
fun ProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
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
                TelegramAvatar(name = "${user.firstName} ${user.lastName}", avatarUrl = user.avatarUrl, size = 96)
                Spacer(Modifier.height(14.dp))
                Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("online", color = TelegramLightBlue, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Account", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))

                InfoBlock(label = user.phoneNumber.ifBlank { "+1 415 555 0199" }, sublabel = "Tap to change phone number")
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp)

                InfoBlock(label = "@${user.username.ifBlank { "username" }}", sublabel = "Username")
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp)

                InfoBlock(label = user.bio.ifBlank { "Hey there! I am using Neont." }, sublabel = "Bio")
            }
        }
    }
}

@Composable
fun InfoBlock(label: String, sublabel: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(sublabel, color = Color(0xFF7F92A2), fontSize = 13.sp)
    }
}
