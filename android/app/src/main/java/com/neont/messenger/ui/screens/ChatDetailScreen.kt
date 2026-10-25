// Neont Messenger - ChatDetailScreen.kt
// Pixel-Perfect Telegram Android Chat Room
package com.neont.messenger.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.ChatRepository
import com.neont.messenger.data.FileDatabaseManager
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.Message
import com.neont.messenger.ui.components.ComposerBar
import com.neont.messenger.ui.components.MessageBubble
import com.neont.messenger.ui.components.TelegramAvatar
import com.neont.messenger.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String? = null,
    chat: Chat? = null,
    messages: List<Message>? = null,
    currentUserId: String = "me",
    isContactOnline: Boolean = true,
    isContactTyping: Boolean = false,
    fileDb: FileDatabaseManager? = null,
    chatRepo: ChatRepository? = null,
    onBackClick: () -> Unit = {},
    onBackClicked: () -> Unit = onBackClick,
    onSendMessage: (text: String) -> Unit = {},
    onTyping: (isTyping: Boolean) -> Unit = {},
    onDeleteMessage: (msg: Message) -> Unit = {},
    onCallClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val actualChatId = chatId ?: chat?.chatId ?: ""
    var currentChat by remember { mutableStateOf(chat ?: Chat(chatId = actualChatId, title = "Chat")) }

    // Stream messages if chatRepo is provided and messages list not passed
    val repoMessages by remember(actualChatId, chatRepo) {
        chatRepo?.listenToMessages(actualChatId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val displayMessages = messages ?: repoMessages
    var messageLimit by rememberSaveable { mutableStateOf(30) }
    val pagedMessages = remember(displayMessages, messageLimit) {
        if (displayMessages.size > messageLimit) {
            displayMessages.takeLast(messageLimit)
        } else {
            displayMessages
        }
    }

    // derivedStateOf: Deteksi posisi scroll agar tidak auto-scroll ke bawah jika pengguna sedang membaca riwayat di atas
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf true
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
            lastVisible.index >= totalItems - 2
        }
    }

    LaunchedEffect(actualChatId, fileDb) {
        if (fileDb != null && chat == null) {
            val found = fileDb.readChats().find { it.chatId == actualChatId }
            if (found != null) {
                currentChat = found
            }
        }
    }

    LaunchedEffect(pagedMessages.size) {
        if (pagedMessages.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(pagedMessages.size - 1)
        }
    }

    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }

    if (showAttachSheet) {
        com.neont.messenger.ui.components.AttachBottomSheet(
            onDismiss = { showAttachSheet = false },
            onOptionSelected = { optionName ->
                showAttachSheet = false
                onSendMessage("[$optionName attached]")
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar: 36dp
                        TelegramAvatar(
                            name = currentChat.title,
                            avatarUrl = currentChat.avatarUrl,
                            size = 36,
                            isOnline = isContactOnline
                        )

                        Spacer(Modifier.width(10.dp))

                        Column {
                            // Nama: 17sp (FontWeight.Medium, Putih)
                            Text(
                                text = currentChat.title,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                maxLines = 1
                            )

                            // Status: "online" (biru #6AB2F2, 13sp) atau "last seen recently" (abu #8B9DAE, 13sp)
                            val statusText = if (isContactTyping) "typing..." else if (isContactOnline) "online" else "last seen recently"
                            val statusColor = if (isContactTyping || isContactOnline) TelegramCheckmark else TelegramTextSecondary

                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCallClick) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface // #17212B
                )
            )
        },
        containerColor = TelegramDarkBG, // Wallpaper: #0F1721
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Chat Stream (Background #0F1721) with chunking and animated bubbles
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(TelegramDarkBG), // #0F1721
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                if (displayMessages.size > messageLimit) {
                    item(key = "load_older_messages_btn") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { messageLimit += 30 },
                                colors = ButtonDefaults.textButtonColors(contentColor = TelegramPrimary)
                            ) {
                                Text(
                                    text = "Load earlier messages (${displayMessages.size - messageLimit} more)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                items(pagedMessages, key = { it.msgId }) { msg ->
                    val isOutgoing = msg.senderId == currentUserId || msg.senderId == "me"
                    MessageBubble(
                        message = msg,
                        isOutgoing = isOutgoing,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            // Composer Bar (Kolom Ketik)
            ComposerBar(
                onSendMessage = { text ->
                    if (onSendMessage != {}) {
                        onSendMessage(text)
                    } else if (chatRepo != null && actualChatId.isNotBlank()) {
                        scope.launch {
                            chatRepo.sendMessage(actualChatId, currentUserId, text)
                        }
                    }
                },
                onTyping = onTyping,
                onAttachClick = { showAttachSheet = true },
                onEmojiClick = { showEmojiKeyboard = !showEmojiKeyboard }
            )

            // Emoji Keyboard drawer
            if (showEmojiKeyboard) {
                com.neont.messenger.ui.components.EmojiKeyboard(
                    onEmojiSelected = { emoji ->
                        onSendMessage(emoji)
                    },
                    onBackspaceClick = {}
                )
            }
        }
    }
}
