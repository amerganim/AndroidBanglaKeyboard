package com.amerganim.banglakeyboard.engine

import java.text.Normalizer

/**
 * Holds the set of valid Bangla consonant-cluster prefixes used by the smart-
 * conjunct assembler. A cluster of two or more consonants is only joined with a
 * hasanta (i.e. rendered as a juktakkhor) if it is the start of a real conjunct;
 * otherwise the consonants get their inherent vowel and stay separate.
 *
 * The host loads the bundled `juktakkhor.txt` once and calls [setFromList]; the
 * engine itself does no I/O.
 */
object Conjuncts {

    private const val HASANTA = '্'

    /** NFC consonant-glyph sequences (e.g. "কষ", "কষম", "নতর") that are valid
     *  prefixes of some juktakkhor. */
    @Volatile
    var clusterPrefixes: Set<String> = emptySet()
        private set

    fun setFromList(listText: String) {
        clusterPrefixes = buildPrefixes(listText)
    }

    /** Parse the conjunct list into the set of valid cluster prefixes (length ≥ 2). */
    fun buildPrefixes(listText: String): Set<String> {
        val consonantGlyphs: Set<String> = RuleTable.table.values
            .filter { it.kind == Kind.CONSONANT }
            .map { Normalizer.normalize(it.main, Normalizer.Form.NFC) }
            .toSet()

        val prefixes = HashSet<String>()
        for (raw in listText.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            for (token in line.split(Regex("\\s+"))) {
                if (token.isBlank()) continue
                // A juktakkhor is consonants joined by hasanta; split to base glyphs.
                val parts = Normalizer.normalize(token, Normalizer.Form.NFC).split(HASANTA)
                // Skip entries that aren't pure consonant clusters (e.g. ৎ-forms).
                if (parts.size < 2 || parts.any { it !in consonantGlyphs }) continue
                for (len in 2..parts.size) {
                    prefixes.add(parts.subList(0, len).joinToString(""))
                }
            }
        }
        return prefixes
    }
}
