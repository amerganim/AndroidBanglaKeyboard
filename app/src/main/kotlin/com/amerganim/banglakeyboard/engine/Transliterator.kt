package com.amerganim.banglakeyboard.engine

/**
 * Greedy longest-match tokenizer + stateful assembler that converts a romanized
 * Bangla string into Bengali script.
 *
 * The assembler tracks the previous emitted unit:
 *  - a vowel after a consonant becomes a dependent sign (kar); otherwise it is an
 *    independent vowel; the inherent vowel `o` emits nothing after a consonant;
 *  - a consonant after a consonant inserts a hasanta (্) to form a conjunct
 *    (in smart mode, only when the cluster is a real juktakkhor).
 */
object Transliterator {

    /**
     * One assembler input: a known [unit], or raw passthrough [text] (unit == null).
     * [boundary] marks the first segment of a fixed-layout key press, across which a
     * consonant must NOT auto-join the previous one (each key is a discrete letter).
     */
    private class Seg(val unit: Unit?, val text: String, val boundary: Boolean = false)

    /**
     * @param smart when true, two consonants are joined only if the cluster is a real
     *   juktakkhor (see [Conjuncts]); also applies pronunciation-based conjunct
     *   spellings (gg→জ্ঞ, n before চ/জ → ঞ). Falls back to plain joining if the
     *   conjunct list hasn't been loaded.
     */
    fun transliterate(latin: String, smart: Boolean = false): String {
        val useSmart = smart && Conjuncts.clusterPrefixes.isNotEmpty()
        val source = if (useSmart) applyPhoneticSpellings(latin) else latin
        return assemble(tokenize(source), useSmart)
    }

    /**
     * Assemble a list of already-segmented roman tokens — the fixed "Amader" layout.
     * Single-letter keys auto-join into conjuncts (ক then ত → ক্ত), but a pre-composed
     * multi-letter key (the ক্ষ key, token "kSh") is treated as atomic: it neither
     * extends into the next key nor lets the previous one extend into it — so ক্ষ then
     * ম then আ stays ক্ষমা (not ক্ষ্মা) and ল then ক্ষ stays লক্ষ. The ক key then হ key
     * never becomes the "kh" digraph খ. Vowels attach as kar.
     */
    fun transliterateTokens(tokens: List<String>, smart: Boolean = false): String {
        val useSmart = smart && Conjuncts.clusterPrefixes.isNotEmpty()
        val segs = ArrayList<Seg>()
        var prevMulti = false
        for (tok in tokens) {
            val sub = tokenize(tok)
            val multi = sub.size > 1 // a pre-composed key like ক্ষ
            val boundary = multi || prevMulti
            sub.forEachIndexed { i, s ->
                segs.add(if (i == 0 && boundary) Seg(s.unit, s.text, boundary = true) else s)
            }
            prevMulti = multi
        }
        return assemble(segs, useSmart)
    }

    /** Greedy tokenization of a roman string into assembler segments. */
    private fun tokenize(latin: String): List<Seg> {
        val segs = ArrayList<Seg>()
        val n = latin.length
        var i = 0
        while (i < n) {
            var match = matchAt(latin, i, lower = false)
            if (match == null && latin[i] in 'A'..'Z') match = matchAt(latin, i, lower = true)
            if (match == null) {
                val c = latin[i]
                segs.add(Seg(null, if (c in 'A'..'Z') c.lowercaseChar().toString() else c.toString()))
                i++
            } else {
                segs.add(Seg(match.first, ""))
                i += match.second
            }
        }
        return segs
    }

    private fun assemble(segs: List<Seg>, useSmart: Boolean): String {
        val out = StringBuilder()
        var prevConsonant = false // non-smart state
        val pending = ArrayList<String>() // consonant glyphs of the current cluster (smart)

        fun flush() {
            if (pending.isNotEmpty()) {
                out.append(pending.joinToString(RuleTable.HASANTA))
                pending.clear()
            }
        }

        for (seg in segs) {
            val unit = seg.unit
            if (unit == null) {
                if (useSmart) flush()
                out.append(seg.text)
                prevConsonant = false
                continue
            }
            when (unit.kind) {
                Kind.CONSONANT -> {
                    if (useSmart) {
                        if (seg.boundary) {
                            // New fixed-layout key: close the previous cluster instead
                            // of joining it (each key is a discrete letter).
                            flush()
                        } else if (pending.isNotEmpty()) {
                            val candidate = pending.joinToString("") + unit.main
                            // ref (র্ + consonant) always forms; ya-phala (্য) and
                            // ra-phala (্র) attach productively to almost any consonant,
                            // so allow them even when the exact cluster isn't listed
                            // (e.g. ফ্য in ফ্যাসিস্ট). Otherwise require a real juktakkhor.
                            val isRef = pending.size == 1 && pending[0] == RA
                            val isPhala = unit.main == YA || unit.main == RA
                            if (!isRef && !isPhala && candidate !in Conjuncts.clusterPrefixes) flush()
                        }
                        pending.add(unit.main)
                    } else {
                        // Across a key boundary, don't auto-insert a conjunct hasanta.
                        if (prevConsonant && !seg.boundary) out.append(RuleTable.HASANTA)
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
        }
        if (useSmart) flush()
        return out.toString()
    }

    /** ref/ra-phala consonant (র) and ya-phala consonant (য) — productive joins. */
    private const val RA = "র"
    private const val YA = "য"

    // n before চ/ছ/জ/ঝ is pronounced (and written) as ঞ.
    private val N_NASAL = Regex("n(chh|ch|jh|j)")

    // "gg" -> জ্ঞ, but NOT when the first g belongs to the ঙ digraph "Ng"
    // (so ঙ্গ "Ngg…" / ঙ্ঘ "Nggh…" stay ঙ+গ/ঘ instead of becoming জ্ঞ).
    private val GG = Regex("(?<!N)gg")

    /**
     * Pronunciation-based conjunct spellings (smart mode): so বিজ্ঞান can be typed
     * "biggan" and অঞ্চল as "onchol".
     */
    private fun applyPhoneticSpellings(s: String): String {
        var r = GG.replace(s, "jNG") // জ্ঞ
        r = N_NASAL.replace(r) { "NG" + it.groupValues[1] }
        return r
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
