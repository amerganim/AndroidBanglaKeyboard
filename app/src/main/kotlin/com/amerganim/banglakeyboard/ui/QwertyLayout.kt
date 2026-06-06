package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.ime.KeyboardMode
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/**
 * A QWERTY layout shared by English and Bangla Phonetic modes. Every character
 * key routes through [KeyboardViewModel.onChar]; the ViewModel decides whether to
 * commit directly (English) or transliterate into the composing buffer (Phonetic).
 */
@Composable
fun QwertyLayout(vm: KeyboardViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)) {
        if (vm.symbolsPage) SymbolRows(vm) else LetterRows(vm)
        BottomRow(vm)
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
private fun SymbolRows(vm: KeyboardViewModel) {
    Row(Modifier.fillMaxWidth()) {
        for (c in "1234567890") CharKey(c, vm)
    }
    Row(Modifier.fillMaxWidth()) {
        for (c in "@#\$_&-+()/") CharKey(c, vm)
    }
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(1.5f))
        for (c in "*\"':;!?") CharKey(c, vm)
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
        CharKey(',', vm)
        KeyButton(
            onClick = vm::onSpace,
            modifier = Modifier.weight(4f),
            label = if (vm.mode == KeyboardMode.ENGLISH) "English" else "বাংলা",
            height = vm.keySize.rowHeight,
        )
        CharKey('.', vm)
        KeyButton(
            onClick = vm::onEnter,
            modifier = Modifier.weight(1.5f),
            icon = Icons.AutoMirrored.Filled.KeyboardReturn,
            style = KeyStyle.ACCENT,
            height = vm.keySize.rowHeight,
        )
    }
}

/** A character key that respects the shift state for casing. */
@Composable
private fun RowScope.CharKey(c: Char, vm: KeyboardViewModel) {
    val shown = if (vm.shifted) c.uppercaseChar() else c
    KeyButton(
        onClick = { vm.onChar(shown) },
        modifier = Modifier.weight(1f),
        label = shown.toString(),
        height = vm.keySize.rowHeight,
    )
}
