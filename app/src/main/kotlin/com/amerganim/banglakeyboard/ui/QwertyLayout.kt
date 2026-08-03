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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.R
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
            contentDescription = stringResource(R.string.key_shift),
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
            contentDescription = stringResource(
                if (vm.symbolsPage) R.string.key_letters else R.string.key_symbols,
            ),
            style = KeyStyle.SPECIAL,
            height = vm.keySize.rowHeight,
        )
        KeyButton(
            onClick = vm::onModeSwitch,
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Language,
            contentDescription = stringResource(R.string.key_switch_language),
            style = KeyStyle.SPECIAL,
            height = vm.keySize.rowHeight,
        )
        KeyButton(
            onClick = vm::toggleEmoji,
            modifier = Modifier.weight(1f),
            label = "😊",
            contentDescription = stringResource(R.string.key_emoji),
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
            // The label names the current mode, which is useful context, but the
            // key's actual function is Space — announce both.
            contentDescription = stringResource(R.string.key_space) + ", " + when (vm.mode) {
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
            contentDescription = stringResource(enterDescription(vm.imeAction)),
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

/** TalkBack description matching [enterIcon], so the key is not announced silently. */
private fun enterDescription(action: Int): Int = when (action) {
    EditorInfo.IME_ACTION_SEARCH -> R.string.key_search
    EditorInfo.IME_ACTION_SEND -> R.string.key_send
    EditorInfo.IME_ACTION_DONE -> R.string.key_done
    EditorInfo.IME_ACTION_GO -> R.string.key_go
    EditorInfo.IME_ACTION_NEXT -> R.string.key_next
    EditorInfo.IME_ACTION_PREVIOUS -> R.string.key_previous
    else -> R.string.key_enter
}

@Composable
private fun RowScope.BackspaceKey(vm: KeyboardViewModel, weight: Float) {
    KeyButton(
        onClick = vm::onBackspace,
        modifier = Modifier.weight(weight),
        icon = Icons.AutoMirrored.Filled.Backspace,
        contentDescription = stringResource(R.string.key_backspace),
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
        contentDescription = stringResource(R.string.key_more_symbols),
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
    // In Bangla modes the number page shows Bangla digits — except in number/PIN
    // fields, where digits stay ASCII (and are typed as ASCII).
    val banglaDigits = vm.mode != KeyboardMode.ENGLISH && !vm.numericField
    // Top-row letters long-press to their digit (Bangla digit in Bangla modes).
    val digit = TOP_ROW_DIGITS[c]
    val longPress: (() -> Unit)? = when {
        hasLiteralAlternate -> { { vm.onLiteral(c) } }
        digit != null -> { { vm.onChar(digit) } }
        else -> null
    }
    // The "।"/"." key long-presses to a punctuation popup — this is the only way to
    // reach the English "." (and other marks) in Bangla mode.
    val isPeriodKey = c == '.'
    val hint = when {
        hasLiteralAlternate -> c.toString()
        digit != null -> if (banglaDigits) BANGLA_DIGITS[digit - '0'].toString() else digit.toString()
        isPeriodKey && vm.mode != KeyboardMode.ENGLISH -> "." // period lives on long-press here
        else -> null
    }
    KeyButton(
        onClick = { vm.onChar(shown) },
        modifier = Modifier.weight(1f),
        // In Bangla mode, show the actual sign glyph so it's discoverable.
        label = displayLabel(shown, vm.mode, banglaDigits),
        onLongPress = longPress,
        hint = hint,
        height = vm.keySize.rowHeight,
        popupChars = if (isPeriodKey) PUNCTUATION_POPUP else null,
        onPopupChar = if (isPeriodKey) { ch -> vm.onLiteral(ch) } else null,
    )
}

/** Common punctuation reachable by long-pressing the "।"/"." key. */
private val PUNCTUATION_POPUP = listOf('.', ',', '?', '!', ':', ';', '-', '"')

private const val BANGLA_DIGITS = "০১২৩৪৫৬৭৮৯"

/** The label shown on a key — Bangla sign/digit glyphs replace their roman triggers. */
private fun displayLabel(c: Char, mode: KeyboardMode, banglaDigits: Boolean): String {
    if (mode != KeyboardMode.ENGLISH) {
        when (c) {
            '^' -> return "ঁ" // chandrabindu
            ':' -> return "ঃ" // visarga
            '`' -> return "্" // hasanta
            '.' -> return "।" // dari
        }
        if (banglaDigits && c in '0'..'9') return BANGLA_DIGITS[c - '0'].toString()
    }
    return c.toString()
}
