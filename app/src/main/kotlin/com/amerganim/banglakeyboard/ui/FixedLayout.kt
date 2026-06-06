package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/**
 * Experimental **Bangla Fixed layout**: a phonetic-mnemonic fixed keyboard. Each
 * QWERTY position shows the Bangla letter that matches the English key's sound
 * (k→ক, m→ম, a→আ…). Tap inserts that letter; **Shift** gives the retroflex /
 * long-vowel variant (t→ত, Shift+t→ট); **long-press** gives the aspirate (k→ক,
 * hold→খ). Keys feed the same engine, so kars and conjuncts form automatically.
 */

/** label = Bangla glyph shown; token = roman fed to the engine. */
private class Fk(
    val label: String,
    val token: String,
    val shiftLabel: String? = null,
    val shiftToken: String? = null,
    val longLabel: String? = null,
    val longToken: String? = null,
)

private val ROW1 = listOf(
    Fk("ক", "k", "খ", "kh"),
    Fk("ও", "O", "ঔ", "OU"),
    Fk("এ", "e", "ঐ", "OI"),
    Fk("র", "r", "ড়", "R"),
    Fk("ত", "t", "ট", "T", "থ", "th"),
    Fk("য়", "y", "য", "z"),
    Fk("উ", "u", "ঊ", "U"),
    Fk("ই", "i", "ঈ", "I"),
    Fk("অ", "o", "আ", "a"),
    Fk("প", "p", "ফ", "ph"),
)

private val ROW2 = listOf(
    Fk("আ", "a", null, null, "অ", "o"),
    Fk("স", "s", "ষ", "S", "শ", "sh"),
    Fk("দ", "d", "ড", "D", "ধ", "dh"),
    Fk("ফ", "f", null, null, "ভ", "v"),
    Fk("গ", "g", "ঘ", "gh"),
    Fk("হ", "h", "ঃ", ":"),
    Fk("জ", "j", "ঝ", "jh"),
    Fk("ক", "k", "খ", "kh"),
    Fk("ল", "l"),
)

private val ROW3 = listOf(
    Fk("য", "z"),
    Fk("ক্ষ", "kSh"),
    Fk("চ", "ch", "ছ", "chh"),
    Fk("ভ", "v", null, null, "ব", "bh"),
    Fk("ব", "b", "ভ", "bh"),
    Fk("ন", "n", "ণ", "N", "ঙ", "Ng"),
    Fk("ম", "m", "ং", "ng", "ঁ", "^"),
)

@Composable
fun FixedRows(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) { for (k in ROW1) FixedKey(k, vm) }
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(0.5f))
        for (k in ROW2) FixedKey(k, vm)
        Spacer(Modifier.weight(0.5f))
    }
    Row(Modifier.fillMaxWidth()) {
        KeyButton(
            onClick = vm::onShift,
            modifier = Modifier.weight(1.5f),
            icon = Icons.Filled.KeyboardArrowUp,
            style = KeyStyle.SPECIAL,
            active = vm.shifted,
            height = vm.keySize.rowHeight,
        )
        for (k in ROW3) FixedKey(k, vm)
        KeyButton(
            onClick = vm::onBackspace,
            modifier = Modifier.weight(1.5f),
            icon = Icons.AutoMirrored.Filled.Backspace,
            style = KeyStyle.SPECIAL,
            repeatOnHold = true,
            height = vm.keySize.rowHeight,
        )
    }
}

@Composable
private fun RowScope.FixedKey(k: Fk, vm: KeyboardViewModel) {
    val useShift = vm.shifted && k.shiftToken != null
    val token = if (useShift) k.shiftToken!! else k.token
    val label = if (useShift) k.shiftLabel!! else k.label
    KeyButton(
        onClick = { vm.onToken(token) },
        modifier = Modifier.weight(1f),
        label = label,
        onLongPress = k.longToken?.let { lt -> { vm.onToken(lt) } },
        hint = if (!useShift) k.longLabel else null,
        height = vm.keySize.rowHeight,
    )
}
