// Neont Messenger - SplashScreen
// Cold boot splash screen dengan auto-routing ke Auth atau Main
package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkBackground
import com.neont.messenger.theme.TelegramLightBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isUserLoggedIn: Boolean,
    onNavigateNext: (destination: String) -> Unit
) {
    LaunchedEffect(Unit) {
        delay(600) // Transisi singkat
        if (isUserLoggedIn) {
            onNavigateNext("main")
        } else {
            onNavigateNext("auth")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(TelegramLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .offset(x = (-2).dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Neont",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Fast. Secure. Offline-First.",
                color = Color(0xFF7F92A2),
                fontSize = 14.sp
            )
        }
    }
}
