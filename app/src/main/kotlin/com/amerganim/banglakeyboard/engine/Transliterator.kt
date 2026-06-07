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

    /**
     * @param smart when true, two consonants are joined into a conjunct only if the
     *   cluster is a real juktakkhor (see [Conjuncts]); otherwise each consonant
     *   keeps its inherent vowel. So `zkhn` -> যখন but `kSh` -> ক্ষ. Falls back to
     *   normal joining if the conjunct list hasn't been loaded yet.
     */
    fun transliterate(latin: String, smart: Boolean = false): String {
        val useSmart = smart && Conjuncts.clusterPrefixes.isNotEmpty()
        val out = StringBuilder()
        var prevConsonant = false // non-smart state
        val pending = ArrayList<String>() // consonant glyphs of the current cluster (smart)
        val n = latin.length

        fun flush() {
            if (pending.isNotEmpty()) {
                out.append(pending.joinToString(RuleTable.HASANTA))
                pending.clear()
            }
        }

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
                if (useSmart) flush()
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
                    if (useSmart) {
                        if (pending.isNotEmpty()) {
                            val candidate = pending.joinToString("") + unit.main
                            // ref (র্ + consonant) always forms; otherwise only if
                            // the cluster is the start of a real juktakkhor.
                            val isRef = pending.size == 1 && pending[0] == RA
                            if (!isRef && candidate !in Conjuncts.clusterPrefixes) flush()
                        }
                        pending.add(unit.main)
                    } else {
                        if (prevConsonant) out.append(RuleTable.HASANTA) // conjunct
                        out.append(unit.main)
                        prevConsonant = true
                    }
                }
                Kind.VOWEL -> {
                    if (useSmart) {
                        if (pending.isNotEmpty()) {
                            flush()
                            out.append(unit.kar) // kar attaches to the cluster
                        } else {
                            out.append(unit.main) // independent vowel
                        }
                    } else {
                        // After a consonant a vowel becomes a dependent sign (empty
                        // for the inherent vowel); otherwise an independent vowel.
                        out.append(if (prevConsonant) unit.kar else unit.main)
                        prevConsonant = false
                    }
                }
                Kind.DIRECT -> {
                    if (useSmart) flush()
                    out.append(unit.main)
                    prevConsonant = false
                }
            }

            i += matchedLen
        }

        if (useSmart) flush()
        return out.toString()
    }

    /** The ref consonant (র); ref always forms before another consonant. */
    private const val RA = "র"

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
