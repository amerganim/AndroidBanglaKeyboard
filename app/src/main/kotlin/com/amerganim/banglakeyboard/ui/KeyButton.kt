package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Visual role of a key. */
enum class KeyStyle { NORMAL, SPECIAL, ACCENT }

/**
 * A single keyboard key. Width is set by the caller via [modifier] (weight).
 *
 * @param repeatOnHold when true, holding the key fires [onClick] repeatedly
 *   (used by Backspace for fast deletion).
 */
@Composable
fun KeyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    style: KeyStyle = KeyStyle.NORMAL,
    active: Boolean = false,
    repeatOnHold: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    hint: String? = null,
    height: Dp = 54.dp,
    popupChars: List<Char>? = null,
    onPopupChar: ((Char) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressedByClick by interaction.collectIsPressedAsState()
    var pressedByHold by remember { mutableStateOf(false) }
    var popupOpen by remember { mutableStateOf(false) }
    val pressed = pressedByClick || pressedByHold

    val baseColor = when {
        active || style == KeyStyle.ACCENT -> colors.primary
        style == KeyStyle.SPECIAL -> colors.secondaryContainer
        else -> colors.surfaceContainerLowest
    }
    val background = if (pressed) baseColor.blendTowards(colors.primary, 0.35f) else baseColor
    val contentColor = if (active || style == KeyStyle.ACCENT) colors.onPrimary else colors.onSurface

    val hasPopup = !popupChars.isNullOrEmpty() && onPopupChar != null
    val clickModifier = when {
        repeatOnHold -> Modifier.repeatingClickable(
            onClick = onClick,
            onPressedChange = { pressedByHold = it },
        )
        hasPopup -> Modifier.tapOrLongPress(
            onClick = onClick,
            onLongPress = { popupOpen = true },
            onPressedChange = { pressedByHold = it },
        )
        onLongPress != null -> Modifier.tapOrLongPress(
            onClick = onClick,
            onLongPress = onLongPress,
            onPressedChange = { pressedByHold = it },
        )
        else -> Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
        )
    }

    Box(
        modifier = modifier
            .padding(horizontal = 3.dp, vertical = 4.dp)
            .height(height)
            .shadow(if (pressed) 0.dp else 1.dp, RoundedCornerShape(9.dp), clip = false)
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        // Small corner hint advertising the long-press alternate character.
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
            label != null -> Text(
                text = label,
                fontSize = if (label.length > 2) 15.sp else 19.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }

        // Long-press punctuation popup (shown above the key).
        if (popupOpen && hasPopup) {
            val yOffset = with(LocalDensity.current) { -(height + 6.dp).roundToPx() }
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffset),
                onDismissRequest = { popupOpen = false },
                properties = PopupProperties(focusable = false),
            ) {
                Row(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(4.dp),
                ) {
                    for (ch in popupChars!!) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onPopupChar!!(ch)
                                    popupOpen = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(ch.toString(), fontSize = 19.sp, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tap fires [onClick]; a long hold fires [onLongPress] instead. Used for keys
 * with an alternate character (e.g. tap `ঁ`, long-press literal `^`).
 */
@Composable
private fun Modifier.tapOrLongPress(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onPressedChange: (Boolean) -> Unit,
): Modifier {
    val currentClick by rememberUpdatedState(onClick)
    val currentLong by rememberUpdatedState(onLongPress)
    val currentPressed by rememberUpdatedState(onPressedChange)
    return this.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                currentPressed(true)
                tryAwaitRelease()
                currentPressed(false)
            },
            onTap = { currentClick() },
            onLongPress = { currentLong() },
        )
    }
}

/** Blend [this] toward [other] by [fraction] for a simple pressed highlight. */
private fun Color.blendTowards(other: Color, fraction: Float): Color = Color(
    red = red + (other.red - red) * fraction,
    green = green + (other.green - green) * fraction,
    blue = blue + (other.blue - blue) * fraction,
    alpha = alpha,
)

/**
 * Fires [onClick] once on press, then—if held past [initialDelayMs]—repeatedly
 * every [repeatIntervalMs] until release. Used for hold-to-delete on Backspace.
 */
@Composable
private fun Modifier.repeatingClickable(
    onClick: () -> Unit,
    onPressedChange: (Boolean) -> Unit,
    initialDelayMs: Long = 380L,
    repeatIntervalMs: Long = 45L,
): Modifier {
    val currentClick by rememberUpdatedState(onClick)
    val currentPressed by rememberUpdatedState(onPressedChange)
    return this.pointerInput(Unit) {
        coroutineScope {
            val scope = this
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                currentPressed(true)
                currentClick() // immediate first delete
                val repeatJob = scope.launch {
                    delay(initialDelayMs)
                    while (true) {
                        currentClick()
                        delay(repeatIntervalMs)
                    }
                }
                waitForUpOrCancellation()
                repeatJob.cancel()
                currentPressed(false)
            }
        }
    }
}
