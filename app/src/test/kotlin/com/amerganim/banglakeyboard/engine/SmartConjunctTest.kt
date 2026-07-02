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

    @Test fun phalaFormsAlwaysJoin() {
        // ya-phala (্য) and ra-phala (্র) join even when the exact cluster isn't listed.
        assertEquals("ফ্য", smart("fz")) // ফ + য (was wrongly breaking to ফয)
        assertEquals("ফ্যাসিস্ট", smart("fzasisT"))
        assertEquals("ক্য", smart("kz"))
        assertEquals("ফ্র", smart("fr"))
        // A vowel between them keeps them separate.
        assertEquals("ফয", smart("foz"))
        // Amader (discrete keys): the ফ key then the য key joins to ফ্য.
        assertEquals("ফ্য", Transliterator.transliterateTokens(listOf("f", "z"), smart = true))
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

    @Test fun raYaPhalaLoanwords() {
        // Word-initial র + ya-phala gets a ZWJ (‍) so it renders as ya-phala on র
        // (র‍্যাব, র‍্যান্ডম…), not reph.
        val z = "‍"
        assertEquals("র${z}্যাব", smart("rzab"))
        assertEquals("র${z}্যাম", smart("rzam"))
        assertEquals("র${z}্যাগিং", smart("rzaging"))
        assertEquals("র${z}্যান্ডম", smart("rzanDom"))
        // Amader: tap র , য , আ , ম
        assertEquals("র${z}্যাম", Transliterator.transliterateTokens(listOf("r", "z", "a", "m"), smart = true))
        // Mid-word র্য stays reph (no ZWJ): সূর্য, কার্য.
        assertEquals("সূর্য", smart("sUrz"))
        assertEquals("কার্য", smart("karz"))
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

    @Test fun fixedLayoutConjuncts() {
        fun keys(vararg t: String) = Transliterator.transliterateTokens(t.toList(), smart = true)
        // Single-letter keys never merge into a digraph, but DO auto-join valid conjuncts.
        assertEquals("কহ", keys("k", "h")) // not the "kh" digraph খ (ক্হ invalid → কহ)
        assertEquals("নগ", keys("n", "g")) // not ং
        assertEquals("ক্ষ", keys("k", "Sh")) // ক + ষ auto-join to ক্ষ
        assertEquals("ক্ত", keys("k", "t")) // auto-join
        // The pre-composed ক্ষ key is atomic — it doesn't over-extend.
        assertEquals("ক্ষ", keys("kSh"))
        assertEquals("ক্ষমা", keys("kSh", "m", "a")) // ম does NOT extend ক্ষ
        assertEquals("লক্ষ", keys("l", "kSh")) // ল does NOT steal the ক
        // The hasanta (্) key joins explicitly too.
        assertEquals("ক্ষ", keys("k", "`", "Sh"))
        // Kars attach.
        assertEquals("কু", keys("k", "u"))
        assertEquals("ক", keys("k"))
        // Non-smart chains adjacent consonants.
        assertEquals("ক্হ", Transliterator.transliterateTokens(listOf("k", "h"), smart = false))
    }

    @Test fun fallsBackToPlainWhenListEmpty() {
        Conjuncts.setFromList("") // no prefixes loaded
        assertEquals("য্খ্ন", smart("zkhn")) // smart no-ops -> normal joining
    }
}
