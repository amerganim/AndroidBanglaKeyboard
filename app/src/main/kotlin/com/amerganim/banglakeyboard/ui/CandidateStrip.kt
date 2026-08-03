package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.amerganim.banglakeyboard.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The bar above the keys: scrollable suggestion / next-word chips on the left and a
 * voice-input (mic) button on the right. Tap a chip to commit; long-press to forget.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CandidateStrip(
    candidates: List<String>,
    onClick: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onMicStart: () -> Unit,
    onMicStop: () -> Unit,
    listening: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(colors.background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
            if (listening) {
                Text(
                    text = "🎙 শুনছি… / Listening…",
                    fontSize = 16.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                LazyRow(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    itemsIndexed(candidates) { index, word ->
                        if (index > 0) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 9.dp)
                                    .width(1.dp)
                                    .background(colors.outline),
                            )
                        }
                        Text(
                            text = word,
                            fontSize = 18.sp,
                            fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (index == 0) colors.primary else colors.onSurface,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = { onClick(word) },
                                    onLongClick = { onLongPress(word) },
                                )
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
        // Push-to-talk: hold to listen, release to insert.
        Box(
            Modifier
                .fillMaxHeight()
                .width(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onMicStart()
                            tryAwaitRelease()
                            onMicStop()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = stringResource(R.string.key_mic),
                tint = if (listening) colors.primary else colors.onSurfaceVariant,
            )
        }
    }
}
