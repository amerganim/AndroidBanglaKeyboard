package com.amerganim.banglakeyboard.ime

import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.amerganim.banglakeyboard.data.DictionaryRepository
import com.amerganim.banglakeyboard.data.KeySize
import com.amerganim.banglakeyboard.data.Lang
import com.amerganim.banglakeyboard.engine.Transliterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.BreakIterator

/**
 * State holder + input controller. Buffers the in-progress word and drives the
 * host's [InputConnection]. In Bangla mode the buffer is romanized and shown as
 * transliterated composing text; in English mode it is the plain word. On commit
 * the word is learned (suggestions + personal next-word model), and when the buffer
 * is empty the candidate strip shows next-word predictions.
 */
class KeyboardViewModel(
    private val connection: () -> InputConnection?,
    private val repository: DictionaryRepository,
    private val scope: CoroutineScope,
    private val onMicStart: () -> Unit = {},
    private val onMicStop: () -> Unit = {},
) {
    var mode by mutableStateOf(KeyboardMode.BANGLA_PHONETIC)
        private set

    /** True while voice input is actively listening. */
    var listening by mutableStateOf(false)
        private set

    /** The language tag for voice input, based on the current mode. */
    val voiceLanguageTag: String
        get() = if (mode == KeyboardMode.ENGLISH) "en-US" else "bn-BD"

    var shifted by mutableStateOf(false)
        private set

    var symbolsPage by mutableStateOf(false)
        private set

    var symbolsPageIndex by mutableStateOf(0)
        private set

    /** Whether the emoji panel is showing instead of the keys. */
    var emojiPanel by mutableStateOf(false)
        private set

    var keySize by mutableStateOf(KeySize.MEDIUM)
        private set

    /** Suggestions (word completions) or next-word predictions for the strip. */
    var candidates by mutableStateOf<List<String>>(emptyList())
        private set

    private var buffer: String = "" // romanized (Bangla) or plain (English) word
    private var prevWord: String = "" // last committed word, for next-word prediction

    // In private mode (password / no-personalized-learning fields) we never show
    // suggestions, save words, or learn predictions.
    private var privateMode: Boolean = false

    private fun lang() = if (mode == KeyboardMode.ENGLISH) Lang.ENGLISH else Lang.BANGLA

    /** The text currently being composed (transliterated in Bangla mode). */
    private fun composed(): String =
        if (mode == KeyboardMode.ENGLISH) buffer else Transliterator.transliterate(buffer)

    private fun completions(): List<String> =
        if (privateMode) emptyList()
        else if (mode == KeyboardMode.ENGLISH) repository.suggestEnglish(buffer) else repository.suggestBangla(buffer)

    // ---- Key events -----------------------------------------------------

    fun onChar(c: Char) {
        val ic = connection() ?: return
        val isWordChar =
            if (mode == KeyboardMode.ENGLISH) c.isLetter() else Transliterator.isPhoneticInput(c)
        if (isWordChar) {
            buffer += c
            ic.setComposingText(composed(), 1)
            candidates = completions()
        } else {
            finalizeWord(ic)
            ic.commitText(c.toString(), 1)
            prevWord = "" // punctuation breaks the next-word chain
            candidates = emptyList()
        }
        if (shifted) shifted = false
    }

    fun onSpace() {
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText(" ", 1)
        candidates = if (privateMode) emptyList() else repository.predictNext(lang(), prevWord)
    }

    fun onBackspace() {
        val ic = connection() ?: return
        if (buffer.isNotEmpty()) {
            buffer = buffer.dropLast(1)
            if (buffer.isEmpty()) {
                ic.setComposingText("", 1)
                ic.finishComposingText()
                candidates = emptyList()
            } else {
                ic.setComposingText(composed(), 1)
                candidates = completions()
            }
        } else {
            // Delete a whole grapheme cluster so an emoji (a surrogate pair /
            // ZWJ / variation-selector sequence) is removed in one press.
            deleteLastGrapheme(ic)
            prevWord = ""
            candidates = emptyList()
        }
    }

    private fun deleteLastGrapheme(ic: InputConnection) {
        val before = ic.getTextBeforeCursor(MAX_GRAPHEME_LOOKBACK, 0)
        if (before.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }
        val it = BreakIterator.getCharacterInstance()
        it.setText(before.toString())
        val end = it.last()
        val start = it.previous()
        val count = if (start == BreakIterator.DONE) before.length else end - start
        ic.deleteSurroundingText(count.coerceAtLeast(1), 0)
    }

    fun onEnter() {
        val ic = connection() ?: return
        if (buffer.isNotEmpty()) {
            finalizeWord(ic)
            candidates = emptyList()
        } else {
            ic.commitText("\n", 1)
            prevWord = ""
            candidates = emptyList()
        }
    }

    /** Commit a literal character (long-press alternate of a sign key). */
    fun onLiteral(c: Char) {
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText(c.toString(), 1)
        prevWord = ""
        candidates = emptyList()
        if (shifted) shifted = false
    }

    /** The user tapped a suggestion/prediction chip. */
    fun onCandidate(word: String) {
        val ic = connection() ?: return
        ic.commitText("$word ", 1) // replaces any composing region, adds a space
        val prev = prevWord
        buffer = ""
        if (!privateMode) scope.launch { repository.commitWord(lang(), prev, word) }
        prevWord = word
        candidates = if (privateMode) emptyList() else repository.predictNext(lang(), word)
        if (shifted) shifted = false
    }

    /** The user long-pressed a chip — forget that word from history. */
    fun onForgetCandidate(word: String) {
        scope.launch { repository.forget(lang(), word) }
        candidates = candidates.filterNot { it == word }
    }

    fun onShift() {
        shifted = !shifted
    }

    fun toggleSymbols() {
        symbolsPage = !symbolsPage
        symbolsPageIndex = 0
    }

    fun switchSymbolsPage() {
        symbolsPageIndex = if (symbolsPageIndex == 0) 1 else 0
    }

    /** Mic key pressed — start listening (push-to-talk). */
    fun micPressStart() = onMicStart()

    /** Mic key released — stop listening and commit what was heard. */
    fun micPressEnd() = onMicStop()

    fun updateListening(value: Boolean) {
        listening = value
    }

    /** Commit recognized voice text into the field. */
    fun commitVoiceText(text: String) {
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText("$text ", 1)
        prevWord = ""
        candidates = emptyList()
    }

    /** Show/hide the emoji panel. */
    fun toggleEmoji() {
        emojiPanel = !emojiPanel
        candidates = emptyList()
    }

    /** Commit an emoji. */
    fun onEmoji(emoji: String) {
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText(emoji, 1)
        prevWord = ""
        candidates = emptyList()
    }

    fun updateKeySize(size: KeySize) {
        keySize = size
    }

    fun onModeSwitch() {
        connection()?.let { finalizeWord(it) }
        symbolsPage = false
        symbolsPageIndex = 0
        emojiPanel = false
        shifted = false
        prevWord = ""
        candidates = emptyList()
        mode = mode.next()
    }

    fun onInputStart(privateField: Boolean = false) {
        buffer = ""
        prevWord = ""
        candidates = emptyList()
        symbolsPage = false
        symbolsPageIndex = 0
        emojiPanel = false
        shifted = false
        privateMode = privateField
    }

    // ---- Helpers --------------------------------------------------------

    /** Commit the in-progress word (if any), learn it, and remember it as prevWord. */
    private fun finalizeWord(ic: InputConnection) {
        if (buffer.isEmpty()) return
        val word = composed()
        ic.finishComposingText() // commits the composing text, which equals `word`
        val prev = prevWord
        buffer = ""
        candidates = emptyList()
        if (!privateMode) scope.launch { repository.commitWord(lang(), prev, word) }
        prevWord = word
    }

    private companion object {
        // Enough to cover the longest emoji ZWJ sequences (e.g. family emoji).
        const val MAX_GRAPHEME_LOOKBACK = 16
    }
}
