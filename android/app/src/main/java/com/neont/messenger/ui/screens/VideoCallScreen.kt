// Neont Messenger - VideoCallScreen.kt
// Layar Video Call Telegram Android: Video Preview, Flip Camera, Mute, dan Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.ui.components.TelegramAvatar
import kotlinx.coroutines.delay

@Composable
fun VideoCallScreen(
    contactName: String,
    onEndCall: (durationSec: Int) -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var isVideoPaused by remember { mutableStateOf(false) }

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
            .background(Color.Black)
    ) {
        // Layar Video Utama (Remote contact simulation)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TelegramAvatar(name = contactName, size = 96)
                Spacer(Modifier.height(16.dp))
                Text(contactName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(formattedTime, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        // Floating PiP Camera Sendiri di pojok kanan atas
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .width(100.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF242F3D)),
            contentAlignment = Alignment.Center
        ) {
            if (isVideoPaused) {
                Text("Camera Off", color = Color.Gray, fontSize = 11.sp)
            } else {
                Text("You", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Action Toolbar di Bawah
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flip Camera
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242F3D))
                    .clickable { isFrontCamera = !isFrontCamera },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Cameraswitch, "Flip Camera", tint = Color.White)
            }

            // Video Toggle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isVideoPaused) Color.White else Color(0xFF242F3D))
                    .clickable { isVideoPaused = !isVideoPaused },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideoPaused) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = "Video Mute",
                    tint = if (isVideoPaused) Color.Black else Color.White
                )
            }

            // Mic Toggle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else Color(0xFF242F3D))
                    .clickable { isMuted = !isMuted },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (isMuted) Color.Black else Color.White
                )
            }

            // End Call (Merah)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onEndCall(callSeconds) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}
