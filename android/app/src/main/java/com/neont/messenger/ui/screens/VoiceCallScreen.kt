// Neont Messenger - VoiceCallScreen.kt
// Layar Panggilan Suara Telegram: Timer berjalan, Mute/Speaker toggle, dan End Call
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.ui.components.TelegramAvatar
import kotlinx.coroutines.delay

@Composable
fun VoiceCallScreen(
    contactName: String,
    contactAvatarUrl: String = "",
    onEndCall: (durationSec: Int) -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isBluetoothOn by remember { mutableStateOf(false) }

    // Timer berjalan
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val formattedTime = remember(callSeconds) {
        val min = callSeconds / 60
        val sec = callSeconds % 60
        String.format("%02d:%02d", min, sec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(24.dp)
    ) {
        // Bagian Atas: Kontak & Timer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TelegramAvatar(
                name = contactName,
                avatarUrl = contactAvatarUrl,
                size = 110
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = contactName,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = formattedTime,
                color = Color(0xFF4FA0FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "End-to-end encrypted",
                color = Color(0xFF7F92A2),
                fontSize = 12.sp
            )
        }

        // Bagian Bawah: Tombol Kontrol Panggilan
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) "Unmute" else "Mute",
                    isActive = isMuted,
                    onClick = { isMuted = !isMuted }
                )

                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = "Speaker",
                    isActive = isSpeakerOn,
                    onClick = { isSpeakerOn = !isSpeakerOn }
                )

                CallControlButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    isActive = isBluetoothOn,
                    onClick = { isBluetoothOn = !isBluetoothOn }
                )
            }

            Spacer(Modifier.height(36.dp))

            // Tombol Akhiri Panggilan (Merah)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onEndCall(callSeconds) },
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
private fun CallControlButton(
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
                .background(if (isActive) Color.White else Color(0xFF242F3D))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}
