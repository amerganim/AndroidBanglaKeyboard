package com.amerganim.banglakeyboard.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * A personal next-word model learned from the user's own typing. Records how often
 * one committed word is followed by another and predicts likely next words.
 * Persisted by the host via [load] / [dump].
 *
 * An optional bundled **starter** set ([loadSeed]) fills in common pairs
 * (কেমন → আছো, শুভ → সকাল) so predictions are useful from day one. The user's own
 * pairs always rank first; starter pairs are never persisted, and [clear] leaves them.
 */
class Bigrams {

    // prev word -> (next word -> count)
    private val map = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    // Bundled starter pairs: prev word -> next words, best first. Swapped atomically.
    @Volatile
    private var seed: Map<String, List<String>> = emptyMap()

    // Starter targets the user explicitly forgot this session.
    private val forgotten = ConcurrentHashMap.newKeySet<String>()

    /** Record that [next] followed [prev]. */
    fun record(prev: String, next: String) {
        if (prev.isBlank() || next.isBlank()) return
        map.getOrPut(prev) { ConcurrentHashMap() }.merge(next, 1, Int::plus)
    }

    /** Most likely words to follow [prev], best first: learned pairs, then starter pairs. */
    fun predict(prev: String, max: Int = 9): List<String> {
        val out = ArrayList<String>(max)
        map[prev]?.entries
            ?.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            ?.forEach { if (out.size < max) out.add(it.key) }
        if (prev !in forgotten) {
            for (w in seed[prev].orEmpty()) {
                if (out.size >= max) break
                if (w !in out && w !in forgotten) out.add(w)
            }
        }
        return out
    }

    /** Forget [word] entirely — as a context and as a prediction target. */
    fun forget(word: String) {
        map.remove(word)
        for (nexts in map.values) nexts.remove(word)
        forgotten.add(word)
    }

    /**
     * Load bundled starter pairs from TSV text: `prev<TAB>next<TAB>weight` per line
     * (`#` comments allowed). Not persisted by [dump].
     */
    fun loadSeed(tsv: String) {
        val tmp = HashMap<String, MutableList<Pair<String, Int>>>()
        for (raw in tsv.lineSequence()) {
            if (raw.isBlank() || raw.trimStart().startsWith('#')) continue
            val parts = raw.split('\t')
            if (parts.size < 2) continue
            val prev = parts[0].trim()
            val next = parts[1].trim()
            val w = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 1
            if (prev.isNotEmpty() && next.isNotEmpty()) tmp.getOrPut(prev) { ArrayList() }.add(next to w)
        }
        seed = tmp.mapValues { (_, v) -> v.sortedByDescending { it.second }.map { it.first }.distinct() }
    }

    /** Drop every learned pair (the bundled starter set stays). */
    fun clear() {
        map.clear()
        forgotten.clear()
    }

    fun load(tsv: String) {
        for (raw in tsv.lineSequence()) {
            val parts = raw.split('\t')
            if (parts.size < 3) continue
            val prev = parts[0].trim()
            val next = parts[1].trim()
            val count = parts[2].trim().toIntOrNull() ?: continue
            if (prev.isNotEmpty() && next.isNotEmpty() && count > 0) {
                map.getOrPut(prev) { ConcurrentHashMap() }[next] = count
            }
        }
    }

    fun dump(): String {
        val sb = StringBuilder()
        for ((prev, nexts) in map) {
            for ((next, count) in nexts) {
                sb.append(prev).append('\t').append(next).append('\t').append(count).append('\n')
            }
        }
        return sb.toString()
    }
}
