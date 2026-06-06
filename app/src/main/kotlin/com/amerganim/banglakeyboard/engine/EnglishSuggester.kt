package com.amerganim.banglakeyboard.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * English word suggestions: prefix completion over a bundled frequency-ranked word
 * list (google-10000-english), boosted by a per-user learned count and extended by
 * the user's own committed words. Element 0 of [suggest] is always the literal typed
 * text so Space always commits exactly what was typed.
 */
class EnglishSuggester {

    private data class Word(val word: String, val freq: Int)

    @Volatile
    private var words: List<Word> = emptyList() // sorted by word (lowercase)
    private val learned = ConcurrentHashMap<String, Int>()

    /**
     * Load a word-per-line list ranked by frequency (most common first), e.g.
     * google-10000-english. Earlier lines get a higher static weight.
     */
    fun loadWordList(text: String) {
        val merged = ArrayList(words)
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() && it[0] != '#' }.toList()
        val n = lines.size
        lines.forEachIndexed { index, w ->
            merged.add(Word(w.lowercase(), n - index)) // rank-based weight
        }
        merged.sortBy { it.word }
        words = merged
    }

    /** Add a user word so it is suggested in future (deduplicated by value). */
    fun addUserWord(word: String) {
        val w = word.lowercase()
        if (w.isBlank() || words.any { it.word == w }) return
        words = (words + Word(w, USER_WEIGHT)).sortedBy { it.word }
    }

    fun recordUsage(word: String) {
        if (word.isNotBlank()) learned.merge(word.lowercase(), 1, Int::plus)
    }

    /** Forget [word] from learned counts and user-added words. */
    fun forget(word: String) {
        val w = word.lowercase()
        learned.remove(w)
        if (words.any { it.word == w && it.freq == USER_WEIGHT }) {
            words = words.filterNot { it.word == w && it.freq == USER_WEIGHT }
        }
    }

    fun loadLearnedData(tsv: String) {
        for (raw in tsv.lineSequence()) {
            val p = raw.indexOf('\t')
            if (p < 0) continue
            val word = raw.substring(0, p).trim()
            val count = raw.substring(p + 1).trim().toIntOrNull() ?: 0
            if (word.isNotEmpty() && count > 0) learned[word.lowercase()] = count
        }
    }

    fun dumpLearnedData(): String {
        val sb = StringBuilder()
        for ((word, count) in learned) sb.append(word).append('\t').append(count).append('\n')
        return sb.toString()
    }

    /**
     * Suggestions for [prefix]: element 0 is the literal typed text; the rest are
     * dictionary/user words starting with [prefix] (case-insensitive), best-ranked
     * first, de-duplicated.
     */
    fun suggest(prefix: String, max: Int = 9): List<String> {
        if (prefix.isEmpty() || max == 0) return emptyList()
        val out = ArrayList<String>()
        out.add(prefix) // as typed

        val lower = prefix.lowercase()
        val snapshot = words
        var i = lowerBound(snapshot, lower)
        val matches = ArrayList<Pair<String, Int>>()
        while (i < snapshot.size) {
            val w = snapshot[i]
            if (!w.word.startsWith(lower)) break
            if (w.word != lower) {
                val score = w.freq + (learned[w.word] ?: 0) * LEARN_WEIGHT
                matches.add(w.word to score)
            }
            i++
        }
        matches.sortWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
        for ((word, _) in matches) {
            if (out.size >= max) break
            out.add(word)
        }
        return out
    }

    private companion object {
        const val LEARN_WEIGHT = 50
        const val USER_WEIGHT = 5000 // user words rank near the top

        fun lowerBound(words: List<Word>, prefix: String): Int {
            var lo = 0
            var hi = words.size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (words[mid].word < prefix) lo = mid + 1 else hi = mid
            }
            return lo
        }
    }
}
