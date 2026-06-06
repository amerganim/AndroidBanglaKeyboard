package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.Normalizer

/**
 * Rigorous round-trip tests: any Bangla passage is reverse-transliterated to its
 * canonical romanization ([BanglaReverse]) and then run back through the forward
 * [Transliterator]; the result must equal the original Bangla. This exercises the
 * engine over real text far more thoroughly than a handful of hand-written cases.
 *
 * To extend coverage, paste any Bangla text into [CORPUS].
 */
class RoundTripTest {

    private fun roundTrip(bangla: String) {
        val nfc = Normalizer.normalize(bangla, Normalizer.Form.NFC)
        for (word in nfc.split(" ").filter { it.isNotBlank() }) {
            val roman = BanglaReverse.toRoman(word)
            assertEquals(
                "round-trip failed for \"$word\" (via roman \"$roman\")",
                word,
                Transliterator.transliterate(roman),
            )
        }
    }

    @Test fun roundTripsCorpus() {
        for (line in CORPUS) roundTrip(line)
    }

    private companion object {
        // Representative Bangla text within the scheme's coverage. Mix of vowels,
        // kars, conjuncts, signs and digits.
        val CORPUS = listOf(
            "আমি বাংলায় গান গাই।",
            "আমার সোনার বাংলা আমি তোমায় ভালোবাসি।",
            "তুমি কেমন আছ আজকে।",
            "বাংলাদেশ একটি সুন্দর দেশ।",
            "আকাশে অনেক তারা আছে।",
            "বিশ্বাস আর ভালোবাসা মানুষের জীবনে দরকার।",
            "ক্ষমা একটি মহৎ গুণ।",
            "নদীর জল ঠান্ডা আর পরিষ্কার।",
            "আজ রাতে চাঁদ উঠেছে।",
            "দুঃখ আর সুখ জীবনের অংশ।",
            "১২৩৪৫ সংখ্যা বাংলায় লেখা।",
            "স্বপ্ন দেখা ভালো অভ্যাস।",
        )
    }
}
