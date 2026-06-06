package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BigramsTest {

    @Test fun predictsMostFrequentNextWord() {
        val b = Bigrams()
        repeat(3) { b.record("আমি", "ভালো") }
        b.record("আমি", "আছি")
        val p = b.predict("আমি")
        assertEquals("ভালো", p.first()) // most frequent first
        assertTrue(p.contains("আছি"))
    }

    @Test fun unknownContextHasNoPrediction() {
        assertTrue(Bigrams().predict("কিছু").isEmpty())
    }

    @Test fun forgetRemovesContextAndTarget() {
        val b = Bigrams()
        b.record("a", "b")
        b.record("x", "b")
        b.forget("b")
        assertTrue(b.predict("a").isEmpty()) // b removed as a target
        assertTrue(b.predict("b").isEmpty()) // b removed as a context
    }

    @Test fun roundTripsThroughDump() {
        val b = Bigrams()
        repeat(2) { b.record("আমি", "বাংলায়") }
        val b2 = Bigrams()
        b2.load(b.dump())
        assertEquals(listOf("বাংলায়"), b2.predict("আমি"))
    }
}

class EnglishSuggesterTest {

    private fun seeded() = EnglishSuggester().apply {
        // ranked list: earlier = more frequent
        loadWordList("the\nand\nhello\nhelp\nhead\nhero\n")
    }

    @Test fun firstSuggestionIsLiteralTyped() {
        assertEquals("hel", seeded().suggest("hel").first())
    }

    @Test fun completesByPrefixRankedByFrequency() {
        val v = seeded().suggest("hel")
        assertTrue(v.contains("hello"))
        assertTrue(v.contains("help"))
        // "hello" is ranked above "help" in the list, so it should come first.
        assertTrue(v.indexOf("hello") < v.indexOf("help"))
    }

    @Test fun userWordBecomesSuggestible() {
        val s = seeded()
        assertFalse(s.suggest("hex").contains("hexagon"))
        s.addUserWord("hexagon")
        assertTrue(s.suggest("hex").contains("hexagon"))
    }

    @Test fun forgetRemovesUserWord() {
        val s = seeded()
        s.addUserWord("hexagon")
        s.forget("hexagon")
        assertFalse(s.suggest("hex").contains("hexagon"))
    }
}

class SuggesterUserDictTest {

    @Test fun userWordIsSuggestedByPrefix() {
        val s = Suggester()
        // "kaj" transliterates to কাজ; a user word কাজগুলো should complete from কাজ.
        s.addUserWord("কাজগুলো")
        assertTrue(s.suggest("kaj").contains("কাজগুলো"))
    }

    @Test fun forgetRemovesUserWord() {
        val s = Suggester()
        s.addUserWord("কাজগুলো")
        s.forget("কাজগুলো")
        assertFalse(s.suggest("kaj").contains("কাজগুলো"))
    }
}
