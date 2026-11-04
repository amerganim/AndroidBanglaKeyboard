package com.amerganim.banglakeyboard.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * Provides Bangla word suggestions for a romanized prefix, ranked by a static
 * dictionary frequency plus a per-user "learned" count (words the user picks
 * often float to the top).
 *
 * Matching happens in three passes:
 *  1. **roman keys** from the curated dictionary (`ami` → আমি);
 *  2. **exact Bangla prefix** of the transliteration over the word list;
 *  3. **sound-alike ("fuzzy") prefix**: both sides are folded with [Phonetic.fold],
 *     which merges letters phonetic typists routinely confuse (ি/ী, ু/ূ, শ/ষ/স,
 *     ত/ট, দ/ড, ন/ণ, জ/য, র/ড়, ya-phala, doubled consonants…), so `jibon` still
 *     offers জীবন and `taka` offers টাকা. Fuzzy hits rank below exact ones, except a
 *     whole-word sound-alike, which is very likely the intended spelling and is
 *     promoted.
 *
 * The engine performs no I/O: the host reads the bundled dictionary and the user's
 * learned data as text and passes it in via [loadDictionaryData] / [loadWordList] /
 * [loadLearnedData], and reads it back via [dumpLearnedData] to persist.
 */
class Suggester {

    /** roman key -> Bangla word, with a static frequency weight. */
    private data class Entry(val key: String, val bangla: String, val freq: Int)

    /** Bangla word (matched by Bangla prefix) with a static frequency weight. [user] marks words the user taught us. */
    private class Word(val bangla: String, val freq: Int, val user: Boolean = false)

    /** A word filed under one of its sound-alike folds. */
    private class Folded(val fold: String, val word: Word)

    /** A searchable set of words: sorted by Bangla, and filed by sound-alike fold. */
    private class Index(val exact: List<Word>, val folded: List<Folded>) {
        companion object {
            val EMPTY = Index(emptyList(), emptyList())

            fun of(sortedWords: List<Word>): Index {
                val folded = ArrayList<Folded>(sortedWords.size + sortedWords.size / 8)
                for (w in sortedWords) {
                    val a = Phonetic.fold(w.bangla)
                    folded.add(Folded(a, w))
                    // A word with ya-phala is filed twice: ya-phala dropped, and kept as য.
                    val b = Phonetic.fold(w.bangla, keepYaPhala = true)
                    if (b != a) folded.add(Folded(b, w))
                }
                folded.sortBy { it.fold }
                return Index(sortedWords, folded)
            }
        }
    }

    // These are read on the UI thread by suggest() while load() populates them on
    // a background thread, so they are swapped atomically (build a new list, then
    // assign) rather than mutated in place. learned is a concurrent map.
    @Volatile
    private var entries: List<Entry> = BUILTIN.map { Entry(it.first, it.second, it.third) }

    @Volatile
    private var words: List<Word> = emptyList() // sorted by `bangla`

    // Search indexes over `words`. `top` holds only frequent and user words and is
    // always scanned in full, so common words are found even for a 1-letter prefix;
    // `all` (the whole lexicon) is scanned with a small cap to bound per-key work.
    @Volatile
    private var all: Index = Index.EMPTY

    @Volatile
    private var top: Index = Index.EMPTY

    private val learned = ConcurrentHashMap<String, Int>()

    /** Replace the word list and rebuild both search indexes (each swapped atomically). */
    private fun assignWords(newWords: List<Word>) {
        words = newWords
        all = Index.of(newWords)
        top = Index.of(newWords.filter { it.user || it.freq >= TOP_MIN_FREQ })
    }

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
     * A word already present keeps the higher of its weights, so a small
     * high-frequency list can be layered over the big lexicon.
     */
    fun loadWordList(tsv: String) {
        val byWord = LinkedHashMap<String, Word>(words.size * 2 + 16)
        for (w in words) byWord[w.bangla] = w
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
            if (bangla.isEmpty()) continue
            val cur = byWord[bangla]
            if (cur == null || cur.user || freq > cur.freq) byWord[bangla] = Word(bangla, freq)
        }
        assignWords(byWord.values.sortedBy { it.bangla }) // atomic swap (both indexes)
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
     * Add a user-typed word so it is suggested in future (deduplicated). It starts
     * with a modest weight and climbs with each use (see [recordUsage]), so a
     * one-off typo can't permanently outrank the correctly spelled word.
     */
    fun addUserWord(bangla: String) {
        if (bangla.isBlank() || words.any { it.bangla == bangla }) return
        assignWords((words + Word(bangla, USER_WEIGHT, user = true)).sortedBy { it.bangla })
    }

    /** Forget [bangla] from learned counts and user-added words. */
    fun forget(bangla: String) {
        learned.remove(bangla)
        if (words.any { it.bangla == bangla && it.user }) {
            assignWords(words.filterNot { it.bangla == bangla && it.user })
        }
    }

    /**
     * Drop everything learned from the user — usage counts and user-added words —
     * leaving the bundled dictionary untouched.
     */
    fun clearLearned() {
        learned.clear()
        if (words.any { it.user }) {
            assignWords(words.filterNot { it.user })
        }
    }

    /**
     * Suggestions for [prefix]: element 0 is always the literal transliteration
     * of [prefix]; the rest are dictionary/word-list entries whose romanization
     * (or Bangla transliteration) starts with [prefix], best-ranked first,
     * de-duplicated.
     */
    fun suggest(prefix: String, smart: Boolean = false, maxResults: Int = 9): List<String> {
        val out = ArrayList<String>()
        if (prefix.isEmpty() || maxResults == 0) return out

        val literal = Transliterator.transliterate(prefix, smart) // element 0: as typed
        out.add(literal)

        val lowerPrefix = prefix.lowercase()
        val cand = HashMap<String, Int>() // best static score per candidate

        for (e in entries) { // snapshot read of the @Volatile list
            if (!keyHasPrefix(e.key, lowerPrefix)) continue
            // A complete roman-key match is what the user is typing right now.
            val score = if (e.key.length == lowerPrefix.length) e.freq + WHOLE_WORD_BONUS else e.freq
            offer(cand, e.bangla, score)
        }
        addWordMatches(literal, cand)
        return rank(literal, cand, out, maxResults)
    }

    /**
     * Suggestions for an already-formed Bangla [prefix] (used by the Amader layout,
     * whose composed text can't be re-derived from roman): element 0 is the prefix
     * itself, followed by word-list/user words that start with it (exactly or
     * sound-alike).
     */
    fun suggestByBangla(prefix: String, maxResults: Int = 9): List<String> {
        val out = ArrayList<String>()
        if (prefix.isEmpty() || maxResults == 0) return out
        out.add(prefix)
        val cand = HashMap<String, Int>()
        addWordMatches(prefix, cand)
        return rank(prefix, cand, out, maxResults)
    }

    /** Exact-prefix, then sound-alike-prefix completions of [literal] from the word list. */
    private fun addWordMatches(literal: String, cand: HashMap<String, Int>) {
        if (literal.isEmpty()) return
        // Keep a typed ya-phala as য so it lines up with the ya-phala-kept filing.
        val litFold = Phonetic.fold(literal, keepYaPhala = true)
        addIndexMatches(top, literal, litFold, cand, TOP_SCAN)
        addIndexMatches(all, literal, litFold, cand, LEXICON_SCAN)
    }

    private fun addIndexMatches(
        index: Index,
        literal: String,
        litFold: String,
        cand: HashMap<String, Int>,
        maxScan: Int,
    ) {
        // Exact Bangla-prefix completion over the sorted word list.
        val exact = index.exact
        var i = lowerBound(exact, literal)
        var scanned = 0
        while (i < exact.size && scanned < maxScan) {
            val w = exact[i]
            if (!w.bangla.startsWith(literal)) break
            offer(cand, w.bangla, w.freq)
            i++
            scanned++
        }

        // Sound-alike completion: tolerates a wrong vowel length, sibilant, retroflex,
        // missing/wrong conjunct, etc. Demoted below exact matches, except a whole-word
        // sound-alike (very likely the intended spelling), which is promoted.
        if (litFold.isEmpty()) return
        val folded = index.folded
        i = foldLowerBound(folded, litFold)
        scanned = 0
        while (i < folded.size && scanned < maxScan) {
            val f = folded[i]
            if (!f.fold.startsWith(litFold)) break
            val w = f.word
            if (w.bangla != literal && !w.bangla.startsWith(literal)) {
                val whole = f.fold.length == litFold.length
                val score = if (whole) w.freq + WHOLE_WORD_BONUS else w.freq * 3 / 4
                offer(cand, w.bangla, score)
            }
            i++
            scanned++
        }
    }

    /** Score, sort and append candidates (excluding [literal], already element 0). */
    private fun rank(
        literal: String,
        cand: HashMap<String, Int>,
        out: ArrayList<String>,
        maxResults: Int,
    ): List<String> {
        val scored = ArrayList<Pair<String, Int>>(cand.size)
        for ((bangla, base) in cand) {
            if (bangla == literal) continue // already element 0
            scored.add(bangla to base + (learned[bangla] ?: 0) * LEARN_WEIGHT)
        }
        // Higher score first; then the shorter word (closer to what's typed); then
        // lexicographic for determinism.
        scored.sortWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first.length }
                .thenBy { it.first },
        )
        for ((bangla, _) in scored) {
            if (out.size >= maxResults) break
            out.add(bangla)
        }
        return out
    }

    private fun offer(cand: HashMap<String, Int>, bangla: String, score: Int) {
        val cur = cand[bangla]
        if (cur == null || score > cur) cand[bangla] = score
    }

    /** First index in [list] (sorted by fold) whose fold is >= [prefix]. */
    private fun foldLowerBound(list: List<Folded>, prefix: String): Int {
        var lo = 0
        var hi = list.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (list[mid].fold < prefix) lo = mid + 1 else hi = mid
        }
        return lo
    }

    private companion object {
        /** Words at or above this weight (the curated common list) go in the `top` index. */
        const val TOP_MIN_FREQ = 35

        /** Max entries scanned per pass: the small `top` index, then the big lexicon. */
        const val TOP_SCAN = 2000
        const val LEXICON_SCAN = 600

        /** Each committed use is worth this much static-frequency weight. */
        const val LEARN_WEIGHT = 40

        /**
         * Starting weight of a user-added word. Low on purpose: every use adds
         * [LEARN_WEIGHT], so a word used twice already ranks near the top, while a
         * single accidental commit doesn't bury the correct spelling.
         */
        const val USER_WEIGHT = 10

        /** Boost for a candidate that is the whole word being typed (not a longer completion). */
        const val WHOLE_WORD_BONUS = 40

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
            Triple("bhalo", "ভালো", 90), Triple("valo", "ভালো", 90),
            Triple("bhalobasa", "ভালোবাসা", 70), Triple("valobasa", "ভালোবাসা", 70),
            Triple("achi", "আছি", 70), Triple("ache", "আছে", 75),
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
