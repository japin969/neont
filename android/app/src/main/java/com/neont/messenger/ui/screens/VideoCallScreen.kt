// Neont Messenger - VideoCallScreen.kt
// Pixel-Perfect Telegram Android Video Call Screen with Floating PiP
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.ui.components.TelegramAvatar
import com.neont.messenger.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VideoCallScreen(
    contactName: String,
    onEndCall: (durationSec: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var isVideoPaused by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }

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
    ) {
        // Layar Video Utama (Remote Contact View)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar 120dp
            TelegramAvatar(name = contactName, size = 120)

            Spacer(Modifier.height(18.dp))

            // Nama 24sp Putih
            Text(
                text = contactName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Status Timer 16sp warna abu (#8B9DAE)
            Text(
                text = if (callSeconds == 0) "connecting video..." else formattedTime,
                color = TelegramTextSecondary, // #8B9DAE
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Floating PiP Camera Sendiri di sudut kanan atas (Corner Radius 12dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 16.dp)
                .width(110.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TelegramInputBG), // #242F3D
            contentAlignment = Alignment.Center
        ) {
            if (isVideoPaused) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideocamOff, contentDescription = null, tint = TelegramTextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Camera Off", color = TelegramTextSecondary, fontSize = 11.sp)
                }
            } else {
                TelegramAvatar(name = "You", size = 56)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("You", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Action Toolbar di Bawah
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flip Camera
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TelegramSurface)
                    .clickable { isFrontCamera = !isFrontCamera },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Cameraswitch, "Flip Camera", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // Video On/Off Toggle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isVideoPaused) Color.White else TelegramSurface)
                    .clickable { isVideoPaused = !isVideoPaused },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideoPaused) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = "Video Toggle",
                    tint = if (isVideoPaused) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Mute Mic Toggle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else TelegramSurface)
                    .clickable { isMuted = !isMuted },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic Toggle",
                    tint = if (isMuted) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Speaker Toggle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isSpeakerOn) TelegramPrimary else TelegramSurface)
                    .clickable { isSpeakerOn = !isSpeakerOn },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    contentDescription = "Speaker",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // End Call: Lingkaran Merah #E53935
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TelegramRed) // #E53935
                    .clickable { onEndCall(callSeconds) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
