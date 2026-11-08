package com.amerganim.banglakeyboard.ime

/** Text-editing shortcuts reachable by long-pressing A / X / C / V / Z. */
enum class EditAction { SELECT_ALL, CUT, COPY, PASTE, UNDO }

/**
 * What the keyboard needs from its hosting service beyond the input connection:
 * the system clipboard, the clipboard history, persistence of recent emojis and
 * short notices. The service implements it; tests and previews can use [None].
 */
interface KeyboardHost {
    /** Current plain-text clipboard contents, or null if empty / not text. */
    fun readClipboard(): String?

    /** Put [text] on the system clipboard. */
    fun writeClipboard(text: String)

    /** Re-read the clipboard history (also captures a clip copied while we were idle). */
    fun refreshClips()
    fun pinClip(text: String, pinned: Boolean)
    fun removeClip(text: String)
    fun clearClips()

    fun saveRecentEmojis(emojis: List<String>)

    /** Show a short message (a string resource id). */
    fun notice(resId: Int)

    object None : KeyboardHost {
        override fun readClipboard(): String? = null
        override fun writeClipboard(text: String) {}
        override fun refreshClips() {}
        override fun pinClip(text: String, pinned: Boolean) {}
        override fun removeClip(text: String) {}
        override fun clearClips() {}
        override fun saveRecentEmojis(emojis: List<String>) {}
        override fun notice(resId: Int) {}
    }
}
