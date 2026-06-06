package com.amerganim.banglakeyboard.engine

/**
 * Greedy longest-match tokenizer + stateful assembler that converts a romanized
 * Bangla string into Bengali script (UTF-8 in, UTF-8 out).
 *
 * Direct Kotlin port of `bnphonetic::Transliterate` from the Windows keyboard.
 * The assembler tracks whether the previous emitted unit was a consonant:
 *  - a vowel after a consonant becomes a dependent sign (kar); otherwise it is
 *    an independent vowel; the inherent vowel `o` emits nothing after a consonant;
 *  - a consonant after a consonant inserts a hasanta (্) to form a conjunct.
 *
 * Input keys are all ASCII, so character indexing matches the C++ byte indexing.
 */
object Transliterator {

    fun transliterate(latin: String): String {
        val table = RuleTable.table
        val out = StringBuilder()
        var prevConsonant = false
        val n = latin.length

        var i = 0
        while (i < n) {
            var unit: Unit? = null
            var matchedLen = 0

            // Greedy: try the longest key first, down to a single character.
            var len = RuleTable.MAX_KEY_LEN
            while (len >= 1) {
                if (i + len <= n) {
                    val candidate = table[latin.substring(i, i + len)]
                    if (candidate != null) {
                        unit = candidate
                        matchedLen = len
                        break
                    }
                }
                len--
            }

            if (unit == null) {
                // Unknown character (space, punctuation, untranslated letter):
                // pass it through verbatim and reset the consonant context.
                out.append(latin[i])
                prevConsonant = false
                i++
                continue
            }

            when (unit.kind) {
                Kind.CONSONANT -> {
                    if (prevConsonant) out.append(RuleTable.HASANTA) // form a conjunct
                    out.append(unit.main)
                    prevConsonant = true
                }
                Kind.VOWEL -> {
                    // After a consonant a vowel becomes a dependent sign (empty
                    // for the inherent vowel); otherwise it is an independent vowel.
                    out.append(if (prevConsonant) unit.kar else unit.main)
                    prevConsonant = false
                }
                Kind.DIRECT -> {
                    out.append(unit.main)
                    prevConsonant = false
                }
            }

            i += matchedLen
        }

        return out.toString()
    }

    /** Whether [c] is a character the phonetic engine buffers/translates. */
    fun isPhoneticInput(c: Char): Boolean {
        if (c in 'a'..'z') return true
        if (c in 'A'..'Z') return true
        if (c in '0'..'9') return true
        return c == '^' || c == ':' || c == '.' || c == '`'
    }
}
