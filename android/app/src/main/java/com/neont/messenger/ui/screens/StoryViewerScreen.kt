// Neont Messenger - StoryViewerScreen.kt
// Layar Story Viewer Fullscreen dengan progress bar beranimasi
package com.neont.messenger.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun StoryViewerScreen(
    story: Story,
    onStoryFinished: () -> Unit,
    onCloseClick: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(story.storyId, isPaused) {
        if (!isPaused) {
            val remainingRatio = 1f - progress.value
            val remainingDuration = (4000 * remainingRatio).toInt()
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = remainingDuration, easing = LinearEasing)
            )
            onStoryFinished()
        }
    }

    Box(
        modifier = Modifier
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
                        if (offset.x < size.width / 3) {
                            // Tap kiri
                        } else {
                            onStoryFinished()
                        }
                    }
                )
            }
    ) {
        // Gambar Story
        AsyncImage(
            model = story.mediaUrl,
            contentDescription = "Story media",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Header & Progress Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // Baris Progress Bar
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(12.dp))

            // Author info & Tombol Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TelegramAvatar(name = story.userName, avatarUrl = story.userAvatar, size = 38)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(story.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("2h ago", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }

                IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // Caption di Bawah
        if (story.caption.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Text(story.caption, color = Color.White, fontSize = 15.sp)
            }
        }
    }
}
