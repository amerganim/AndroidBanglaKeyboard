package com.amerganim.banglakeyboard.engine

/**
 * Sound-alike folding for forgiving word matching.
 *
 * Phonetic typists (especially on a phone, where nobody reaches for Shift) routinely
 * type a letter that *sounds* right but is spelled differently: `jibon` gives জিবন
 * for জীবন, `taka` gives তাকা for টাকা, `shikkha` gives শিক্খা for শিক্ষা. [fold]
 * maps a Bangla string to a coarse key in which such letters collapse together, so a
 * prefix match on folded forms finds the intended dictionary word.
 *
 * Folding is intentionally lossy and only used for matching, never for output.
 */
object Phonetic {

    private const val HASANTA = '্'
    private const val NUKTA = '়'
    private const val ZWJ = '‍'
    private const val ZWNJ = '‌'

    /** Folded stand-in for য় (kept apart from জ/য, which are a different sound). */
    private const val YA_NUKTA = '\u09DF' // য় (precomposed)

    private val single: Map<Char, Char> = mapOf(
        // Vowel length is rarely typed correctly.
        'ী' to 'ি', 'ূ' to 'ু', 'ঈ' to 'ই', 'ঊ' to 'উ',
        // Sibilants শ ষ স sound alike.
        'শ' to 'স', 'ষ' to 'স',
        // Retroflex vs dental (T/t, D/d…): most people never press Shift.
        'ট' to 'ত', 'ঠ' to 'থ', 'ড' to 'দ', 'ঢ' to 'ধ',
        // Nasals.
        'ণ' to 'ন', 'ঞ' to 'ন', 'ঙ' to 'ং',
        // জ / য (j / z) and the precomposed nukta letters.
        'য' to 'জ', YA_NUKTA to YA_NUKTA, '\u09DC' to 'র', '\u09DD' to 'র', // য় ড় ঢ়
        // Khanda-ta is just ত.
        'ৎ' to 'ত',
    )

    /** Signs dropped entirely (people often omit them while typing). */
    private fun isDropped(c: Char) =
        c == HASANTA || c == ZWJ || c == ZWNJ || c == 'ঁ' || c == 'ঃ' || c == NUKTA

    /**
     * @param keepYaPhala when false (the default) ya-phala (্য) is dropped, since it's
     *   mostly silent or heard as a doubling (`dhonnobad` → ধন্যবাদ); when true it is
     *   kept as য (folded to জ), for people who type it as `z`/`y` (`foz` → ফ্যা…).
     */
    fun fold(s: String, keepYaPhala: Boolean = false): String {
        val out = StringBuilder(s.length)
        val n = s.length
        var i = 0
        fun emit(c: Char) {
            // Doubled letters collapse (dhonnobad → ধন্নবাদ matches ধন্যবাদ).
            if (out.isNotEmpty() && out[out.length - 1] == c) return
            out.append(c)
        }
        while (i < n) {
            val c = s[i]
            val next = if (i + 1 < n) s[i + 1] else '\u0000'
            val next2 = if (i + 2 < n) s[i + 2] else '\u0000'
            when {
                // Decomposed nukta letters (NFC keeps these decomposed).
                next == NUKTA && (c == 'ড' || c == 'ঢ') -> { emit('র'); i += 2 }
                next == NUKTA && c == 'য' -> { emit(YA_NUKTA); i += 2 }
                // ক্ষ is typed kkh / kh / ksh: fold to the kh sound.
                c == 'ক' && next == HASANTA && next2 == 'ষ' -> { emit('ক'); emit('খ'); i += 3 }
                // জ্ঞ is pronounced (and typed) like g(g)y.
                c == 'জ' && next == HASANTA && next2 == 'ঞ' -> { emit('গ'); i += 3 }
                // Ya-phala (্য) is mostly silent / a doubling — drop it.
                !keepYaPhala && c == HASANTA && next == 'য' && next2 != NUKTA -> { i += 2 }
                isDropped(c) -> i++
                else -> { emit(single[c] ?: c); i++ }
            }
        }
        return out.toString()
    }
}
