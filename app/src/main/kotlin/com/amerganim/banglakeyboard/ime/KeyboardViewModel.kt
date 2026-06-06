package com.amerganim.banglakeyboard.ime

import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.amerganim.banglakeyboard.data.DictionaryRepository
import com.amerganim.banglakeyboard.data.KeySize
import com.amerganim.banglakeyboard.engine.Transliterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder + input controller for the keyboard. Holds the romanized buffer
 * for phonetic mode and drives the host's [InputConnection] (composing preview,
 * commits, deletes), mirroring the Windows keyboard's "live underlined preview,
 * commit on Space" model.
 *
 * This is a plain class (not an AndroidX ViewModel): the IME service owns its
 * lifetime and supplies the current [InputConnection] via [connection].
 */
class KeyboardViewModel(
    private val connection: () -> InputConnection?,
    private val repository: DictionaryRepository,
    private val scope: CoroutineScope,
) {
    var mode by mutableStateOf(KeyboardMode.BANGLA_PHONETIC)
        private set

    var shifted by mutableStateOf(false)
        private set

    var symbolsPage by mutableStateOf(false)
        private set

    /** Which symbols page is showing (0 or 1) when [symbolsPage] is true. */
    var symbolsPageIndex by mutableStateOf(0)
        private set

    /** Key size (row height), driven by user settings. */
    var keySize by mutableStateOf(KeySize.MEDIUM)
        private set

    /** Suggestions for the current buffer (phonetic mode only). */
    var candidates by mutableStateOf<List<String>>(emptyList())
        private set

    /** The current romanized buffer (for phonetic composing). */
    private var romanBuffer: String = ""

    // ---- Key events from the UI ----------------------------------------

    /** A character key (already cased by the UI per the shift state). */
    fun onChar(c: Char) {
        val ic = connection() ?: return
        when (mode) {
            KeyboardMode.ENGLISH -> ic.commitText(c.toString(), 1)
            KeyboardMode.BANGLA_PHONETIC -> {
                if (Transliterator.isPhoneticInput(c)) {
                    romanBuffer += c
                    ic.setComposingText(Transliterator.transliterate(romanBuffer), 1)
                    refreshCandidates()
                } else {
                    // Non-phonetic char (e.g. a symbol): flush the buffer first.
                    finishComposing(ic)
                    ic.commitText(c.toString(), 1)
                }
            }
        }
        // One-shot shift: releases after a single character.
        if (shifted) shifted = false
    }

    fun onSpace() {
        val ic = connection() ?: return
        if (mode == KeyboardMode.BANGLA_PHONETIC && romanBuffer.isNotEmpty()) {
            // Commit exactly what was previewed, then a space (Windows behavior).
            finishComposing(ic)
        }
        ic.commitText(" ", 1)
    }

    fun onBackspace() {
        val ic = connection() ?: return
        if (mode == KeyboardMode.BANGLA_PHONETIC && romanBuffer.isNotEmpty()) {
            romanBuffer = romanBuffer.dropLast(1)
            if (romanBuffer.isEmpty()) {
                ic.setComposingText("", 1)
                ic.finishComposingText()
                candidates = emptyList()
            } else {
                ic.setComposingText(Transliterator.transliterate(romanBuffer), 1)
                refreshCandidates()
            }
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    fun onEnter() {
        val ic = connection() ?: return
        if (mode == KeyboardMode.BANGLA_PHONETIC && romanBuffer.isNotEmpty()) {
            // Enter commits the composed word without inserting a newline.
            finishComposing(ic)
        } else {
            ic.commitText("\n", 1)
        }
    }

    /**
     * Commit a literal character, bypassing the phonetic engine. Used for the
     * long-press alternates of sign keys (e.g. literal `^` / `` ` `` in Bangla mode).
     */
    fun onLiteral(c: Char) {
        val ic = connection() ?: return
        finishComposing(ic) // commit any pending word first
        ic.commitText(c.toString(), 1)
        if (shifted) shifted = false
    }

    /** The user tapped a suggestion chip. */
    fun onCandidate(word: String) {
        val ic = connection() ?: return
        ic.commitText(word, 1)
        clearBuffer()
        scope.launch { repository.recordUsage(word) }
    }

    fun onShift() {
        shifted = !shifted
    }

    fun toggleSymbols() {
        symbolsPage = !symbolsPage
        symbolsPageIndex = 0
    }

    /** Switch between the two symbol pages (more / fewer symbols). */
    fun switchSymbolsPage() {
        symbolsPageIndex = if (symbolsPageIndex == 0) 1 else 0
    }

    fun updateKeySize(size: KeySize) {
        keySize = size
    }

    /** Globe key: commit any pending word, then advance to the next mode. */
    fun onModeSwitch() {
        connection()?.let { finishComposing(it) }
        symbolsPage = false
        symbolsPageIndex = 0
        shifted = false
        mode = mode.next()
    }

    /** Called by the service when a new input field starts. */
    fun onInputStart() {
        clearBuffer()
        symbolsPage = false
        symbolsPageIndex = 0
        shifted = false
    }

    // ---- Helpers --------------------------------------------------------

    private fun finishComposing(ic: InputConnection) {
        ic.finishComposingText()
        clearBuffer()
    }

    private fun clearBuffer() {
        romanBuffer = ""
        candidates = emptyList()
    }

    private fun refreshCandidates() {
        candidates = if (romanBuffer.isEmpty()) {
            emptyList()
        } else {
            repository.suggester.suggest(romanBuffer)
        }
    }
}
