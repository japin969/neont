// Neont Messenger - ChatListScreen.kt
// Tampilan Daftar Chat Telegram Android: Folder Tabs, StoryBar, LazyColumn, Telegram FAB, & Swipe Actions (Pin/Mute/Delete/Archive)
package com.neont.messenger.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.Chat
import com.neont.messenger.data.model.MessageStatus
import com.neont.messenger.data.model.Story
import com.neont.messenger.theme.*
import com.neont.messenger.ui.components.StoryBar
import com.neont.messenger.ui.components.TelegramAvatar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chats: List<Chat>,
    stories: List<Story>,
    onChatClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onFabClick: () -> Unit,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onTogglePin: (String) -> Unit = {},
    onToggleMute: (String) -> Unit = {},
    onDeleteChat: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFolderTab by remember { mutableStateOf("All") }
    val folderTabs = listOf("All", "Personal", "Groups", "Channels")

    val filteredChats = remember(chats, selectedFolderTab) {
        when (selectedFolderTab) {
            "Personal" -> chats.filter { it.type == "direct" }
            "Groups" -> chats.filter { it.type == "group" }
            "Channels" -> chats.filter { it.type == "channel" }
            else -> chats
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(TelegramDarkHeader)) {
                // Action Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Neont",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
                )

                // Telegram Folder Tabs Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(folderTabs) { tab ->
                        val isSelected = tab == selectedFolderTab
                        Column(
                            modifier = Modifier
                                .clickable { selectedFolderTab = tab }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) TelegramLightBlue else Color(0xFF7F92A2),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(Modifier.height(4.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .height(2.5.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(TelegramLightBlue)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = TelegramLightBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New Message FAB",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = TelegramDarkBackground,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stories Header Bar
            item {
                StoryBar(
                    stories = stories,
                    onStoryClick = onStoryClick,
                    onAddStoryClick = onAddStoryClick
                )
                Divider(color = TelegramDarkDivider, thickness = 1.dp)
            }

            // Chat Items dengan Swipe Actions
            items(filteredChats, key = { it.chatId }) { chat ->
                SwipeableChatRowItem(
                    chat = chat,
                    onClick = { onChatClick(chat.chatId) },
                    onTogglePin = { onTogglePin(chat.chatId) },
                    onToggleMute = { onToggleMute(chat.chatId) },
                    onDeleteChat = { onDeleteChat(chat.chatId) }
                )
                Divider(
                    color = TelegramDarkDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 74.dp)
                )
            }
        }
    }
}

/**
 * Swipeable Chat Row:
 * Swipe Kanan -> Pin/Unpin & Mute/Unmute
 * Swipe Kiri  -> Delete & Archive
 */
@Composable
private fun SwipeableChatRowItem(
    chat: Chat,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onDeleteChat: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelegramDarkBackground)
    ) {
        // Background Actions saat digeser ke Kanan (Pin & Mute)
        if (offsetX > 0) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF2B5278))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (chat.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = "Pin/Unpin",
                        tint = if (chat.isPinned) TelegramLightBlue else Color.White
                    )
                }
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (chat.isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Mute/Unmute",
                        tint = Color.White
                    )
                }
            }
        }

        // Background Actions saat digeser ke Kiri (Delete & Archive)
        if (offsetX < 0) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFC0392B))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDeleteChat) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDeleteChat) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archive",
                        tint = Color.White
                    )
                }
            }
        }

        // Konten Baris Chat Depan
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(TelegramDarkBackground)
                .fillMaxWidth()
                .draggable(
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-160f, 160f)
                        offsetX = newOffset
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        // Kembali ke posisi awal dengan animasi halus
                        offsetX = 0f
                    }
                )
        ) {
            ChatRowContent(
                chat = chat,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ChatRowContent(
    chat: Chat,
    onClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessage?.timestamp) {
        chat.lastMessage?.timestamp?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        TelegramAvatar(
            name = chat.title,
            avatarUrl = chat.avatarUrl,
            size = 52
        )

        Spacer(Modifier.width(12.dp))

        // Center Content: Title & Last Message
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = timeFormatted,
                    color = Color(0xFF7F92A2),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.lastMessage?.senderId == "me") {
                        when (chat.lastMessage.status) {
                            MessageStatus.PENDING -> Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Pending",
                                tint = Color(0x99FFFFFF),
                                modifier = Modifier.size(14.dp)
                            )
                            MessageStatus.SENT -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = CheckmarkBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            MessageStatus.DELIVERED, MessageStatus.READ -> Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered / Read",
                                tint = CheckmarkBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            MessageStatus.FAILED -> Text("!", color = Color.Red, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = chat.lastMessage?.text ?: "No messages yet",
                        color = Color(0xFF8B9DAE),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badges: Pin, Mute, Unread Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (chat.isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Muted",
                            tint = Color(0xFF7F92A2),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (chat.isPinned && chat.unreadCount == 0) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFF7F92A2),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (chat.isMuted) BadgeMuted else BadgeUnread)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
