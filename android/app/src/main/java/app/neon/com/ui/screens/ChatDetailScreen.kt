package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.ChatRepository
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.*
import app.neon.com.ui.components.*
import app.neon.com.ui.theme.*
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
    onAttachClick: (String) -> Unit = {},
    onHeaderClick: () -> Unit = {},
    onMicClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) } // State dikontrol di sini

    val actualChatId = chatId ?: chat?.chatId ?: ""
    val displayMessages = messages ?: emptyList()

    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayMessages.size - 1)
        }
    }

    if (showAttachSheet) {
        AttachBottomSheet(
            onDismiss = { showAttachSheet = false },
            onOptionSelected = { optionName ->
                showAttachSheet = false
                onAttachClick(optionName)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onHeaderClick() }
                    ) {
                        TelegramAvatar(name = chat?.title ?: "Chat", size = 40, isOnline = isContactOnline)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(chat?.title ?: "Chat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (isContactTyping) {
                                Text("typing...", color = TelegramPrimary, fontSize = 13.sp)
                            } else if (isContactOnline) {
                                Text("online", color = TelegramPrimary, fontSize = 13.sp)
                            } else {
                                Text("last seen recently", color = TelegramTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onCallClick) {
                        Icon(Icons.Default.Phone, "Call", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO: Menu Chat */ }) {
                        Icon(Icons.Default.MoreVert, "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(displayMessages, key = { it.msgId }) { msg ->
                    MessageBubble(
                        message = msg,
                        isOutgoing = msg.senderId == currentUserId,
                        onLongClick = { 
                            selectedMessage = it
                            showMessageDialog = true 
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }


            if (showMessageDialog && selectedMessage != null) {
                AlertDialog(
                    onDismissRequest = { showMessageDialog = false },
                    title = { Text("Pilih Aksi", color = Color.White) },
                    containerColor = app.neon.com.ui.theme.TelegramSurface,
                    text = {
                        Column {
                            TextButton(onClick = { 
                                showMessageDialog = false
                                onDeleteMessage(selectedMessage!!) 
                            }) { Text("Hapus Pesan", color = Color.Red) }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMessageDialog = false }) {
                            Text("Batal", color = Color.White)
                        }
                    }
                )
            }
            ComposerBar(
                text = textInput,
                onTextChange = { textInput = it },
                onSendMessage = { text ->
                    if (text.isNotBlank()) {
                        if (onSendMessage != {}) {
                            onSendMessage(text)
                        } else if (chatRepo != null && actualChatId.isNotBlank()) {
                            scope.launch {
                                chatRepo.sendMessage(actualChatId, currentUserId, text)
                            }
                        }
                        textInput = "" // Reset setelah kirim
                        showEmojiKeyboard = false
                    }
                },
                onTyping = onTyping,
                onAttachClick = { showAttachSheet = true },
                onMicClick = onMicClick,
                onEmojiClick = { showEmojiKeyboard = !showEmojiKeyboard }
            )

            if (showEmojiKeyboard) {
                EmojiKeyboard(
                    onEmojiSelected = { emoji ->
                        textInput += emoji // Tambahin ke input, gak langsung kirim
                    },
                    onBackspaceClick = {
                        if (textInput.isNotEmpty()) textInput = textInput.dropLast(1)
                    }
                )
            }
        }
    }
}
