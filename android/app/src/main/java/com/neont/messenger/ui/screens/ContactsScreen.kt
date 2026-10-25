// Neont Messenger - ContactsScreen.kt
// Layar Kontak Telegram Android membaca data dari users.json
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ContactsScreen(
    contacts: List<User>,
    onContactClick: (User) -> Unit,
    onBackClick: () -> Unit = {},
    onAddContactClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.firstName.contains(searchQuery, ignoreCase = true) ||
                    it.lastName.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Contacts", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onAddContactClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(filteredContacts, key = { it.uid }) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactClick(user) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelegramAvatar(
                        name = "${user.firstName} ${user.lastName}",
                        avatarUrl = user.avatarUrl,
                        size = 48,
                        isOnline = user.isOnline
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user.firstName} ${user.lastName}".trim(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (user.isOnline) "online" else "last seen recently",
                            color = if (user.isOnline) TelegramLightBlue else Color(0xFF7F92A2),
                            fontSize = 13.sp
                        )
                    }
                }
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 78.dp))
            }
        }
    }
}
