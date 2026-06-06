package com.amerganim.banglakeyboard.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * A personal next-word model learned from the user's own typing. Records how often
 * one committed word is followed by another and predicts likely next words. No
 * bundled data — it starts empty and improves with use. Persisted by the host via
 * [load] / [dump].
 */
class Bigrams {

    // prev word -> (next word -> count)
    private val map = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    /** Record that [next] followed [prev]. */
    fun record(prev: String, next: String) {
        if (prev.isBlank() || next.isBlank()) return
        map.getOrPut(prev) { ConcurrentHashMap() }.merge(next, 1, Int::plus)
    }

    /** Most likely words to follow [prev], best first. */
    fun predict(prev: String, max: Int = 9): List<String> {
        val nexts = map[prev] ?: return emptyList()
        return nexts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(max)
            .map { it.key }
    }

    /** Forget [word] entirely — as a context and as a prediction target. */
    fun forget(word: String) {
        map.remove(word)
        for (nexts in map.values) nexts.remove(word)
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
