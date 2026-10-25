// Neont Messenger - SearchScreen.kt
// Layar Pencarian Global Telegram Android (Chats, Messages, Contacts)
package com.neont.messenger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.User
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkDivider
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.ui.components.TelegramAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    chats: List<Chat>,
    contacts: List<User>,
    onChatClick: (String) -> Unit,
    onContactClick: (User) -> Unit,
    onBackClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val matchedChats = remember(chats, query) {
        if (query.isBlank()) emptyList()
        else chats.filter { it.title.contains(query, ignoreCase = true) || it.lastMessage?.text?.contains(query, ignoreCase = true) == true }
    }

    val matchedContacts = remember(contacts, query) {
        if (query.isBlank()) emptyList()
        else contacts.filter { it.firstName.contains(query, ignoreCase = true) || it.username.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search chats, messages...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (matchedChats.isNotEmpty()) {
                item {
                    Text("CHATS", color = Color(0xFF5288C1), fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
                items(matchedChats, key = { it.chatId }) { chat ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onChatClick(chat.chatId) }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TelegramAvatar(name = chat.title, avatarUrl = chat.avatarUrl, size = 46)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(chat.title, color = Color.White, fontSize = 15.sp)
                            Text(chat.lastMessage?.text ?: "", color = Color(0xFF7F92A2), fontSize = 13.sp, maxLines = 1)
                        }
                    }
                    Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                }
            }

            if (matchedContacts.isNotEmpty()) {
                item {
                    Text("CONTACTS", color = Color(0xFF5288C1), fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
                items(matchedContacts, key = { it.uid }) { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onContactClick(user) }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TelegramAvatar(name = user.firstName, avatarUrl = user.avatarUrl, size = 46)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 15.sp)
                            Text("@${user.username}", color = Color(0xFF7F92A2), fontSize = 13.sp)
                        }
                    }
                    Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}
