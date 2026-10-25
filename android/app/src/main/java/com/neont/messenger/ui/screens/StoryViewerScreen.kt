// Neont Messenger - StoryViewerScreen.kt
// Pixel-Perfect Telegram Android Story Viewer with Segmented Progress Bar & Tap Gestures
package com.neont.messenger.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neont.messenger.data.model.Story
import com.neont.messenger.ui.components.TelegramAvatar
import com.neont.messenger.ui.theme.*

@Composable
fun StoryViewerScreen(
    story: Story,
    onStoryFinished: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    var replyText by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(story.storyId, isPaused) {
        if (!isPaused) {
            val remaining = (1f - progress.value) * 5000f
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = remaining.toInt(), easing = LinearEasing)
            )
            onStoryFinished()
        } else {
            progress.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        // Tap left 30% -> restart or finish, tap right 70% -> next
                        if (offset.x < size.width * 0.3f) {
                            // Back / Restart
                            onCloseClick()
                        } else {
                            onStoryFinished()
                        }
                    }
                )
            }
    ) {
        // Konten Story Utama (Gambar Foto atau Gradient Card)
        if (story.mediaUrl.isNotBlank()) {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = story.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1721)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TelegramAvatar(name = story.userName, size = 110)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = story.caption.ifEmpty { "My Story" },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Header Atas: Segmented Progress Bar (5 detik) + User Info + Tombol Close
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 24.dp, start = 12.dp, end = 12.dp)
        ) {
            // Segmented Progress Bar 5 Detik
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.value)
                        .background(Color.White)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TelegramAvatar(
                        name = story.userName,
                        avatarUrl = story.mediaUrl,
                        size = 38
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = story.userName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "today at 14:20",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bottom Caption & Quick Reply Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            if (story.caption.isNotBlank()) {
                Text(
                    text = story.caption,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Send a message...", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            replyText = ""
                            onStoryFinished()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TelegramPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Reply", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
