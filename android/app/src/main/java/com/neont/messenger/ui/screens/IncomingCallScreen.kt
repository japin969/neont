// Neont Messenger - IncomingCallScreen.kt
// Layar Panggilan Masuk Telegram: Animasi Ripple/Pulse, Tombol Accept (Hijau) & Reject (Merah)
package com.neont.messenger.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.ui.components.TelegramAvatar

@Composable
fun IncomingCallScreen(
    callerName: String,
    callerAvatarUrl: String = "",
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit
) {
    // Animasi denyut pulsing untuk avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.scale(scaleAnim),
                contentAlignment = Alignment.Center
            ) {
                TelegramAvatar(
                    name = callerName,
                    avatarUrl = callerAvatarUrl,
                    size = 120
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = callerName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Incoming Neont Call...",
                color = Color(0xFF4FA0FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Tombol Terima & Tolak di Bawah
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tolak (Merah)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onRejectCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Decline", color = Color.White, fontSize = 13.sp)
            }

            // Terima (Hijau)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF43A047))
                        .clickable { onAcceptCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Accept", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
