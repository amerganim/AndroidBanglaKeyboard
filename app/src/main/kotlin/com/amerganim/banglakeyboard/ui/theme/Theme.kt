package com.amerganim.banglakeyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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

/**
 * A keyboard color theme built from a few roles. [key] is the normal key face,
 * [special] the shift/backspace/symbol keys, [accent] the Enter key and highlights.
 */
private fun keyboardScheme(
    dark: Boolean,
    background: Color,
    key: Color,
    special: Color,
    text: Color,
    subText: Color,
    accent: Color,
    onAccent: Color,
): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = special,
        onPrimaryContainer = text,
        background = background,
        surface = background,
        onBackground = text,
        onSurface = text,
        onSurfaceVariant = subText,
        surfaceContainerLowest = key,
        surfaceContainerLow = key,
        surfaceContainerHighest = special,
        secondaryContainer = special,
        onSecondaryContainer = text,
        surfaceVariant = special,
        outline = subText.copy(alpha = 0.4f),
    )
}

/**
 * The keyboard's selectable color themes. [SYSTEM] follows the phone's light/dark
 * setting; the rest are fixed. Themes only color the keyboard — the app's own
 * screens keep following the system.
 */
enum class KeyboardTheme {
    SYSTEM,
    LIGHT,
    DARK,
    BLACK,
    BANGLA_GREEN,
    OCEAN,
    MIDNIGHT,
    ROSE,
    SUNSET,
    LAVENDER,
    ;

    /** The color scheme for this theme; [systemDark] is only used by [SYSTEM]. */
    fun colors(systemDark: Boolean): ColorScheme = when (this) {
        SYSTEM -> if (systemDark) DarkColors else LightColors
        LIGHT -> LightColors
        DARK -> DarkColors
        BLACK -> keyboardScheme(
            dark = true, background = Color(0xFF000000), key = Color(0xFF1C1C1E),
            special = Color(0xFF111113), text = Color(0xFFF2F2F2), subText = Color(0xFFA0A0A8),
            accent = AccentDark, onAccent = Color(0xFF15152E),
        )
        // Bottle green with the flag's red on Enter.
        BANGLA_GREEN -> keyboardScheme(
            dark = true, background = Color(0xFF0B3B2C), key = Color(0xFF15573F),
            special = Color(0xFF0F4A35), text = Color(0xFFF1F8F4), subText = Color(0xFFA9CDBD),
            accent = Color(0xFFE63946), onAccent = Color.White,
        )
        OCEAN -> keyboardScheme(
            dark = true, background = Color(0xFF0B2545), key = Color(0xFF173C6B),
            special = Color(0xFF102E55), text = Color(0xFFEEF4FB), subText = Color(0xFFA8BFD9),
            accent = Color(0xFF4FC3F7), onAccent = Color(0xFF062038),
        )
        MIDNIGHT -> keyboardScheme(
            dark = true, background = Color(0xFF17132B), key = Color(0xFF2B2450),
            special = Color(0xFF211B3E), text = Color(0xFFF0EDFA), subText = Color(0xFFB9B0DA),
            accent = Color(0xFFB197FC), onAccent = Color(0xFF1C1238),
        )
        ROSE -> keyboardScheme(
            dark = false, background = Color(0xFFF6DDE4), key = Color(0xFFFFFFFF),
            special = Color(0xFFEBC0CC), text = Color(0xFF3A1A24), subText = Color(0xFF7A4D5B),
            accent = Color(0xFFD6336C), onAccent = Color.White,
        )
        SUNSET -> keyboardScheme(
            dark = false, background = Color(0xFFFBE3C8), key = Color(0xFFFFF8EE),
            special = Color(0xFFF2C894), text = Color(0xFF3B2410), subText = Color(0xFF7A5634),
            accent = Color(0xFFE8590C), onAccent = Color.White,
        )
        LAVENDER -> keyboardScheme(
            dark = false, background = Color(0xFFE7E1F6), key = Color(0xFFFFFFFF),
            special = Color(0xFFD2C7EE), text = Color(0xFF24193F), subText = Color(0xFF5C4F80),
            accent = Color(0xFF7048E8), onAccent = Color.White,
        )
    }

    companion object {
        fun fromName(name: String?): KeyboardTheme =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/** App theme. Follows the system dark/light setting. */
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

/** Keyboard theme: the user's chosen [theme] (SYSTEM follows light/dark). */
@Composable
fun KeyboardColorTheme(theme: KeyboardTheme, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = theme.colors(isSystemInDarkTheme()), content = content)
}
