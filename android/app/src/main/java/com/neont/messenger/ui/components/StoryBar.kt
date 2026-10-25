// Neont Messenger - StoryBar
// Deretan Stories Telegram Android di atas Chat List dengan ring gradien biru
package com.neont.messenger.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.Story
import com.neont.messenger.theme.TelegramLightBlue

@Composable
fun StoryBar(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item: Add My Story
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onAddStoryClick() }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    TelegramAvatar(
                        name = "My Story",
                        size = 54
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF17212B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(0.dp, Color.Transparent, CircleShape)
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add story",
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.dp, TelegramLightBlue, CircleShape)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "My Story",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Daftar Stories Kontak
        items(stories, key = { it.storyId }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onStoryClick(story) }
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (story.isViewed) Color.DarkGray else TelegramLightBlue,
                            shape = CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TelegramAvatar(
                        name = story.userName,
                        avatarUrl = story.userAvatar,
                        size = 52
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = story.userName.split(" ").firstOrNull() ?: story.userName,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
