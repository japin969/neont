package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.Chat
import app.neon.com.data.model.User
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    chats: List<Chat>? = null,
    contacts: List<User>? = null,
    fileDb: FileDatabaseManager? = null,
    onChatClick: (String) -> Unit = {},
    onContactClick: (User) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var localChats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var localContacts by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(fileDb) {
        if (chats == null && fileDb != null) {
            localChats = fileDb.readChats()
        }
        if (contacts == null && fileDb != null) {
            localContacts = fileDb.readUsers()
        }
    }

    val activeChats = chats ?: localChats
    val activeContacts = contacts ?: localContacts

    val filteredChats = remember(searchQuery, activeChats) {
        if (searchQuery.isBlank()) emptyList()
        else activeChats.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val filteredContacts = remember(searchQuery, activeContacts) {
        if (searchQuery.isBlank()) emptyList()
        else activeContacts.filter {
            it.firstName.contains(searchQuery, ignoreCase = true) ||
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
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search", color = TelegramTextSecondary) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
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
            if (filteredChats.isNotEmpty()) {
                item {
                    Text(
                        text = "CHATS",
                        color = TelegramPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp, 8.dp)
                    )
                }
                items(filteredChats, key = { "chat_${it.chatId}" }) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatClick(chat.chatId) }
                            .padding(16.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TelegramPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(chat.title.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(chat.title, color = Color.White, fontSize = 16.sp)
                    }
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                }
            }

            if (filteredContacts.isNotEmpty()) {
                item {
                    Text(
                        text = "CONTACTS",
                        color = TelegramPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp, 8.dp)
                    )
                }
                items(filteredContacts, key = { "contact_${it.uid}" }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactClick(user) }
                            .padding(16.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TelegramPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.firstName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 16.sp)
                            Text("@${user.username}", color = TelegramTextSecondary, fontSize = 13.sp)
                        }
                    }
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}
