package com.amerganim.banglakeyboard.ime

import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
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
    private val onKeyFeedback: () -> Unit = {},
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

    /** The editor's requested Enter action (Search/Done/Next/Go/Send) or NONE. */
    var imeAction by mutableStateOf(EditorInfo.IME_ACTION_NONE)
        private set

    fun updateImeAction(action: Int) { imeAction = action }

    // The in-progress word as a list of key-press tokens. For phonetic/English each
    // token is one character; for the Amader (fixed) layout each token is a whole
    // roman key (e.g. "kh", "T") so adjacent keys are NOT greedily re-tokenized.
    private val tokens = ArrayList<String>()
    private var prevWord: String = "" // last committed word, for next-word prediction
    private var keySoundEnabled: Boolean = false
    private var smartConjunct: Boolean = false

    private fun bufferEmpty() = tokens.isEmpty()
    private fun romanString() = tokens.joinToString("")

    fun updateKeySound(enabled: Boolean) { keySoundEnabled = enabled }
    fun updateSmartConjunct(enabled: Boolean) { smartConjunct = enabled }

    /** Play a key-press sound if enabled. Called from key handlers. */
    private fun feedback() { if (keySoundEnabled) onKeyFeedback() }

    // Hide the suggestion bar entirely (password fields only).
    private var noSuggest: Boolean = false

    // Don't save/learn words or predictions (passwords + fields opting out of
    // personalized learning / suggestions). Suggestions can still be shown.
    private var noLearn: Boolean = false

    // The current field's input type, for auto-capitalization (getCursorCapsMode).
    private var inputType: Int = 0

    private fun lang() = if (mode == KeyboardMode.ENGLISH) Lang.ENGLISH else Lang.BANGLA

    /**
     * Auto-capitalization: in English mode only, set the shift state from the field's
     * caps mode at the cursor (start of field / after a sentence, per the field's
     * flags). Bangla modes are skipped — there shift selects a different letter.
     */
    private fun refreshAutoCaps() {
        if (mode != KeyboardMode.ENGLISH || !bufferEmpty()) return
        val capFlags = inputType and CAP_FLAGS
        if (capFlags == 0) { // field didn't ask for any auto-capitalization
            shifted = false
            return
        }
        // Compute caps from the text before the cursor (more reliable across editors,
        // incl. Compose, than InputConnection.getCursorCapsMode at input-start).
        val before = connection()?.getTextBeforeCursor(64, 0) ?: ""
        shifted = TextUtils.getCapsMode(before, before.length, capFlags) != 0
    }

    /** The text currently being composed (transliterated in Bangla modes). */
    private fun composed(): String = when (mode) {
        KeyboardMode.ENGLISH -> romanString()
        // Amader: assemble the discrete key tokens (no greedy re-tokenization).
        KeyboardMode.BANGLA_FIXED -> Transliterator.transliterateTokens(tokens, smartConjunct)
        else -> Transliterator.transliterate(romanString(), smartConjunct)
    }

    private fun completions(): List<String> = when {
        noSuggest -> emptyList()
        mode == KeyboardMode.ENGLISH -> repository.suggestEnglish(romanString())
        // Amader: the composed Bangla can't be re-derived from roman, so match by it.
        mode == KeyboardMode.BANGLA_FIXED -> repository.suggestBanglaByText(composed())
        else -> repository.suggestBangla(romanString(), smartConjunct)
    }

    private fun predictions(prev: String): List<String> =
        if (noSuggest) emptyList() else repository.predictNext(lang(), prev)

    // ---- Key events -----------------------------------------------------

    fun onChar(c: Char) {
        feedback()
        val ic = connection() ?: return
        val isWordChar =
            if (mode == KeyboardMode.ENGLISH) c.isLetter() else Transliterator.isPhoneticInput(c)
        if (isWordChar) {
            tokens.add(c.toString())
            ic.setComposingText(composed(), 1)
            candidates = completions()
        } else {
            finalizeWord(ic)
            ic.commitText(c.toString(), 1)
            prevWord = "" // punctuation breaks the next-word chain
            candidates = emptyList()
        }
        if (shifted) shifted = false
        refreshAutoCaps() // re-engage caps after sentence punctuation
    }

    /**
     * A fixed-layout (Amader) key: appends a whole roman [token] (e.g. "k", "kh",
     * "T") as one discrete unit, then assembles via the engine for kars/conjuncts.
     */
    fun onToken(token: String) {
        feedback()
        val ic = connection() ?: return
        tokens.add(token)
        ic.setComposingText(composed(), 1)
        candidates = completions()
        if (shifted) shifted = false
    }

    fun onSpace() {
        feedback()
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText(" ", 1)
        candidates = predictions(prevWord)
        refreshAutoCaps()
    }

    fun onBackspace() {
        feedback()
        val ic = connection() ?: return
        if (tokens.isNotEmpty()) {
            tokens.removeAt(tokens.size - 1) // remove one key-press
            if (tokens.isEmpty()) {
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
        refreshAutoCaps()
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
        feedback()
        val ic = connection() ?: return
        if (!bufferEmpty()) finalizeWord(ic) // commit the in-progress word first
        if (imeAction != EditorInfo.IME_ACTION_NONE) {
            // Field declared an explicit action (Search/Done/Next/Go/Send): do it.
            ic.performEditorAction(imeAction)
        } else {
            // No explicit action — send a real Enter key so the field decides:
            // single-line search/login boxes submit, multi-line inserts a newline.
            // (A literal "\n" is ignored by single-line fields like Google search.)
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            prevWord = ""
        }
        candidates = emptyList()
        refreshAutoCaps() // a newline starts a new line/sentence
    }

    /** Commit a literal character (long-press alternate of a sign key). */
    fun onLiteral(c: Char) {
        feedback()
        val ic = connection() ?: return
        finalizeWord(ic)
        ic.commitText(c.toString(), 1)
        prevWord = ""
        candidates = emptyList()
        if (shifted) shifted = false
        refreshAutoCaps()
    }

    /** The user tapped a suggestion/prediction chip. */
    fun onCandidate(word: String) {
        feedback()
        val ic = connection() ?: return
        ic.commitText("$word ", 1) // replaces any composing region, adds a space
        val prev = prevWord
        tokens.clear()
        if (!noLearn) scope.launch { repository.commitWord(lang(), prev, word) }
        prevWord = word
        candidates = predictions(word)
        if (shifted) shifted = false
        refreshAutoCaps()
    }

    /** The user long-pressed a chip — forget that word from history. */
    fun onForgetCandidate(word: String) {
        scope.launch { repository.forget(lang(), word) }
        candidates = candidates.filterNot { it == word }
    }

    fun onShift() {
        feedback()
        shifted = !shifted
    }

    fun toggleSymbols() {
        feedback()
        symbolsPage = !symbolsPage
        symbolsPageIndex = 0
    }

    fun switchSymbolsPage() {
        feedback()
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
        feedback()
        emojiPanel = !emojiPanel
        candidates = emptyList()
    }

    /** Commit an emoji. */
    fun onEmoji(emoji: String) {
        feedback()
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
        feedback()
        connection()?.let { finalizeWord(it) }
        symbolsPage = false
        symbolsPageIndex = 0
        emojiPanel = false
        shifted = false
        prevWord = ""
        candidates = emptyList()
        mode = mode.next()
        refreshAutoCaps() // engage caps if we just switched into English
    }

    /**
     * Called when the text selection/cursor changes. If the user moved the cursor
     * away from the word we're composing, abandon the buffer so new typing starts
     * fresh at the new position (instead of jumping back to the old word).
     */
    fun onSelectionChanged(newSelStart: Int, newSelEnd: Int, candStart: Int, candEnd: Int) {
        if (!bufferEmpty()) {
            val cursorInComposing =
                candStart >= 0 && newSelStart == newSelEnd && newSelStart in candStart..candEnd
            if (!cursorInComposing) {
                connection()?.finishComposingText() // keep the already-composed text in place
                tokens.clear()
                prevWord = ""
                candidates = emptyList()
            }
        }
        refreshAutoCaps() // caps state follows the cursor position
    }

    fun onInputStart(noLearning: Boolean = false, noSuggestions: Boolean = false, fieldInputType: Int = 0) {
        tokens.clear()
        prevWord = ""
        candidates = emptyList()
        symbolsPage = false
        symbolsPageIndex = 0
        emojiPanel = false
        shifted = false
        noLearn = noLearning
        noSuggest = noSuggestions
        inputType = fieldInputType
        refreshAutoCaps() // capitalize the first letter of an empty/sentence-start field
    }

    // ---- Helpers --------------------------------------------------------

    /** Commit the in-progress word (if any), learn it, and remember it as prevWord. */
    private fun finalizeWord(ic: InputConnection) {
        if (bufferEmpty()) return
        val word = composed()
        ic.finishComposingText() // commits the composing text, which equals `word`
        val prev = prevWord
        tokens.clear()
        candidates = emptyList()
        if (!noLearn) scope.launch { repository.commitWord(lang(), prev, word) }
        prevWord = word
    }

    private companion object {
        // Enough to cover the longest emoji ZWJ sequences (e.g. family emoji).
        const val MAX_GRAPHEME_LOOKBACK = 16

        // The field's auto-capitalization request bits.
        const val CAP_FLAGS = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
            InputType.TYPE_TEXT_FLAG_CAP_WORDS or
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    }
}
