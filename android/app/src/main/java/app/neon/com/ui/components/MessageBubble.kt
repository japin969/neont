package app.neon.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.Message
import app.neon.com.data.model.MessageStatus
import app.neon.com.data.model.MessageType
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleBg = if (isOutgoing) app.neon.com.ui.theme.TelegramBubbleOut else app.neon.com.ui.theme.TelegramBubbleIn
    val timeColor = if (isOutgoing) app.neon.com.ui.theme.TelegramTimeOut else app.neon.com.ui.theme.TelegramTimeIn
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .padding(8.dp)
        ) {
            Column {
                when (message.type) {
                    MessageType.IMAGE -> {
                        AsyncImage(
                            model = message.mediaUrl ?: message.text,
                            contentDescription = "Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    MessageType.VIDEO -> {
                        AsyncImage(
                            model = message.mediaUrl ?: message.text,
                            contentDescription = "Video",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    MessageType.FILE -> {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = "File", tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = message.mediaUrl?.substringAfterLast("/") ?: "File",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 16.sp,
                            lineHeight = 21.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = timeFormatted, color = timeColor, fontSize = 11.sp)
                    if (isOutgoing) {
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = if (message.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (message.status == MessageStatus.READ) Color(0xFF64B5F6) else timeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
