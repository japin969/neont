// Neont Messenger - EmojiKeyboard.kt
// Panel Emoji dan Stickers Telegram Android
package com.neont.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkHeader
import com.neont.messenger.theme.TelegramDarkSurface
import com.neont.messenger.theme.TelegramLightBlue

@Composable
fun EmojiKeyboard(
    onEmojiSelected: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("Smileys", "Gestures", "Hearts", "Animals", "Objects")

    val emojiLists = listOf(
        listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😚", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🫢", "🫣", "🤫", "🤔"),
        listOf("👍", "👎", "👏", "🙌", "👐", "🤝", "🤛", "🤜", "👊", "✊", "🤛", "✌️", "🤞", "🤟", "🤘", "👌", "🤌", "🤏", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪"),
        listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "🔥", "✨", "⚡", "🌟", "💫", "💥"),
        listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄"),
        listOf("📱", "💻", "⌚", "📷", "💡", "🔦", "📖", "📚", "🏷️", "💰", "💳", "💎", "🔑", "🔒", "🎁", "🎈", "🎉", "🏆", "⚽", "🏀", "🚗", "✈️", "🚀", "🛸", "🛰️")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(TelegramDarkSurface)
    ) {
        // Tab Row Kategori Emoji
        TabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = TelegramDarkHeader,
            contentColor = TelegramLightBlue
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = { Text(cat, fontSize = 12.sp, color = if (selectedCategoryIndex == index) TelegramLightBlue else Color.Gray) }
                )
            }
        }

        // Grid Emoji
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(emojiLists[selectedCategoryIndex]) { emoji ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }

            // Floating Backspace Button di kanan bawah
            IconButton(
                onClick = onBackspaceClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(TelegramDarkHeader, CircleShape)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
            }
        }
    }
}
