package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Normalizer

/**
 * Rigorous round-trip tests: any Bangla passage is reverse-transliterated to its
 * canonical romanization ([BanglaReverse]) and then run back through the forward
 * [Transliterator]; the result must equal the original Bangla. This exercises the
 * engine over real text far more thoroughly than a handful of hand-written cases.
 *
 * To extend coverage, paste any Bangla text into [CORPUS] or [ARTICLE].
 */
class RoundTripTest {

    /** Returns the words in [text] that fail to round-trip, with details. */
    private fun failures(text: String): List<String> {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val out = mutableListOf<String>()
        for (word in nfc.split(Regex("\\s+")).filter { it.isNotBlank() }) {
            val roman = BanglaReverse.toRoman(word)
            val got = Transliterator.transliterate(roman)
            if (got != word) out += "\"$word\" -> roman \"$roman\" -> \"$got\""
        }
        return out
    }

    @Test fun roundTripsCuratedCorpus() {
        val fails = CORPUS.flatMap { failures(it) }
        assertTrue("Curated round-trip failures:\n" + fails.joinToString("\n"), fails.isEmpty())
    }

    @Test fun roundTripsRealArticle() {
        val fails = ARTICLE.flatMap { failures(it) }
        assertTrue(
            "Real-article round-trip failures (${fails.size}):\n" + fails.joinToString("\n"),
            fails.isEmpty(),
        )
    }

    private companion object {
        // Representative Bangla text within the scheme's coverage.
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

        // Real text from a Prothom Alo article (prothomalo.com/business/economics/x80otttiyt).
        val ARTICLE = listOf(
            "চট্টগ্রাম বন্দরের সবচেয়ে গুরুত্বপূর্ণ নিউমুরিং কনটেইনার টার্মিনাল নিয়ে একই দিনে একই বিষয়ে দুটি ভিন্ন বার্তার চিঠি গেছে।",
            "ওই দুই চিঠিকে কেন্দ্র করে নতুন করে আলোচনায় এসেছে নিউমুরিং কনটেইনার টার্মিনাল।",
            "দুটি চিঠি পাঠিয়েছে নৌ মন্ত্রণালয়।",
            "চট্টগ্রাম বন্দরের চারটি চালু কনটেইনার টার্মিনালের মধ্যে এনসিটিই সবচেয়ে বড়।",
            "গত বছর বন্দরে ওঠানো নামানো মোট কনটেইনারের শতাংশ পরিচালিত হয়েছে এই টার্মিনালের মাধ্যমে।",
            "২০২৪ সালের ৭ জুলাই থেকে নৌবাহিনীর প্রতিষ্ঠান টার্মিনালটি পরিচালনা করছে।",
            "গত বছর মোট কনটেইনারের ৪৪ শতাংশ পরিচালিত হয়েছে।",
        )
    }
}
