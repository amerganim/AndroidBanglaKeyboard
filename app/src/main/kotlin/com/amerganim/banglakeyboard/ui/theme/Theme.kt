package com.amerganim.banglakeyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand accent (indigo/violet) used for primary actions and the Enter key.
private val Accent = Color(0xFF5B5BD6)
private val AccentDark = Color(0xFF9D9DF5)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E3FB),
    onPrimaryContainer = Color(0xFF1A1A4D),
    // Keyboard background.
    background = Color(0xFFDDE1E7),
    surface = Color(0xFFDDE1E7),
    onSurface = Color(0xFF1B1C1E),
    onSurfaceVariant = Color(0xFF44474C),
    // Normal key face.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FA),
    // Special-key face (shift, symbols, backspace).
    secondaryContainer = Color(0xFFC4C9D2),
    onSecondaryContainer = Color(0xFF1B1C1E),
    surfaceVariant = Color(0xFFCDD2DB),
    outline = Color(0xFFB6BBC4),
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color(0xFF15152E),
    primaryContainer = Color(0xFF35357A),
    onPrimaryContainer = Color(0xFFE3E3FB),
    // Keyboard background.
    background = Color(0xFF15171C),
    surface = Color(0xFF15171C),
    onSurface = Color(0xFFECEDEF),
    onSurfaceVariant = Color(0xFFC2C5CC),
    // Normal key face.
    surfaceContainerLowest = Color(0xFF34373D),
    surfaceContainerLow = Color(0xFF2A2D33),
    // Special-key face.
    secondaryContainer = Color(0xFF23262B),
    onSecondaryContainer = Color(0xFFECEDEF),
    surfaceVariant = Color(0xFF23262B),
    outline = Color(0xFF44474C),
)

/** App + keyboard theme. Follows the system dark/light setting. */
@Composable
fun BanglaKeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
