// Neont Messenger - ChatDetailScreen.kt
// Layar Chat Detail Telegram Android: Realtime Firebase, Auto-scroll, Swipe-to-Reply, Emoji Keyboard, Attach Sheet, Long-press Menu, & Pulsing Typing Dots
package com.neont.messenger.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.Message
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramLightBlue
import com.neont.messenger.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    currentUserId: String = "me",
    isContactOnline: Boolean = true,
    isContactTyping: Boolean = false,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoice: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onTyping: (Boolean) -> Unit = {},
    onDeleteMessage: (Message) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }
    var replyingMessage by remember { mutableStateOf<Message?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TelegramAvatar(name = chat.title, avatarUrl = chat.avatarUrl, size = 40, isOnline = isContactOnline)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(chat.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (isContactTyping) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("typing", color = TelegramLightBlue, fontSize = 12.sp)
                                    Spacer(Modifier.width(4.dp))
                                    PulsingTypingDots()
                                }
                            } else {
                                Text(if (isContactOnline) "online" else "last seen recently", color = if (isContactOnline) TelegramLightBlue else Color(0xFF7F92A2), fontSize = 12.sp)
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCallClick) { Icon(Icons.Default.Call, "Call", tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0E1621))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x66000000)).padding(horizontal = 12.dp, vertical = 3.dp)) {
                                Text("Today", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    items(messages, key = { it.msgId }) { message ->
                        val isOutgoing = message.senderId == currentUserId
                        MessageBubble(
                            message = message,
                            isOutgoing = isOutgoing,
                            onReply = { replyingMessage = it },
                            onDelete = { onDeleteMessage(it) }
                        )
                    }
                }
            }

            // Composer Input Bar
            ComposerBar(
                onSendMessage = { text ->
                    onSendMessage(text)
                    replyingMessage = null
                    scope.launch {
                        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                    }
                },
                onSendVoice = onSendVoice,
                onAttachClick = { showAttachSheet = true },
                onEmojiToggle = { showEmojiKeyboard = !showEmojiKeyboard },
                onTyping = onTyping,
                replyMessageSnippet = replyingMessage?.text,
                onCancelReply = { replyingMessage = null }
            )

            // Panel Emoji Keyboard
            if (showEmojiKeyboard) {
                EmojiKeyboard(
                    onEmojiSelected = { emoji ->
                        onSendMessage(emoji)
                    },
                    onBackspaceClick = {}
                )
            }
        }
    }

    // Modal Attach Bottom Sheet
    if (showAttachSheet) {
        AttachBottomSheet(
            onDismiss = { showAttachSheet = false },
            onOptionSelected = { type ->
                showAttachSheet = false
                onSendMessage("Shared $type")
            }
        )
    }
}

/**
 * 3 Titik Berdenyut (Pulsing Typing Dots) khas Telegram Android
 */
@Composable
fun PulsingTypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_anim")

    @Composable
    fun animateDotScale(delayMillis: Int): State<Float> {
        return infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_scale"
        )
    }

    val dot1 by animateDotScale(0)
    val dot2 by animateDotScale(150)
    val dot3 by animateDotScale(300)

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(4.dp).scale(dot1).clip(CircleShape).background(TelegramLightBlue))
        Box(modifier = Modifier.size(4.dp).scale(dot2).clip(CircleShape).background(TelegramLightBlue))
        Box(modifier = Modifier.size(4.dp).scale(dot3).clip(CircleShape).background(TelegramLightBlue))
    }
}
