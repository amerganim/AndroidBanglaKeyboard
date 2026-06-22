package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import android.view.inputmethod.EditorInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.ime.KeyboardMode
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/**
 * A QWERTY layout shared by English and Bangla Phonetic modes, plus two symbol
 * pages. Every character key routes through [KeyboardViewModel.onChar]; the
 * ViewModel decides whether to commit directly (English) or transliterate into the
 * composing buffer (Phonetic). In Bangla mode `^` -> ঁ (chandrabindu), `` ` `` -> ্
 * (hasanta), `:` -> ঃ (visarga).
 */
@Composable
fun QwertyLayout(vm: KeyboardViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)) {
        if (vm.emojiPanel) {
            EmojiPanel(vm) // brings its own bottom bar (ABC / space / backspace)
        } else {
            when {
                vm.symbolsPage && vm.symbolsPageIndex == 0 -> SymbolPage1(vm)
                vm.symbolsPage -> SymbolPage2(vm)
                vm.mode == KeyboardMode.BANGLA_FIXED -> FixedRows(vm)
                else -> LetterRows(vm)
            }
            BottomRow(vm)
        }
    }
}

@Composable
private fun LetterRows(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) {
        for (c in "qwertyuiop") CharKey(c, vm)
    }
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(0.5f))
        for (c in "asdfghjkl") CharKey(c, vm)
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
        for (c in "zxcvbnm") CharKey(c, vm)
        BackspaceKey(vm, weight = 1.5f)
    }
}

@Composable
private fun SymbolPage1(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) { for (c in "1234567890") CharKey(c, vm) }
    Row(Modifier.fillMaxWidth()) { for (c in "@#৳_&-+()/") CharKey(c, vm) }
    Row(Modifier.fillMaxWidth()) {
        PageSwitchKey(vm, "1/2")
        for (c in listOf('^', ':', '!', '?', '*', '"', '\'')) CharKey(c, vm)
        BackspaceKey(vm, weight = 1.5f)
    }
}

@Composable
private fun SymbolPage2(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) {
        for (c in listOf('`', '~', '[', ']', '{', '}', '<', '>', '|', '\\')) CharKey(c, vm)
    }
    Row(Modifier.fillMaxWidth()) {
        for (c in listOf('=', '£', '¢', '€', '¥', '°', '%', '©', '®', '™')) CharKey(c, vm)
    }
    Row(Modifier.fillMaxWidth()) {
        PageSwitchKey(vm, "2/2")
        for (c in listOf('√', 'π', '÷', '×', '±', '¶', '…')) CharKey(c, vm)
        BackspaceKey(vm, weight = 1.5f)
    }
}

@Composable
private fun BottomRow(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) {
        KeyButton(
            onClick = vm::toggleSymbols,
            modifier = Modifier.weight(1.5f),
            label = if (vm.symbolsPage) "ABC" else "?123",
            style = KeyStyle.SPECIAL,
            height = vm.keySize.rowHeight,
        )
        KeyButton(
            onClick = vm::onModeSwitch,
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Language,
            style = KeyStyle.SPECIAL,
            height = vm.keySize.rowHeight,
        )
        KeyButton(
            onClick = vm::toggleEmoji,
            modifier = Modifier.weight(1f),
            label = "😊",
            style = KeyStyle.SPECIAL,
            height = vm.keySize.rowHeight,
        )
        CharKey(',', vm)
        KeyButton(
            onClick = vm::onSpace,
            modifier = Modifier.weight(3f),
            label = when (vm.mode) {
                KeyboardMode.ENGLISH -> "English"
                KeyboardMode.BANGLA_PHONETIC -> "বাংলা"
                KeyboardMode.BANGLA_FIXED -> "আমাদের"
            },
            height = vm.keySize.rowHeight,
        )
        CharKey('.', vm)
        KeyButton(
            onClick = vm::onEnter,
            modifier = Modifier.weight(1.5f),
            icon = enterIcon(vm.imeAction),
            style = KeyStyle.ACCENT,
            height = vm.keySize.rowHeight,
        )
    }
}

/** Icon for the Enter key based on the field's requested action (like Samsung). */
private fun enterIcon(action: Int): ImageVector = when (action) {
    EditorInfo.IME_ACTION_SEARCH -> Icons.Filled.Search
    EditorInfo.IME_ACTION_SEND -> Icons.AutoMirrored.Filled.Send
    EditorInfo.IME_ACTION_DONE -> Icons.Filled.Done
    EditorInfo.IME_ACTION_GO -> Icons.AutoMirrored.Filled.ArrowForward
    EditorInfo.IME_ACTION_NEXT -> Icons.AutoMirrored.Filled.KeyboardTab
    EditorInfo.IME_ACTION_PREVIOUS -> Icons.AutoMirrored.Filled.ArrowBack
    else -> Icons.AutoMirrored.Filled.KeyboardReturn
}

@Composable
private fun RowScope.BackspaceKey(vm: KeyboardViewModel, weight: Float) {
    KeyButton(
        onClick = vm::onBackspace,
        modifier = Modifier.weight(weight),
        icon = Icons.AutoMirrored.Filled.Backspace,
        style = KeyStyle.SPECIAL,
        repeatOnHold = true,
        height = vm.keySize.rowHeight,
    )
}

@Composable
private fun RowScope.PageSwitchKey(vm: KeyboardViewModel, label: String) {
    KeyButton(
        onClick = vm::switchSymbolsPage,
        modifier = Modifier.weight(1.5f),
        label = label,
        style = KeyStyle.SPECIAL,
        height = vm.keySize.rowHeight,
    )
}

/** The top QWERTY row long-presses to a digit (q→1 … p→0), like other keyboards. */
private val TOP_ROW_DIGITS = mapOf(
    'q' to '1', 'w' to '2', 'e' to '3', 'r' to '4', 't' to '5',
    'y' to '6', 'u' to '7', 'i' to '8', 'o' to '9', 'p' to '0',
)

/** A character key that respects the shift state for casing. */
@Composable
private fun RowScope.CharKey(c: Char, vm: KeyboardViewModel) {
    val shown = if (vm.shifted) c.uppercaseChar() else c
    // In any Bangla mode `^`/`` ` `` produce Bangla signs; long-press types the literal.
    val hasLiteralAlternate = vm.mode != KeyboardMode.ENGLISH && (c == '^' || c == '`')
    // Top-row letters long-press to their digit (Bangla digit in Bangla modes).
    val digit = TOP_ROW_DIGITS[c]
    val longPress: (() -> Unit)? = when {
        hasLiteralAlternate -> { { vm.onLiteral(c) } }
        digit != null -> { { vm.onChar(digit) } }
        else -> null
    }
    val hint = when {
        hasLiteralAlternate -> c.toString()
        digit != null -> digit.toString()
        else -> null
    }
    KeyButton(
        onClick = { vm.onChar(shown) },
        modifier = Modifier.weight(1f),
        // In Bangla mode, show the actual sign glyph so it's discoverable.
        label = displayLabel(shown, vm.mode),
        onLongPress = longPress,
        hint = hint,
        height = vm.keySize.rowHeight,
    )
}

/** The label shown on a key — Bangla sign glyphs replace their roman triggers. */
private fun displayLabel(c: Char, mode: KeyboardMode): String {
    if (mode != KeyboardMode.ENGLISH) {
        when (c) {
            '^' -> return "ঁ" // chandrabindu
            ':' -> return "ঃ" // visarga
            '`' -> return "্" // hasanta
            '.' -> return "।" // dari
        }
    }
    return c.toString()
}
