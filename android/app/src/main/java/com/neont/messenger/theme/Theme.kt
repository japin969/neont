// Neont Messenger - Theme.kt
package com.neont.messenger.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TelegramLightBlue,
    secondary = TelegramSkyBlue,
    tertiary = TelegramBlueDark,
    background = TelegramDarkBackground,
    surface = TelegramDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramLightBlue,
    secondary = TelegramSkyBlue,
    tertiary = TelegramBlueDark,
    background = Color(0xFFF0F2F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun NeontTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Telegram Android defaults to iconic dark

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
