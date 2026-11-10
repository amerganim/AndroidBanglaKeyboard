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
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
 * Whether keys are drawn with key-cap backgrounds. When false ("borderless"), only
 * the Enter key and active keys keep a filled cap; other keys show just their
 * label, and light up while pressed.
 */
val LocalKeyBorders = staticCompositionLocalOf { true }

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
    /**
     * What TalkBack announces for this key. Required for icon-only keys, which
     * would otherwise be silent; text keys fall back to reading their [label].
     */
    contentDescription: String? = null,
    style: KeyStyle = KeyStyle.NORMAL,
    active: Boolean = false,
    repeatOnHold: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    hint: String? = null,
    /** Small corner icon advertising a long-press shortcut (e.g. copy on C). */
    hintIcon: ImageVector? = null,
    height: Dp = 54.dp,
    popupChars: List<Char>? = null,
    onPopupChar: ((Char) -> Unit)? = null,
    /**
     * Spacebar cursor control: when set, sliding sideways on the key calls
     * [onDragStart] once and then [onDragStep] with whole character steps
     * (negative = left); a plain tap still fires [onClick].
     */
    onDragStart: (() -> Unit)? = null,
    onDragStep: ((Int) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressedByClick by interaction.collectIsPressedAsState()
    var pressedByHold by remember { mutableStateOf(false) }
    var popupOpen by remember { mutableStateOf(false) }
    val pressed = pressedByClick || pressedByHold
    val filled = active || style == KeyStyle.ACCENT
    val borderless = !LocalKeyBorders.current && !filled

    val baseColor = when {
        filled -> colors.primary
        style == KeyStyle.SPECIAL -> colors.secondaryContainer
        else -> colors.surfaceContainerLowest
    }
    val background = when {
        borderless && !pressed -> Color.Transparent
        pressed -> baseColor.blendTowards(colors.primary, 0.35f)
        else -> baseColor
    }
    val contentColor = if (active || style == KeyStyle.ACCENT) colors.onPrimary else colors.onSurface

    val hasPopup = !popupChars.isNullOrEmpty() && onPopupChar != null
    val clickModifier = when {
        onDragStep != null -> Modifier.tapOrSlide(
            onClick = onClick,
            onSlideStart = onDragStart ?: {},
            onSlideStep = onDragStep,
            onPressedChange = { pressedByHold = it },
        )
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
            .shadow(if (pressed || borderless) 0.dp else 1.dp, RoundedCornerShape(9.dp), clip = false)
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .then(clickModifier)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics(mergeDescendants = true) {
                        this.contentDescription = contentDescription
                        this.role = Role.Button
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Small corner hint advertising the long-press alternate character. It is
        // decorative — TalkBack would otherwise read "q 1" for every top-row key.
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                    .clearAndSetSemantics {},
            )
        } else if (hintIcon != null) {
            Icon(
                imageVector = hintIcon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .size(10.dp),
            )
        }
        when {
            // The description lives on the parent Box, so the icon itself is decorative.
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
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

/**
 * Tap fires [onClick]. Sliding sideways past a small threshold instead enters cursor
 * mode: [onSlideStart] once, then [onSlideStep] for every [stepDp] of travel. The
 * finger keeps moving the cursor until it is lifted; no click fires after a slide.
 */
@Composable
private fun Modifier.tapOrSlide(
    onClick: () -> Unit,
    onSlideStart: () -> Unit,
    onSlideStep: (Int) -> Unit,
    onPressedChange: (Boolean) -> Unit,
    stepDp: Dp = 12.dp,
): Modifier {
    val currentClick by rememberUpdatedState(onClick)
    val currentStart by rememberUpdatedState(onSlideStart)
    val currentStep by rememberUpdatedState(onSlideStep)
    val currentPressed by rememberUpdatedState(onPressedChange)
    return this.pointerInput(Unit) {
        val stepPx = stepDp.toPx()
        val slop = viewConfiguration.touchSlop
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            currentPressed(true)
            var totalX = 0f
            var usedX = 0f
            var sliding = false
            var lifted = false
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    lifted = true
                    break
                }
                totalX += change.positionChange().x
                if (!sliding && kotlin.math.abs(totalX) > slop) {
                    sliding = true
                    usedX = if (totalX > 0) slop else -slop
                    currentStart()
                }
                if (sliding) {
                    change.consume()
                    val steps = ((totalX - usedX) / stepPx).toInt()
                    if (steps != 0) {
                        currentStep(steps)
                        usedX += steps * stepPx
                    }
                }
            }
            currentPressed(false)
            if (lifted && !sliding) currentClick()
        }
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
