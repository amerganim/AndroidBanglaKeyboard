package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.amerganim.banglakeyboard.R
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/** Emoji picker shown in place of the keys. Tap an emoji to insert it. */
@Composable
fun EmojiPanel(vm: KeyboardViewModel, modifier: Modifier = Modifier) {
    var category by remember { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val rowHeight = vm.keySize.rowHeight

    Column(modifier.fillMaxWidth()) {
        // Category tabs.
        Row(Modifier.fillMaxWidth()) {
            EmojiCategories.forEachIndexed { index, cat ->
                val selected = index == category
                Box(
                    Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(if (selected) colors.secondaryContainer else colors.background)
                        .clickable { category = index },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(cat.icon, fontSize = 20.sp)
                }
            }
        }

        // Emoji grid for the selected category.
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxWidth().height(rowHeight * 3.2f),
        ) {
            items(EmojiCategories[category].emojis) { e ->
                Box(
                    Modifier
                        .height(rowHeight)
                        .clickable { vm.onEmoji(e) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(e, fontSize = 22.sp, textAlign = TextAlign.Center)
                }
            }
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

private class EmojiCategory(val icon: String, val emojis: List<String>)

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
                "💅 👶 🧒 👦 👧 🧑 👨 👩 🧓 👴 👵 🧔 👮 👷 💂 🕵️ 👰 🤵 👸 🤴 🦸 🦹 🤰 🤱 👼 🎅 🤶",
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
