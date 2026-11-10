package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amerganim.banglakeyboard.R
import com.amerganim.banglakeyboard.ui.theme.KeyboardTheme

/**
 * A horizontally scrolling row of keyboard-theme swatches, each a tiny preview of
 * the keyboard in that theme (background, key caps, Enter accent). With
 * [borders] false the previews are drawn borderless too.
 */
@Composable
fun ThemePicker(selected: KeyboardTheme, borders: Boolean, onSelect: (KeyboardTheme) -> Unit) {
    val systemDark = isSystemInDarkTheme()
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (theme in KeyboardTheme.entries) {
            val c = theme.colors(systemDark)
            val isSelected = theme == selected
            Column(
                Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(theme) }
                    .semantics {
                        role = Role.RadioButton
                        this.selected = isSelected
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(width = 72.dp, height = 52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.background)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(7.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(4) { i ->
                                    val accent = row == 1 && i == 3
                                    MiniKey(
                                        when {
                                            accent -> c.primary
                                            borders -> c.surfaceContainerLowest
                                            else -> Color.Transparent
                                        },
                                        dot = if (accent) c.onPrimary else c.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(themeName(theme)),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MiniKey(face: Color, dot: Color) {
    Box(
        Modifier
            .size(width = 11.dp, height = 14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(face),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(3.dp).clip(RoundedCornerShape(2.dp)).background(dot))
    }
}

private fun themeName(theme: KeyboardTheme): Int = when (theme) {
    KeyboardTheme.SYSTEM -> R.string.theme_system
    KeyboardTheme.LIGHT -> R.string.theme_light
    KeyboardTheme.DARK -> R.string.theme_dark
    KeyboardTheme.BLACK -> R.string.theme_black
    KeyboardTheme.BANGLA_GREEN -> R.string.theme_bangla_green
    KeyboardTheme.OCEAN -> R.string.theme_ocean
    KeyboardTheme.MIDNIGHT -> R.string.theme_midnight
    KeyboardTheme.ROSE -> R.string.theme_rose
    KeyboardTheme.SUNSET -> R.string.theme_sunset
    KeyboardTheme.LAVENDER -> R.string.theme_lavender
}
