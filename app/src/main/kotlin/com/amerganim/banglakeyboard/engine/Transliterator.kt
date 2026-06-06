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
        val out = StringBuilder()
        var prevConsonant = false
        val n = latin.length

        var i = 0
        while (i < n) {
            // First try a case-sensitive match (so the scheme's capital-specific
            // letters win: T=ট vs t=ত, Ng=ঙ vs ng=ং, etc.).
            var match = matchAt(latin, i, lower = false)
            // If a capital has no mapping, fall back to its lowercase Bangla form
            // instead of emitting a stray English letter (e.g. `A` -> আ, `M` -> ম).
            if (match == null && latin[i] in 'A'..'Z') {
                match = matchAt(latin, i, lower = true)
            }

            if (match == null) {
                // Unknown character (space, punctuation, untranslated letter):
                // pass it through and reset the consonant context. Lowercase a
                // stray capital so no uppercase English leaks into Bangla text.
                val c = latin[i]
                out.append(if (c in 'A'..'Z') c.lowercaseChar() else c)
                prevConsonant = false
                i++
                continue
            }

            val unit = match.first
            val matchedLen = match.second

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

    /**
     * Greedy longest-match lookup at [i]. When [lower] is true the candidate is
     * lowercased before lookup (the uppercase-fallback pass).
     */
    private fun matchAt(s: String, i: Int, lower: Boolean): Pair<Unit, Int>? {
        val n = s.length
        var len = RuleTable.MAX_KEY_LEN
        while (len >= 1) {
            if (i + len <= n) {
                var key = s.substring(i, i + len)
                if (lower) key = key.lowercase()
                val u = RuleTable.table[key]
                if (u != null) return u to len
            }
            len--
        }
        return null
    }

    /** Whether [c] is a character the phonetic engine buffers/translates. */
    fun isPhoneticInput(c: Char): Boolean {
        if (c in 'a'..'z') return true
        if (c in 'A'..'Z') return true
        if (c in '0'..'9') return true
        return c == '^' || c == ':' || c == '.' || c == '`'
    }
}
