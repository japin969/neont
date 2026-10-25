// Neont Messenger - MessageBubble.kt
// Pixel-perfect bubble chat Telegram Android: Entry animation, Swipe-to-Reply, Long-press context menu, Voice, Image
package com.neont.messenger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neont.messenger.data.model.Message
import com.neont.messenger.data.model.MessageStatus
import com.neont.messenger.data.model.MessageType
import com.neont.messenger.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    onReply: (Message) -> Unit = {},
    onDelete: (Message) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showContextMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Bubble entry animation: scale 0.95 -> 1.0 + fade 150ms
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(message.msgId) {
        visible = true
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isOutgoing) BubbleOutgoingDark else BubbleIncomingDark
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.95f, animationSpec = tween(150)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (delta < 0 || offsetX > 0) {
                            val newOffset = (offsetX + delta).coerceIn(-120f, 0f)
                            offsetX = newOffset
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX <= -70f) {
                            onReply(message)
                        }
                        offsetX = 0f
                    }
                ),
            horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .widthIn(min = 72.dp, max = 320.dp)
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showContextMenu = true }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column {
                        // Reply header preview jika me-reply pesan
                        if (message.replyToMsgId != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(28.dp)
                                        .background(TelegramLightBlue)
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("Reply", color = TelegramLightBlue, fontSize = 11.sp)
                                    Text("Original message preview", color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }

                        when (message.type) {
                            MessageType.TEXT -> {
                                Text(message.text, color = Color.White, fontSize = 15.sp, lineHeight = 19.sp)
                            }
                            MessageType.IMAGE -> {
                                AsyncImage(
                                    model = message.mediaUrl ?: "",
                                    contentDescription = "Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                )
                            }
                            MessageType.VOICE -> {
                                VoiceNoteBubbleContent(message = message, isOutgoing = isOutgoing)
                            }
                            else -> Text(message.text, color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(timeFormatted, color = Color(0x99FFFFFF), fontSize = 11.sp)
                            if (isOutgoing) {
                                when (message.status) {
                                    MessageStatus.PENDING -> Icon(Icons.Default.Schedule, "Pending", tint = Color(0x99FFFFFF), modifier = Modifier.size(12.dp))
                                    MessageStatus.SENT -> Icon(Icons.Default.Check, "Sent", tint = CheckmarkBlue, modifier = Modifier.size(12.dp))
                                    MessageStatus.DELIVERED, MessageStatus.READ -> Icon(Icons.Default.DoneAll, "Read", tint = CheckmarkBlue, modifier = Modifier.size(14.dp))
                                    MessageStatus.FAILED -> Text("!", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Long Press Context Menu
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.background(TelegramDarkHeader)
                ) {
                    DropdownMenuItem(
                        text = { Text("Reply", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Reply, null, tint = TelegramLightBlue) },
                        onClick = {
                            showContextMenu = false
                            onReply(message)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy Text", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color.LightGray) },
                        onClick = {
                            showContextMenu = false
                            clipboardManager.setText(AnnotatedString(message.text))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Forward", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Forward, null, tint = Color.LightGray) },
                        onClick = { showContextMenu = false }
                    )
                    if (isOutgoing) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                            onClick = {
                                showContextMenu = false
                                onDelete(message)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceNoteBubbleContent(message: Message, isOutgoing: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }
    val waveform = remember { message.waveform ?: listOf(35, 60, 90, 45, 75, 50, 85, 40, 65, 80, 55, 30, 70, 45) }

    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) TelegramLightBlue else Color(0xFF405364))
                .combinedClickable(onClick = { isPlaying = !isPlaying }, onLongClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Row(modifier = Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                waveform.forEach { bar ->
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((bar * 0.24).dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isPlaying) Color.White else Color(0x80FFFFFF))
                    )
                }
            }
            Text("0:${message.mediaDurationSec ?: 14}", color = Color(0x99FFFFFF), fontSize = 11.sp)
        }
    }
}
