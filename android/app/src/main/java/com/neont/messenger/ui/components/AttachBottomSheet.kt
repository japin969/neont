// Neont Messenger - AttachBottomSheet.kt
// Bottom Sheet Lampiran Telegram: Gallery, File, Location, Poll, Music, Contact
package com.neont.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.theme.TelegramDarkHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (type: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TelegramDarkHeader,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachOptionItem(icon = Icons.Default.Image, label = "Gallery", color = Color(0xFF29B6F6)) {
                    onOptionSelected("gallery")
                }
                AttachOptionItem(icon = Icons.Default.InsertDriveFile, label = "File", color = Color(0xFF26A69A)) {
                    onOptionSelected("file")
                }
                AttachOptionItem(icon = Icons.Default.Place, label = "Location", color = Color(0xFFFF7043)) {
                    onOptionSelected("location")
                }
                AttachOptionItem(icon = Icons.Default.Poll, label = "Poll", color = Color(0xFFAB47BC)) {
                    onOptionSelected("poll")
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Spacer(Modifier.width(20.dp))
                AttachOptionItem(icon = Icons.Default.MusicNote, label = "Music", color = Color(0xFFEC407A)) {
                    onOptionSelected("music")
                }
                Spacer(Modifier.width(48.dp))
                AttachOptionItem(icon = Icons.Default.Person, label = "Contact", color = Color(0xFF5C6BC0)) {
                    onOptionSelected("contact")
                }
            }
        }
    }
}

@Composable
private fun AttachOptionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}
