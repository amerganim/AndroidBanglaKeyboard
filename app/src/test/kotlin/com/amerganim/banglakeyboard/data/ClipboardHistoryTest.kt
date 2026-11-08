package com.amerganim.banglakeyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHistoryTest {

    private val hour = ClipboardHistory.TTL_MS

    private fun texts(h: ClipboardHistory, now: Long) = h.clips(now).map { it.text }

    @Test fun newestFirstAndDuplicatesMoveToTop() {
        val h = ClipboardHistory()
        h.add("one", 1)
        h.add("two", 2)
        h.add("one", 3)
        assertEquals(listOf("one", "two"), texts(h, 3))
    }

    @Test fun blankIsIgnored() {
        val h = ClipboardHistory()
        assertFalse(h.add("   ", 1))
        assertTrue(h.clips(1).isEmpty())
    }

    @Test fun unpinnedClipsExpireAfterAnHour() {
        val h = ClipboardHistory()
        h.add("old", 0)
        h.add("new", hour)
        assertEquals(listOf("new", "old"), texts(h, hour))
        assertEquals(listOf("new"), texts(h, hour + 1))
    }

    @Test fun pinnedClipsNeverExpireAndSurviveClear() {
        val h = ClipboardHistory()
        h.add("address", 0)
        h.add("temp", 0)
        h.setPinned("address", true, 0)
        assertEquals(listOf("address"), texts(h, 10 * hour))
        h.add("another", 10 * hour)
        h.clearUnpinned()
        assertEquals(listOf("address"), texts(h, 10 * hour))
    }

    @Test fun unpinningRestartsTheClock() {
        val h = ClipboardHistory()
        h.add("x", 0)
        h.setPinned("x", true, 0)
        h.setPinned("x", false, 5 * hour)
        assertEquals(listOf("x"), texts(h, 5 * hour + 10)) // not instantly expired
    }

    @Test fun recopyingKeepsThePin() {
        val h = ClipboardHistory()
        h.add("x", 0)
        h.setPinned("x", true, 0)
        h.add("y", 1)
        h.add("x", 2)
        assertTrue(h.clips(2).first { it.text == "x" }.pinned)
    }

    @Test fun unpinnedCountIsCapped() {
        val h = ClipboardHistory()
        for (i in 1..30) h.add("clip $i", i.toLong())
        val clips = h.clips(30)
        assertEquals(ClipboardHistory.MAX_UNPINNED, clips.size)
        assertEquals("clip 30", clips.first().text) // oldest dropped, newest kept
    }

    @Test fun longTextIsTrimmed() {
        val h = ClipboardHistory()
        h.add("a".repeat(ClipboardHistory.MAX_CHARS + 50), 1)
        assertEquals(ClipboardHistory.MAX_CHARS, h.clips(1).first().text.length)
    }

    @Test fun roundTripsBanglaMultilineAndTabs() {
        val h = ClipboardHistory()
        h.add("আমার ঠিকানা:\nঢাকা\tবাংলাদেশ", 5)
        h.add("plain", 6)
        h.setPinned("plain", true, 6)
        val h2 = ClipboardHistory()
        h2.deserialize(h.serialize(), 6)
        assertEquals(h.clips(6), h2.clips(6))
    }

    @Test fun corruptLinesAreSkipped() {
        val h = ClipboardHistory()
        h.deserialize("garbage\n1\tnot-a-time\tAAAA\n", 0)
        assertTrue(h.clips(0).isEmpty())
    }
}
