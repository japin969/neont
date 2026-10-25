package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.ChatRepository
import com.neont.messenger.data.FileDatabaseManager
import com.neont.messenger.data.model.Message
import com.neont.messenger.ui.components.ComposerBar
import com.neont.messenger.ui.components.MessageBubble
import com.neont.messenger.ui.theme.TelegramChatBG
import com.neont.messenger.ui.theme.TelegramHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    fileDb: FileDatabaseManager,
    chatRepo: ChatRepository,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var chatTitle by remember { mutableStateOf("Chat") }
    val messages by chatRepo.listenToMessages(chatId).collectAsState(initial = emptyList())

    LaunchedEffect(chatId) {
        val chats = fileDb.readChats()
        chatTitle = chats.find { it.chatId == chatId }?.title ?: "Chat"
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = chatTitle,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "online",
                            color = Color(0xFFD4E6F1),
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramHeader
                )
            )
        },
        containerColor = TelegramChatBG,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(TelegramChatBG),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.msgId }) { msg ->
                    val isOutgoing = msg.senderId == "me"
                    MessageBubble(
                        message = msg,
                        isOutgoing = isOutgoing
                    )
                }
            }

            ComposerBar(
                onSendMessage = { text ->
                    scope.launch {
                        chatRepo.sendMessage(chatId, "me", text)
                    }
                }
            )
        }
    }
}
