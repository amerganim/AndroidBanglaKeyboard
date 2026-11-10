package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.amerganim.banglakeyboard.R
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/**
 * Emoji picker shown in place of the keys. Tap an emoji to insert it. The first tab
 * holds recently used emojis and is where the panel opens once there are any.
 */
@Composable
fun EmojiPanel(vm: KeyboardViewModel, modifier: Modifier = Modifier) {
    // Snapshot the recents when the panel opens, so the grid doesn't reshuffle under
    // the user's finger as they tap emojis; it refreshes the next time it opens.
    val tabs = remember {
        listOf(EmojiCategory(RECENT_ICON, vm.recentEmojis)) + EmojiCategories
    }
    var category by remember { mutableIntStateOf(if (vm.recentEmojis.isNotEmpty()) 0 else 1) }
    val colors = MaterialTheme.colorScheme
    val rowHeight = vm.keySize.rowHeight

    Column(modifier.fillMaxWidth()) {
        // Category tabs.
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, cat ->
                val selected = index == category
                Box(
                    Modifier
                        .weight(1f)
                        .height(PANEL_HEADER_HEIGHT)
                        .background(if (selected) colors.secondaryContainer else colors.background)
                        .clickable { category = index },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(cat.icon, fontSize = 20.sp)
                }
            }
        }

        // Emoji grid for the selected category.
        val emojis = tabs[category].emojis
        if (emojis.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(panelBodyHeight(rowHeight)).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.emoji_recent_empty),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxWidth().height(panelBodyHeight(rowHeight)),
        ) {
            items(emojis) { e -> EmojiCell(e, vm) }
        }

        // Bottom bar: back to letters, space, backspace.
        Row(Modifier.fillMaxWidth()) {
            KeyButton(
                onClick = vm::toggleEmoji,
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

/**
 * One emoji in the grid. Skin-tone emojis show the user's chosen tone and a small
 * corner dot; long-press opens the six tone variants above the cell.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmojiCell(emoji: String, vm: KeyboardViewModel) {
    val colors = MaterialTheme.colorScheme
    val rowHeight = vm.keySize.rowHeight
    val base = SkinTones.baseOf(emoji)
    // In the category tabs show the chosen tone; in "recent" the exact emoji used.
    val shown = if (base == emoji) vm.emojiTones[emoji] ?: emoji else emoji
    var picker by remember { mutableStateOf(false) }

    Box(
        Modifier
            .height(rowHeight)
            .combinedClickable(
                onClick = { vm.onEmoji(shown) },
                onLongClick = if (base != null) { { picker = true } } else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(shown, fontSize = 22.sp, textAlign = TextAlign.Center)
        if (base != null) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 8.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(colors.onSurfaceVariant.copy(alpha = 0.5f)),
            )
        }
        if (picker && base != null) {
            val yOffset = with(LocalDensity.current) { -(rowHeight + 4.dp).roundToPx() }
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, yOffset),
                onDismissRequest = { picker = false },
                properties = PopupProperties(focusable = false),
            ) {
                Row(
                    Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHighest)
                        .padding(4.dp),
                ) {
                    for (variant in SkinTones.variants(base)) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (variant == shown) colors.secondaryContainer else Color.Transparent,
                                )
                                .clickable {
                                    picker = false
                                    vm.onEmojiVariant(base, variant)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(variant, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

private class EmojiCategory(val icon: String, val emojis: List<String>)

/** Tab icon for recently used emojis. */
private const val RECENT_ICON = "🕘"

private fun row(s: String) = s.split(" ").filter { it.isNotBlank() }

private val EmojiCategories = listOf(
    EmojiCategory(
        "😀",
        row(
            "😀 😃 😄 😁 😆 😅 😂 🤣 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 😚 😋 😛 😜 🤪 😝 🤗 🤭 🤫 🤔 " +
                "😐 😑 😶 😏 😒 🙄 😬 😌 😔 😪 🤤 😴 😷 🤒 🤕 🤧 🥵 🥶 🥴 😵 🤯 🥳 😎 🤓 🧐 😕 😟 🙁 " +
                "😮 😯 😲 😳 🥺 😨 😰 😢 😭 😱 😖 😞 😓 😩 😫 🥱 😤 😡 😠 🤬 😈 👿 💀 💩 🤡 😺 😻 😹",
        ),
    ),
    EmojiCategory(
        "👍",
        row(
            "👍 👎 👌 ✌️ 🤞 🤟 🤘 👏 🙌 👐 🙏 🤝 💪 👈 👉 👆 👇 ☝️ ✋ 🤚 🖐️ 🖖 👋 🤙 ✊ 👊 🤛 🤜 " +
                "💅 🤳 👶 🧒 👦 👧 🧑 👨 👩 🧓 👴 👵 🧔 👮 👷 💂 🕵️ 👰 🤵 👸 🤴 🦸 🦹 🤰 🤱 👼 " +
                "🎅 🤶 🙋 🙆 🙅 🤷 🤦 🙇 💁 🙎 🙍 💆 💇 🚶 🏃 💃 🕺 🧘 🛀",
        ),
    ),
    EmojiCategory(
        "❤️",
        row(
            "❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❣️ 💕 💞 💓 💗 💖 💘 💝 💟 💋 💌 😻",
        ),
    ),
    EmojiCategory(
        "🐶",
        row(
            "🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🐤 🦆 🦉 🐴 🦄 🐝 🐛 🦋 🐌 🐞 " +
                "🐢 🐍 🐙 🦀 🐠 🐟 🐬 🐳 🐋 🦈 🌸 🌹 🌻 🌷 🌼 🌲 🌴 🌵 🍀 🍁 🍂 🌍 🌙 ⭐ 🔥 🌈 ⚡ ❄️",
        ),
    ),
    EmojiCategory(
        "🍔",
        row(
            "🍏 🍎 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🥑 🌽 🥕 🍞 🧀 🍳 🍔 🍟 🍕 🌭 🥪 🌮 " +
                "🍝 🍜 🍲 🍣 🍤 🍱 🍙 🍚 🍦 🍰 🎂 🍫 🍬 🍭 🍩 🍪 🍯 ☕ 🍵 🥤 🍺 🍻 🥂 🍷 🍸 🍹",
        ),
    ),
    EmojiCategory(
        "⚽",
        row(
            "⚽ 🏀 🏈 ⚾ 🎾 🏐 🏉 🎱 🏓 🏸 🏒 🏑 🏏 ⛳ 🎯 🎮 🎲 🎸 🎺 🎻 🥁 🎨 🎤 🎧 🚗 🚕 🚙 🚌 " +
                "🚲 🛵 ✈️ 🚀 🚁 ⛵ 🚢 🏠 🏢 ⏰ ⌚ 📱 💻 🔋 💡 📷 🎉 🎊 🎁 🎈 🎄 🏆 🥇 🥈 🥉",
        ),
    ),
    EmojiCategory(
        "🔥",
        row(
            "✅ ❌ ⭕ ❗ ❓ 💯 🔥 ✨ ⭐ 🌟 💫 ⚡ 💥 💦 💨 🌈 ☀️ 🌙 ⭐ ☁️ ❄️ 💧 🌊 ✔️ ➕ ➖ ✖️ 💲 " +
                "©️ ®️ ™️ 🔴 🟠 🟡 🟢 🔵 🟣 ⚫ ⚪ 🟥 🟧 🟨 🟩 🟦 🔇 🔔 🎵 🎶 💬 💭 👀 🚫 ♻️ ⚠️",
        ),
    ),
)
