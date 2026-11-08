package com.amerganim.banglakeyboard.data

import java.util.Base64

/** One remembered clip. [time] is when it was copied (epoch ms). */
data class Clip(val text: String, val time: Long, val pinned: Boolean = false)

/**
 * The keyboard's clipboard history: recently copied text, newest first.
 *
 * Kept deliberately small and short-lived, like Gboard's: unpinned clips expire after
 * [TTL_MS] and at most [MAX_UNPINNED] are kept; pinned clips stay until the user
 * removes them. Pure Kotlin (no Android types) so it is unit-testable; the service
 * persists it via [serialize] / [deserialize] in no-backup storage.
 */
class ClipboardHistory {

    private val items = ArrayList<Clip>()

    /** Current clips, newest first, after dropping expired ones. */
    fun clips(now: Long): List<Clip> {
        prune(now)
        return items.toList()
    }

    /**
     * Remember [text] copied at [now]. Blank text is ignored, long text is trimmed to
     * [MAX_CHARS]. Copying something already in the list moves it to the top (keeping
     * its pin). Returns true if the list changed.
     */
    fun add(text: String, now: Long): Boolean {
        if (text.isBlank()) return false
        val t = if (text.length > MAX_CHARS) text.substring(0, MAX_CHARS) else text
        val existing = items.indexOfFirst { it.text == t }
        val pinned = existing >= 0 && items[existing].pinned
        if (existing >= 0) items.removeAt(existing)
        items.add(0, Clip(t, now, pinned))
        prune(now)
        return true
    }

    fun setPinned(text: String, pinned: Boolean, now: Long) {
        val i = items.indexOfFirst { it.text == text }
        if (i < 0) return
        // Unpinning restarts the expiry clock, so the clip doesn't vanish instantly.
        items[i] = items[i].copy(pinned = pinned, time = if (pinned) items[i].time else now)
    }

    fun remove(text: String) {
        items.removeAll { it.text == text }
    }

    /** Remove every unpinned clip (pinned ones are kept on purpose). */
    fun clearUnpinned() {
        items.removeAll { !it.pinned }
    }

    fun clearAll() = items.clear()

    private fun prune(now: Long) {
        items.removeAll { !it.pinned && now - it.time > TTL_MS }
        var unpinned = 0
        val it = items.iterator()
        while (it.hasNext()) {
            val c = it.next()
            if (!c.pinned && ++unpinned > MAX_UNPINNED) it.remove()
        }
        while (items.size > MAX_TOTAL) items.removeAt(items.size - 1)
    }

    /** One clip per line: `pinned<TAB>time<TAB>base64(text)`. */
    fun serialize(): String {
        val enc = Base64.getEncoder()
        return items.joinToString("\n") { c ->
            (if (c.pinned) "1" else "0") + "\t" + c.time + "\t" +
                enc.encodeToString(c.text.toByteArray(Charsets.UTF_8))
        }
    }

    fun deserialize(data: String, now: Long) {
        items.clear()
        val dec = Base64.getDecoder()
        for (line in data.lineSequence()) {
            val p = line.split('\t')
            if (p.size != 3) continue
            val time = p[1].toLongOrNull() ?: continue
            val text = runCatching { String(dec.decode(p[2]), Charsets.UTF_8) }.getOrNull() ?: continue
            if (text.isNotBlank()) items.add(Clip(text, time, p[0] == "1"))
        }
        prune(now)
    }

    companion object {
        /** Unpinned clips are forgotten after one hour. */
        const val TTL_MS: Long = 60 * 60 * 1000L
        const val MAX_UNPINNED = 20
        const val MAX_TOTAL = 50
        const val MAX_CHARS = 5000
    }
}
