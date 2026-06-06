package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test

/** Ported from the Windows keyboard's `tests/test_transliterator.cpp`. */
class TransliteratorTest {

    private fun check(input: String, expected: String) {
        assertEquals("transliterate(\"$input\")", expected, Transliterator.transliterate(input))
    }

    @Test fun independentVowelAndKar() {
        check("ami", "আমি")
        check("amar", "আমার")
        check("tumi", "তুমি")
        check("kobita", "কবিতা")
    }

    @Test fun anusvaraVsVelarNasal() {
        check("rong", "রং")
        check("bangla", "বাংলা")
    }

    @Test fun combiningMarksAndVocalicVowel() {
        check("cha^d", "চাঁদ") // chandrabindu
        check("rriSi", "ঋষি")
        check("a^", "আঁ")
        check("a:", "আঃ") // visarga
        check(".", "।") // dari
        check("Sesh.", "ষেশ।")
    }

    @Test fun zaYaJaAreDistinct() {
        check("z", "য")
        check("noy", "নয়")
        check("kaj", "কাজ")
    }

    @Test fun explicitHasantaAndKhandaTa() {
        check("r`k", "র্ক") // ref
        check("k`z", "ক্য") // ya-phala
        check("t`", "ৎ") // khanda-ta
    }

    @Test fun capitalVowelsRenderAsKar() {
        check("tU", "তূ")
        check("tO", "তো")
    }

    @Test fun boPholaViaW() {
        check("swopno", "স্বপ্ন")
        check("bishwas", "বিশ্বাস")
    }

    @Test fun inherentVowel() {
        check("boi", "বই")
        check("onek", "অনেক")
    }

    @Test fun conjunctsViaAutoHasanta() {
        check("kk", "ক্ক")
        check("sundor", "সুন্দর")
    }

    @Test fun multiCharConsonantsAndAspirates() {
        check("Dhaka", "ঢাকা")
        check("bhai", "ভাই")
    }

    @Test fun banglaDigits() {
        check("2024", "২০২৪")
    }

    @Test fun unknownCharactersPassThrough() {
        check("ami tumi", "আমি তুমি")
    }
}
