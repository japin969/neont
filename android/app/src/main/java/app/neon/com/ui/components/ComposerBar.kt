// Neont Messenger - ComposerBar.kt
// Pixel-Perfect Reverse Engineering of Telegram Android Message Composer Bar
package app.neon.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.*

@Composable
fun ComposerBar(
    onSendMessage: (String) -> Unit,
    onTyping: (Boolean) -> Unit = {},
    onAttachClick: () -> Unit = {},
    onEmojiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // rememberSaveable to prevent losing draft on recomposition/configuration change
    var text by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(text) {
        onTyping(text.isNotBlank())
    }

    // Background #17212B, Padding 6dp, with elevation shadow
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp)
            .background(TelegramSurface) // #17212B
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TextField Container: Background #242F3D, Corner Radius 24dp
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(TelegramInputBG) // #242F3D
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Emoji (dalam TextField di sebelah kiri): warna abu-abu #8B9DAE
            IconButton(
                onClick = onEmojiClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mood,
                    contentDescription = "Emoji",
                    tint = TelegramTextSecondary, // #8B9DAE
                    modifier = Modifier.size(24.dp)
                )
            }

            // Input Text Field: Multi-line (maksimal 5 baris, menyusut otomatis jika teks dihapus)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Message",
                        color = TelegramTextSecondary, // warna abu-abu #8B9DAE
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(TelegramPrimary),
                    minLines = 1,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }

            // Ikon Attach (kiri / di ujung TextField): warna abu-abu #8B9DAE
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = TelegramTextSecondary, // #8B9DAE
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(-45f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Tombol Kanan: AnimatedContent (Mic vs Send dengan Scale & 90-derajat Rotate Animation)
        val hasText = text.isNotBlank()

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (hasText) TelegramPrimary else Color.Transparent) // #2481CC saat Send
                .clickable {
                    if (hasText) {
                        val messageToSend = text.trim()
                        text = ""
                        onSendMessage(messageToSend)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = hasText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.2f, animationSpec = tween(200)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.2f, animationSpec = tween(180))
                        )
                },
                label = "composer_button_anim"
            ) { targetHasText ->
                val rotation by animateFloatAsState(
                    targetValue = if (targetHasText) 0f else -90f,
                    animationSpec = tween(200),
                    label = "icon_rotation"
                )
                if (targetHasText) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(-15f + rotation) // 90° rotation + tilt
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice",
                        tint = TelegramTextSecondary, // #8B9DAE
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation + 90f) // 90° rotation
                    )
                }
            }
        }
    }
}
