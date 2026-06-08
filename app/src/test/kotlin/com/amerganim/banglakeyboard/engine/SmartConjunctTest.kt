package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Smart-conjunct mode: two consonants join into a conjunct only when they form a
 * real juktakkhor; otherwise each keeps its inherent vowel.
 */
class SmartConjunctTest {

    @Before fun loadConjuncts() {
        // Unit tests run with the module dir (app/) as the working directory.
        Conjuncts.setFromList(File("src/main/assets/juktakkhor.txt").readText())
    }

    private fun smart(input: String) = Transliterator.transliterate(input, smart = true)
    private fun plain(input: String) = Transliterator.transliterate(input, smart = false)

    @Test fun invalidClusterGetsInherentVowel() {
        // য্খ and খ্ন are not real conjuncts, so smart mode keeps them separate.
        assertEquals("যখন", smart("zkhn"))
        assertEquals("যখন", smart("zokhon")) // explicit inherent vowels: same result
    }

    @Test fun realConjunctsStillForm() {
        assertEquals("ক্ষ", smart("kSh"))
        assertEquals("ক্ম", smart("km")) // ক্ম is a valid juktakkhor
        assertEquals("ক্ত", smart("kt"))
        assertEquals("ন্ত্র", smart("ntr"))
        assertEquals("স্ত্র", smart("str"))
        assertEquals("ক্ষ্ম", smart("kShm"))
    }

    @Test fun refAlwaysForms() {
        assertEquals("র্ক", smart("rk"))
        assertEquals("রক", smart("rok")) // explicit inherent vowel -> no ref
    }

    @Test fun vowelsAndWordsUnaffected() {
        assertEquals("আমি", smart("ami"))
        assertEquals("কবিতা", smart("kobita"))
        assertEquals("বিস্তারিত", smart("bistarito"))
        assertEquals("আমার", smart("amar"))
        assertEquals("বাংলা", smart("bangla"))
    }

    @Test fun plainModeStillChainsConjuncts() {
        // Without smart, adjacent consonants always join (old behavior).
        assertEquals("য্খ্ন", plain("zkhn"))
    }

    @Test fun pronunciationConjunctSpellings() {
        // gg -> জ্ঞ, and n before চ/জ -> ঞ (smart mode).
        assertEquals("জ্ঞান", smart("ggan"))
        assertEquals("বিজ্ঞান", smart("biggan"))
        assertEquals("অঞ্চল", smart("onchol"))
        assertEquals("সঞ্চয়", smart("sonchoy"))
        assertEquals("পাঞ্জাবি", smart("panjabi"))
        // Plain mode keeps the literal spelling.
        assertEquals("বিগ্গান", plain("biggan"))
    }

    @Test fun fixedLayoutTokensDoNotMerge() {
        fun keys(vararg t: String) = Transliterator.transliterateTokens(t.toList(), smart = true)
        // Separate keys never merge or auto-join (each is a discrete letter).
        assertEquals("কহ", keys("k", "h")) // not the "kh" digraph খ
        assertEquals("নগ", keys("n", "g")) // not ং
        assertEquals("কষ", keys("k", "Sh")) // two keys → no conjunct
        // A multi-unit key (the Amader ক্ষ key sends one token "kSh") joins inside it.
        assertEquals("ক্ষ", keys("kSh"))
        assertEquals("ক্ষমা", keys("kSh", "m", "a")) // ম does NOT join ক্ষ
        assertEquals("লক্ষ", keys("l", "kSh")) // ল does NOT steal the ক
        // The hasanta (্) key explicitly joins two letter keys.
        assertEquals("ক্ষ", keys("k", "`", "Sh"))
        // Kars still attach across keys.
        assertEquals("কু", keys("k", "u"))
        assertEquals("ক", keys("k"))
        // Without smart, separate keys still don't auto-join.
        assertEquals("কহ", Transliterator.transliterateTokens(listOf("k", "h"), smart = false))
    }

    @Test fun fallsBackToPlainWhenListEmpty() {
        Conjuncts.setFromList("") // no prefixes loaded
        assertEquals("য্খ্ন", smart("zkhn")) // smart no-ops -> normal joining
    }
}
