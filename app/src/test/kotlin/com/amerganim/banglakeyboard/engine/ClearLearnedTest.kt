package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Clearing learned data must remove everything the user taught the keyboard while
 * leaving the bundled dictionaries intact — the whole point is that ordinary
 * suggestions keep working afterwards.
 */
class ClearLearnedTest {

    @Test fun banglaUserWordIsForgotten() {
        val s = Suggester()
        s.addUserWord("আমারনিজেরশব্দ")
        assertTrue(s.suggestByBangla("আমারনিজের").contains("আমারনিজেরশব্দ"))

        s.clearLearned()
        assertFalse(s.suggestByBangla("আমারনিজের").contains("আমারনিজেরশব্দ"))
    }

    @Test fun banglaBuiltinDictionarySurvivesClear() {
        val s = Suggester()
        s.addUserWord("আমারনিজেরশব্দ")
        s.clearLearned()
        // Still the ordinary built-in behaviour from SuggesterTest.
        assertEquals("আমি", s.suggest("ami").first())
        assertTrue(s.suggest("ama").contains("আমার"))
    }

    @Test fun banglaUsageCountsAreReset() {
        val s = Suggester()
        repeat(20) { s.recordUsage("আমাকে") }
        s.clearLearned()
        // With the boost gone, the heavily-used word no longer outranks the rest.
        val dumped = s.dumpLearnedData()
        assertTrue(dumped.isBlank())
    }

    @Test fun englishUserWordIsForgotten() {
        val e = EnglishSuggester()
        e.addUserWord("zzzcustomword")
        assertTrue(e.suggest("zzzcustom").contains("zzzcustomword"))

        e.clearLearned()
        assertFalse(e.suggest("zzzcustom").contains("zzzcustomword"))
    }

    @Test fun englishLoadedWordListSurvivesClear() {
        val e = EnglishSuggester()
        e.loadWordList("hello\nworld\n")
        e.addUserWord("zzzcustomword")
        e.clearLearned()
        assertTrue(e.suggest("hel").contains("hello"))
        assertTrue(e.dumpLearnedData().isBlank())
    }

    @Test fun bigramsAreCleared() {
        val b = Bigrams()
        b.record("আমি", "যাব")
        assertTrue(b.predict("আমি").contains("যাব"))

        b.clear()
        assertTrue(b.predict("আমি").isEmpty())
    }
}
