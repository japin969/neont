// Neont Messenger - ComposerBar.kt
// Message Input Bar Telegram Android: Scale + Rotate Mic/Send (200ms), Emoji/Attach toggles, Typing debounced
package com.neont.messenger.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramDarkSurface
import com.neont.messenger.theme.TelegramLightBlue
import kotlinx.coroutines.delay

@Composable
fun ComposerBar(
    onSendMessage: (String) -> Unit,
    onSendVoice: () -> Unit = {},
    onAttachClick: () -> Unit = {},
    onEmojiToggle: () -> Unit = {},
    onTyping: (Boolean) -> Unit = {},
    replyMessageSnippet: String? = null,
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        if (text.isNotBlank()) {
            onTyping(true)
            delay(2000)
            onTyping(false)
        } else {
            onTyping(false)
        }
    }

    Column(modifier = modifier.fillMaxWidth().background(TelegramDarkSurface)) {
        // Baris Reply Header jika sedang me-reply pesan
        if (replyMessageSnippet != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkHeader)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(3.dp).height(32.dp).background(TelegramLightBlue))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Reply to message", color = TelegramLightBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(replyMessageSnippet, color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Baris Input Utama
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TelegramDarkHeader)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEmojiToggle, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Mood, "Emoji", tint = Color(0xFF8B9DAE), modifier = Modifier.size(24.dp))
                }

                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                    if (text.isEmpty()) {
                        Text("Message", color = Color(0xFF7F92A2), fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(TelegramLightBlue),
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = onAttachClick, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.AttachFile, "Attach", tint = Color(0xFF8B9DAE), modifier = Modifier.size(22.dp).rotate(-45f))
                }
            }

            Spacer(Modifier.width(8.dp))

            // Animasi Transisi Mic <-> Send (Scale + Rotate 200ms)
            AnimatedContent(
                targetState = text.isNotBlank(),
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.7f, animationSpec = tween(200))) togetherWith
                            (fadeOut(tween(200)) + scaleOut(targetScale = 0.7f, animationSpec = tween(200)))
                },
                label = "mic_send_anim"
            ) { hasText ->
                if (hasText) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TelegramLightBlue)
                            .clickable {
                                val msg = text.trim()
                                text = ""
                                onSendMessage(msg)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TelegramDarkHeader)
                            .clickable { onSendVoice() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, "Record", tint = Color(0xFF8B9DAE), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
