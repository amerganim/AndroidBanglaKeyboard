package com.amerganim.banglakeyboard.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * Provides Bangla word suggestions for a romanized prefix, ranked by a static
 * dictionary frequency plus a per-user "learned" count (words the user picks
 * often float to the top).
 *
 * Kotlin port of `bnphonetic::Suggester`. The engine performs no I/O: the host
 * reads the bundled dictionary and the user's learned data as text and passes it
 * in via [loadDictionaryData] / [loadWordList] / [loadLearnedData], and reads it
 * back via [dumpLearnedData] to persist.
 */
class Suggester {

    /** roman key -> Bangla word, with a static frequency weight. */
    private data class Entry(val key: String, val bangla: String, val freq: Int)

    /** Bangla word (matched by Bangla prefix) with a static frequency weight. */
    private data class Word(val bangla: String, val freq: Int)

    // These are read on the UI thread by suggest() while load() populates them on
    // a background thread, so they are swapped atomically (build a new list, then
    // assign) rather than mutated in place. learned is a concurrent map.
    @Volatile
    private var entries: List<Entry> = BUILTIN.map { Entry(it.first, it.second, it.third) }

    @Volatile
    private var words: List<Word> = emptyList() // sorted by `bangla`

    private val learned = ConcurrentHashMap<String, Int>()

    /**
     * Add dictionary entries from TSV text: `roman<TAB>bangla<TAB>freq` per line
     * (freq optional, default 30). Matched by romanized prefix.
     */
    fun loadDictionaryData(tsv: String) {
        val added = ArrayList<Entry>()
        for (raw in tsv.lineSequence()) {
            val t = raw.trim()
            if (t.isEmpty() || t[0] == '#') continue
            val p1 = t.indexOf('\t')
            if (p1 < 0) continue
            val p2 = t.indexOf('\t', p1 + 1)
            val key = t.substring(0, p1).trim()
            val bangla = (if (p2 < 0) t.substring(p1 + 1) else t.substring(p1 + 1, p2)).trim()
            var freq = 30
            if (p2 >= 0) {
                val f = t.substring(p2 + 1).trim()
                if (f.isNotEmpty()) freq = f.toIntOrNull() ?: 30
            }
            if (key.isNotEmpty() && bangla.isNotEmpty()) added.add(Entry(key, bangla, freq))
        }
        entries = entries + added // atomic swap
    }

    /**
     * Add a Bangla word list from TSV text: `bangla<TAB>freq` per line (default
     * weight 8). Only a purely-numeric second field is treated as a frequency,
     * so a raw `bangla<TAB>phonemes` lexicon also loads. Matched by Bangla prefix.
     */
    fun loadWordList(tsv: String) {
        val merged = ArrayList(words)
        for (raw in tsv.lineSequence()) {
            val t = raw.trim()
            if (t.isEmpty() || t[0] == '#') continue
            val p = t.indexOf('\t')
            val bangla = (if (p < 0) t else t.substring(0, p)).trim()
            var freq = 8
            if (p >= 0) {
                val f = t.substring(p + 1).trim()
                if (f.isNotEmpty() && f.all { it in '0'..'9' }) freq = f.toIntOrNull() ?: 8
            }
            if (bangla.isNotEmpty()) merged.add(Word(bangla, freq))
        }
        merged.sortBy { it.bangla }
        words = merged // atomic swap
    }

    /** Load learned counts from TSV text: `bangla<TAB>count` per line. */
    fun loadLearnedData(tsv: String) {
        for (raw in tsv.lineSequence()) {
            val t = raw.trim()
            if (t.isEmpty() || t[0] == '#') continue
            val p = t.indexOf('\t')
            if (p < 0) continue
            val bangla = t.substring(0, p).trim()
            val count = t.substring(p + 1).trim().toIntOrNull() ?: 0
            if (bangla.isNotEmpty() && count > 0) learned[bangla] = count
        }
    }

    /** Dump learned counts as TSV text: `bangla<TAB>count` per line. */
    fun dumpLearnedData(): String {
        val sb = StringBuilder()
        for ((bangla, count) in learned) {
            sb.append(bangla).append('\t').append(count).append('\n')
        }
        return sb.toString()
    }

    /** Record that the user committed [bangla] (boosts it in future ranking). */
    fun recordUsage(bangla: String) {
        if (bangla.isNotEmpty()) learned.merge(bangla, 1, Int::plus)
    }

    /**
     * Suggestions for [prefix]: element 0 is always the literal transliteration
     * of [prefix]; the rest are dictionary/word-list entries whose romanization
     * (or Bangla transliteration) starts with [prefix], best-ranked first,
     * de-duplicated.
     */
    fun suggest(prefix: String, maxResults: Int = 9): List<String> {
        val out = ArrayList<String>()
        if (prefix.isEmpty() || maxResults == 0) return out

        val literal = Transliterator.transliterate(prefix) // element 0: as typed
        out.add(literal)

        val lowerPrefix = prefix.lowercase()

        // Snapshot the swappable lists so a concurrent load() can't mutate them
        // mid-read.
        val entriesSnapshot = entries
        val wordsSnapshot = words

        // Best static score per candidate Bangla word.
        val cand = HashMap<String, Int>()

        for (e in entriesSnapshot) {
            if (!keyHasPrefix(e.key, lowerPrefix)) continue
            val cur = cand[e.bangla]
            if (cur == null || e.freq > cur) cand[e.bangla] = e.freq
        }

        // Bangla-prefix completion over the sorted word list.
        if (literal.isNotEmpty()) {
            var i = lowerBound(wordsSnapshot, literal)
            while (i < wordsSnapshot.size) {
                val w = wordsSnapshot[i]
                if (!w.bangla.startsWith(literal)) break
                val cur = cand[w.bangla]
                if (cur == null || w.freq > cur) cand[w.bangla] = w.freq
                i++
            }
        }

        val scored = ArrayList<Pair<String, Int>>(cand.size)
        for ((bangla, base) in cand) {
            if (bangla == literal) continue // already element 0
            val score = base + (learned[bangla] ?: 0) * LEARN_WEIGHT
            scored.add(bangla to score)
        }
        // Higher score first; ties broken lexicographically for determinism.
        scored.sortWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

        for ((bangla, _) in scored) {
            if (out.size >= maxResults) break
            out.add(bangla)
        }
        return out
    }

    private companion object {
        /** Each committed use is worth this much static-frequency weight. */
        const val LEARN_WEIGHT = 40

        /** Whether [key] starts with [lowerPrefix], comparing case-insensitively. */
        fun keyHasPrefix(key: String, lowerPrefix: String): Boolean {
            if (key.length < lowerPrefix.length) return false
            for (i in lowerPrefix.indices) {
                if (key[i].lowercaseChar() != lowerPrefix[i]) return false
            }
            return true
        }

        /** First index in the sorted [words] whose `bangla` is >= [prefix]. */
        fun lowerBound(words: List<Word>, prefix: String): Int {
            var lo = 0
            var hi = words.size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (words[mid].bangla < prefix) lo = mid + 1 else hi = mid
            }
            return lo
        }

        // Always-available fallback seed list (key, bangla, freq).
        val BUILTIN = listOf(
            Triple("ami", "আমি", 100), Triple("amar", "আমার", 95),
            Triple("amake", "আমাকে", 70), Triple("amader", "আমাদের", 70),
            Triple("tumi", "তুমি", 95), Triple("tomar", "তোমার", 85),
            Triple("tomake", "তোমাকে", 65), Triple("apni", "আপনি", 80),
            Triple("apnar", "আপনার", 70), Triple("se", "সে", 85),
            Triple("tar", "তার", 80), Triple("tara", "তারা", 70),
            Triple("eta", "এটা", 70), Triple("ei", "এই", 80),
            Triple("ki", "কি", 90), Triple("kothay", "কোথায়", 70),
            Triple("keno", "কেন", 75), Triple("kemon", "কেমন", 75),
            Triple("ke", "কে", 70), Triple("koto", "কত", 65),
            Triple("bhalo", "ভালো", 90), Triple("bhalobasa", "ভালোবাসা", 70),
            Triple("achhi", "আছি", 70), Triple("achhe", "আছে", 75),
            Triple("kora", "করা", 65), Triple("hobe", "হবে", 70),
            Triple("dhonnobad", "ধন্যবাদ", 80), Triple("aj", "আজ", 65),
            Triple("ekhon", "এখন", 65), Triple("bangla", "বাংলা", 85),
            Triple("bangladesh", "বাংলাদেশ", 75), Triple("manush", "মানুষ", 65),
            Triple("bondhu", "বন্ধু", 65), Triple("baba", "বাবা", 65),
            Triple("ma", "মা", 70), Triple("bhai", "ভাই", 65),
            Triple("somoy", "সময়", 65), Triple("kaj", "কাজ", 65),
            Triple("taka", "টাকা", 60), Triple("nam", "নাম", 60),
            Triple("kotha", "কথা", 65), Triple("jibon", "জীবন", 60),
            Triple("sundor", "সুন্দর", 70), Triple("onek", "অনেক", 70),
            Triple("sob", "সব", 65), Triple("kichu", "কিছু", 65),
        )
    }
}
