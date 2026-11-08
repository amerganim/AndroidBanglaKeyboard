package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amerganim.banglakeyboard.R
import com.amerganim.banglakeyboard.data.Clip
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/**
 * Clipboard history shown in place of the keys (opened from the 📋 button in the
 * suggestion bar). Tap a clip to paste it; pin it to keep it past the one-hour
 * expiry; ✕ removes it. Same height as the emoji panel, so the keyboard doesn't jump.
 */
@Composable
fun ClipboardPanel(vm: KeyboardViewModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val rowHeight = vm.keySize.rowHeight

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(PANEL_HEADER_HEIGHT).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.clipboard_title),
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (vm.clips.any { !it.pinned }) {
                TextButton(onClick = vm::onClipsClear) {
                    Text(stringResource(R.string.clipboard_clear))
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(panelBodyHeight(rowHeight))) {
            when {
                !vm.clipboardEnabled -> PanelMessage(stringResource(R.string.clipboard_off))
                vm.clips.isEmpty() -> PanelMessage(stringResource(R.string.clipboard_empty))
                else -> LazyColumn(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(vm.clips, key = { it.text }) { clip -> ClipRow(clip, vm) }
                }
            }
        }

        // Bottom bar: back to letters, space, backspace (same as the emoji panel).
        Row(Modifier.fillMaxWidth()) {
            KeyButton(
                onClick = vm::toggleClipboard,
                modifier = Modifier.weight(2f),
                label = "ABC",
                contentDescription = stringResource(R.string.key_letters),
                style = KeyStyle.SPECIAL,
                height = rowHeight,
            )
            KeyButton(
                onClick = vm::onSpace,
                modifier = Modifier.weight(5f),
                label = "",
                contentDescription = stringResource(R.string.key_space),
                height = rowHeight,
            )
            KeyButton(
                onClick = vm::onBackspace,
                modifier = Modifier.weight(2f),
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.key_backspace),
                style = KeyStyle.SPECIAL,
                repeatOnHold = true,
                height = rowHeight,
            )
        }
    }
}

@Composable
private fun ClipRow(clip: Clip, vm: KeyboardViewModel) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceContainerLowest)
            .clickable { vm.onClipPaste(clip.text) }
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            clip.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 15.sp,
            color = colors.onSurface,
            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
        )
        IconButton(onClick = { vm.onClipPin(clip.text, !clip.pinned) }) {
            Icon(
                imageVector = if (clip.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = stringResource(
                    if (clip.pinned) R.string.clipboard_unpin else R.string.clipboard_pin,
                ),
                tint = if (clip.pinned) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = { vm.onClipDelete(clip.text) }) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.clipboard_delete),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PanelMessage(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Height of the tab / title strip at the top of the emoji and clipboard panels. */
internal val PANEL_HEADER_HEIGHT = 38.dp

/**
 * Height of a panel's body so that header + body exactly replaces the three letter
 * rows (each key row is its height plus 4dp padding above and below). Keeping the
 * panels the same height as the keys stops the keyboard from jumping when they open.
 */
internal fun panelBodyHeight(rowHeight: Dp): Dp = (rowHeight + 8.dp) * 3 - PANEL_HEADER_HEIGHT
