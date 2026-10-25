// Neont Messenger - MessageBubble.kt
// Pixel-Perfect Reverse Engineering of Telegram Android Chat Bubbles
// Mendukung Text Bubble & Animated VoiceNoteBubble (Running Waveform via rememberInfiniteTransition)
package app.neon.com.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.Message
import app.neon.com.data.model.MessageStatus
import app.neon.com.data.model.MessageType
import app.neon.com.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    // Sudut (Corner Radius khas Telegram):
    // Outgoing: 18dp di kiri-atas, 18dp di kanan-atas, 18dp di kiri-bawah, dan 4dp di kanan-bawah (ekor)
    // Incoming: 18dp di kanan-atas, 18dp di kiri-atas, 18dp di kanan-bawah, dan 4dp di kiri-bawah (ekor)
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        )
    }

    // Warna Bubble: Outgoing #2B5278, Incoming #182533
    val bubbleBg = if (isOutgoing) TelegramBubbleOut else TelegramBubbleIn

    // Warna Timestamp transparan: Outgoing #7D9FBA, Incoming #6C7883
    val timeColor = if (isOutgoing) TelegramTimeOut else TelegramTimeIn

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp)
        ) {
            if (message.type == MessageType.VOICE) {
                // Tampilan Voice Note Khas Telegram dengan Animated Waveform
                VoiceNoteBubbleContent(
                    message = message,
                    isOutgoing = isOutgoing,
                    timeFormatted = timeFormatted,
                    timeColor = timeColor
                )
            } else {
                // Tampilan Text Message Biasa
                TextMessageContent(
                    message = message,
                    isOutgoing = isOutgoing,
                    timeFormatted = timeFormatted,
                    timeColor = timeColor
                )
            }
        }
    }
}

@Composable
private fun TextMessageContent(
    message: Message,
    isOutgoing: Boolean,
    timeFormatted: String,
    timeColor: Color
) {
    Column {
        Text(
            text = message.text,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // Timestamp di pojok kanan bawah, tidak menabrak teks
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = timeFormatted,
                color = timeColor,
                fontSize = 11.sp
            )

            if (isOutgoing) {
                MessageStatusIcon(status = message.status)
            }
        }
    }
}

@Composable
private fun VoiceNoteBubbleContent(
    message: Message,
    isOutgoing: Boolean,
    timeFormatted: String,
    timeColor: Color
) {
    var isPlaying by remember { mutableStateOf(false) }

    // Infinite animation untuk simulasi running waveform saat voice note diputar
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phaseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(modifier = Modifier.width(220.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Play / Pause Circular Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TelegramPrimary)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Waveform baris berjalan (24 batang vertikal)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barCount = 20
                for (i in 0 until barCount) {
                    val baseHeight = 6f + ((i * 7) % 18).toFloat()
                    val animatedHeight = if (isPlaying) {
                        val factor = kotlin.math.sin(phaseAnim + (i * 0.45f)).toFloat()
                        (baseHeight + (factor * 6f)).coerceIn(4f, 24f)
                    } else {
                        baseHeight
                    }

                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying && i < 10) TelegramPrimary else Color(0xFF6B87A0))
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Info durasi & timestamp di kanan bawah
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val durationSec = message.mediaDurationSec ?: 14
            val durationText = "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
            Text(
                text = durationText,
                color = timeColor,
                fontSize = 12.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = timeFormatted,
                    color = timeColor,
                    fontSize = 11.sp
                )
                if (isOutgoing) {
                    MessageStatusIcon(status = message.status)
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.PENDING -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Pending",
                tint = TelegramTimeOut,
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = TelegramTimeOut,
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.DELIVERED, MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = TelegramCheckmark, // #6AB2F2 (biru Telegram)
                modifier = Modifier.size(15.dp)
            )
        }
        MessageStatus.FAILED -> {
            Text("!", color = TelegramRed, fontSize = 11.sp)
        }
    }
}
