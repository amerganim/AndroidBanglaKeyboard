package com.amerganim.banglakeyboard.ime

/** The input modes the keyboard cycles through via the globe key. */
enum class KeyboardMode(val label: String) {
    ENGLISH("EN"),
    BANGLA_PHONETIC("বাং"),
    BANGLA_FIXED("আমাদের"), // "Amader" — our own phonetic-mnemonic layout
    ;

    fun next(): KeyboardMode {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }
}
