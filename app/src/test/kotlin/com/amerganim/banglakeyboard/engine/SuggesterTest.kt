package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Ported from the suggestion checks in `tests/test_transliterator.cpp`. */
class SuggesterTest {

    /** A suggester seeded with the built-in dictionary only (no assets). */
    private fun default() = Suggester()

    @Test fun firstSuggestionIsLiteralTransliteration() {
        assertEquals("আমি", default().suggest("ami").first())
    }

    @Test fun dictionaryWordsSharingPrefixFollow() {
        val v = default().suggest("ama")
        assertTrue(v.contains("আমার"))
        assertTrue(v.contains("আমাকে"))
    }

    @Test fun builtinPrefixMatches() {
        val bhal = default().suggest("bhal")
        assertTrue(bhal.contains("ভালো"))
        assertTrue(bhal.contains("ভালোবাসা"))
        assertTrue(default().suggest("dhonnobad").contains("ধন্যবাদ"))
    }

    @Test fun emptyPrefixYieldsNothing() {
        assertTrue(default().suggest("").isEmpty())
    }

    @Test fun loadedDictionaryWordBecomesSuggestible() {
        val s = Suggester()
        s.loadDictionaryData("notunshobdo\tনতুনশব্দ\t99\n# comment line\n")
        assertTrue(s.suggest("notunsh").contains("নতুনশব্দ"))
    }

    @Test fun wordListPrefixCompletion() {
        val s = Suggester()
        s.loadWordList("আকাশ\t50\nআকাশগঙ্গা\t10\n# comment\nঅন্য\t5\n")
        val v = s.suggest("aka") // transliterate("aka") = আকা
        assertTrue(v.contains("আকাশ"))
        assertTrue(v.contains("আকাশগঙ্গা"))
    }

    @Test fun forgivingMatchToleratesMissingConjunct() {
        val s = Suggester()
        s.loadWordList("ফ্যাসিস্ট\t50\n")
        // "foz" makes ফয (no conjunct) — exact prefix ফয != ফ্য, but the loose index
        // (hasanta stripped) still surfaces ফ্যাসিস্ট.
        assertTrue(s.suggest("foz").contains("ফ্যাসিস্ট"))
        // And it of course still matches when the conjunct is typed.
        assertTrue(s.suggest("fz").contains("ফ্যাসিস্ট"))
    }

    @Test fun exactMatchOutranksForgivingMatch() {
        val s = Suggester()
        s.loadWordList("কত\t50\nক্ত\t50\n")
        // Typing কত should put the exact কত before the loose-only ক্ত.
        val v = s.suggest("kot") // কত
        val exact = v.indexOf("কত")
        val loose = v.indexOf("ক্ত")
        assertTrue(exact in 0 until loose || loose == -1)
    }

    @Test fun learnedUsagePromotesWord() {
        val s = Suggester()
        repeat(5) { s.recordUsage("আমাকে") }
        val v = s.suggest("ama")
        // v[0] is the literal; v[1] should now be the learned word.
        assertTrue(v.size >= 2)
        assertEquals("আমাকে", v[1])
    }

    @Test fun learnedDataRoundTrips() {
        val s = Suggester()
        repeat(5) { s.recordUsage("আমাকে") }
        val s2 = Suggester()
        s2.loadLearnedData(s.dumpLearnedData())
        val v2 = s2.suggest("ama")
        assertTrue(v2.size >= 2)
        assertEquals("আমাকে", v2[1])
    }
}
