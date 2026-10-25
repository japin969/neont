// Neont Messenger - VoiceCallScreen.kt
// Pixel-Perfect Telegram Android Voice Call Interface
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.ui.components.TelegramAvatar
import com.neont.messenger.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VoiceCallScreen(
    contactName: String,
    onEndCall: (durationSeconds: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var callDuration by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isVideoRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDuration++
        }
    }

    val timeString = remember(callDuration) {
        val min = callDuration / 60
        val sec = callDuration % 60
        String.format("%02d:%02d", min, sec)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1721),
                        Color(0xFF17212B),
                        Color(0xFF0B1015)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // Top Section: Avatar 120dp, Name 24sp, Timer 16sp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar: 120dp di tengah atas
            TelegramAvatar(
                name = contactName,
                size = 120
            )

            Spacer(Modifier.height(20.dp))

            // Nama: 24sp putih
            Text(
                text = contactName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Status: Timer (00:23) 16sp warna abu (#8B9DAE)
            Text(
                text = if (callDuration == 0) "calling..." else timeString,
                color = TelegramTextSecondary, // #8B9DAE
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    tint = TelegramCheckmark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "End-to-end encrypted",
                    color = TelegramTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom Section: Tombol bawah Mute, Speaker, Video, Bluetooth, End Call
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallRoundButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = { isMuted = !isMuted }
                )

                CallRoundButton(
                    icon = if (isVideoRequested) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    label = "Video",
                    isActive = isVideoRequested,
                    onClick = { isVideoRequested = !isVideoRequested }
                )

                CallRoundButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = "Speaker",
                    isActive = isSpeakerOn,
                    onClick = { isSpeakerOn = !isSpeakerOn }
                )

                CallRoundButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    isActive = isBluetoothOn,
                    onClick = { isBluetoothOn = !isBluetoothOn }
                )
            }

            Spacer(Modifier.height(36.dp))

            // Tombol End Call: Lingkaran merah menyala #E53935
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TelegramRed) // #E53935
                    .clickable { onEndCall(callDuration) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun CallRoundButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else TelegramSurface)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = TelegramTextSecondary,
            fontSize = 12.sp
        )
    }
}
