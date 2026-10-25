package com.neont.messenger.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.neont.messenger.ui.theme.TelegramDarkBG
import com.neont.messenger.ui.theme.TelegramPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isUserLoggedIn: Boolean? = null,
    onNavigateNext: (destination: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animasi fade in & pop scale
        alpha.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = tween(600))

        delay(900) // Total waktu splash ~1.5 detik

        val loggedIn = isUserLoggedIn ?: (FirebaseAuth.getInstance().currentUser != null)
        if (loggedIn) {
            onNavigateNext("main")
        } else {
            onNavigateNext("auth")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBG),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(TelegramPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Neont Logo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 4.dp, top = 2.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Neont",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Fast. Secure. Powerful.",
                color = Color(0xFF7F92A2),
                fontSize = 14.sp
            )
        }
    }
}
