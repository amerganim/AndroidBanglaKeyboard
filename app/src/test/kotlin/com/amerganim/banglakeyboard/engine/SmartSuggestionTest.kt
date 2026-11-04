package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Suggestion quality: sound-alike matching, frequency ranking, learned-word
 * weighting and starter next-word predictions.
 */
class SmartSuggestionTest {

    /** A suggester loaded with the real bundled assets, as the keyboard does. */
    private fun bundled(): Suggester {
        val assets = File("src/main/assets")
        val s = Suggester()
        s.loadDictionaryData(File(assets, "dictionary.tsv").readText())
        s.loadWordList(
            File(assets, "words.tsv").readText() + "\n" + File(assets, "common_words.tsv").readText(),
        )
        return s
    }

    private val real by lazy { bundled() }

    private fun top(prefix: String, n: Int = 4) = real.suggest(prefix).take(n)

    // ---- sound-alike folding ------------------------------------------------

    @Test fun foldMergesCommonConfusions() {
        assertEquals(Phonetic.fold("জীবন"), Phonetic.fold("জিবন")) // ী / ি
        assertEquals(Phonetic.fold("টাকা"), Phonetic.fold("তাকা")) // ট / ত
        assertEquals(Phonetic.fold("শিক্ষা"), Phonetic.fold("শিক্খা")) // ক্ষ / ক্খ
        assertEquals(Phonetic.fold("ধন্যবাদ"), Phonetic.fold("ধন্নবাদ")) // ya-phala / doubling
        assertEquals(Phonetic.fold("চাঁদ"), Phonetic.fold("চাদ")) // missing chandrabindu
        assertEquals(Phonetic.fold("বাড়ি"), Phonetic.fold("বারি")) // ড় / র
        assertEquals(Phonetic.fold("সম\u09AF\u09BC"), Phonetic.fold("সম\u09DF")) // decomposed vs precomposed য়
    }

    // ---- real-dictionary behaviour -------------------------------------------

    @Test fun misspelledVowelLengthStillOffersCorrectWord() {
        assertTrue(top("jibon").toString(), "জীবন" in top("jibon", 3))
        assertTrue(top("ki").toString(), "কী" in top("ki", 3))
    }

    @Test fun lowercaseRetroflexOffersCorrectWord() {
        // Nobody presses Shift on a phone: taka should still give টাকা.
        assertTrue(top("taka").toString(), "টাকা" in top("taka", 3))
        assertTrue(top("thik").toString(), "ঠিক" in top("thik", 3))
    }

    @Test fun sibilantAndConjunctMistakesAreForgiven() {
        assertTrue(top("sikkha").toString(), "শিক্ষা" in top("sikkha", 4))
        assertTrue(top("dhonnobad").toString(), "ধন্যবাদ" in top("dhonnobad", 3))
        assertTrue(top("bari").toString(), "বাড়ি" in top("bari", 3))
    }

    @Test fun commonWordsOutrankRareOnes() {
        // For "kor" the everyday verb forms should lead, not rare lexicon words.
        val v = real.suggest("kor").drop(1).take(5)
        assertTrue(v.toString(), v.any { it in setOf("করে", "করা", "করি", "করব", "করবে") })
    }

    @Test fun avroStyleChGivesChho() {
        assertTrue(top("chel").toString(), "ছেলে" in top("chel", 4))
        assertTrue(top("cai").toString(), "চাই" in top("cai", 3))
        assertTrue(top("cOkh").toString(), "চোখ" in top("cOkh", 3))
        // Old-habit spelling still finds the word.
        assertTrue(top("chai").toString(), "চাই" in real.suggest("chai"))
    }

    // ---- learned words ---------------------------------------------------------

    @Test fun singleAccidentalCommitDoesNotBuryCorrectSpelling() {
        val s = Suggester()
        s.loadWordList("জীবন\t50\n")
        // The user once committed the misspelling জিবনটা (literal) by pressing Space.
        s.addUserWord("জিবনটা")
        s.recordUsage("জিবনটা")
        val v = s.suggest("jibon")
        assertTrue(v.toString(), v.indexOf("জীবন") in 1 until (v.indexOf("জিবনটা").takeIf { it > 0 } ?: 99))
    }

    @Test fun repeatedlyUsedUserWordRisesToTop() {
        val s = Suggester()
        s.loadWordList("আকাশ\t50\n")
        s.addUserWord("আকাশলীনা")
        repeat(3) { s.recordUsage("আকাশলীনা") }
        assertEquals("আকাশলীনা", s.suggest("aka")[1])
    }

    // ---- starter predictions ---------------------------------------------------

    @Test fun starterPredictionsWorkBeforeAnyLearning() {
        val b = Bigrams()
        b.loadSeed("কেমন\tআছো\t9\nকেমন\tআছেন\t8\n# comment\n")
        assertEquals(listOf("আছো", "আছেন"), b.predict("কেমন"))
        assertTrue(b.dump().isBlank()) // never persisted
    }

    @Test fun learnedPairsComeBeforeStarterPairs() {
        val b = Bigrams()
        b.loadSeed("কেমন\tআছো\t9\n")
        b.record("কেমন", "আছিস")
        assertEquals(listOf("আছিস", "আছো"), b.predict("কেমন"))
        b.clear()
        assertEquals(listOf("আছো"), b.predict("কেমন")) // starter set survives clear
    }

    @Test fun forgottenStarterWordIsHidden() {
        val b = Bigrams()
        b.loadSeed("কেমন\tআছো\t9\nকেমন\tআছেন\t8\n")
        b.forget("আছো")
        assertEquals(listOf("আছেন"), b.predict("কেমন"))
    }
}
