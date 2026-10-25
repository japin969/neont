// Neont Messenger - Color.kt
// Palet Warna Telegram Android Pixel-Perfect (Classic Dark & Light)
package com.neont.messenger.theme

import androidx.compose.ui.graphics.Color

// Backgrounds
val TelegramDarkBackground = Color(0xFF17212B)
val TelegramDarkHeader = Color(0xFF242F3D)
val TelegramDarkSurface = Color(0xFF1E2C3A)
val TelegramDarkDivider = Color(0xFF101419)

// Accent Colors
val TelegramLightBlue = Color(0xFF5288C1)
val TelegramSkyBlue = Color(0xFF4FA0FF)
val TelegramBlueDark = Color(0xFF2B5278)

// Chat Bubbles
val BubbleIncomingDark = Color(0xFF182533)
val BubbleOutgoingDark = Color(0xFF2B5278)
val BubbleIncomingLight = Color(0xFFFFFFFF)
val BubbleOutgoingLight = Color(0xFFEEFFDE)

// Indicators & Badges
val CheckmarkBlue = Color(0xFF4FA0FF)
val BadgeUnread = Color(0xFF5288C1)
val BadgeMuted = Color(0xFF4E5C6A)
val OnlineGreen = Color(0xFF00C853)

// Telegram Avatar Gradient Array (Berdasarkan ID / Karakter)
val AvatarGradients = listOf(
    listOf(Color(0xFFE17076), Color(0xFFFF885E)), // Red / Coral
    listOf(Color(0xFFFAA774), Color(0xFFFFCD6A)), // Orange / Yellow
    listOf(Color(0xFFA695E7), Color(0xFF7E6CD9)), // Violet
    listOf(Color(0xFF7BC862), Color(0xFF65A947)), // Green
    listOf(Color(0xFF6EC9CB), Color(0xFF539DA4)), // Cyan
    listOf(Color(0xFF65AADD), Color(0xFF4986BC)), // Blue
    listOf(Color(0xFFEE7AAE), Color(0xFFD65691))  // Pink
)
