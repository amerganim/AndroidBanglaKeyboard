package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Like [JuktakkhorTest] but exercises **smart-conjunct mode** — the mode the
 * keyboard uses when "smart juktakkhor" is enabled. Every conjunct in the bundled
 * list must still round-trip (reverse-romanize -> smart transliterate -> same
 * conjunct), so the smart gating (and its phonetic shortcuts) never makes a real
 * conjunct un-typeable.
 */
class JuktakkhorSmartTest {

    @Before fun loadConjuncts() {
        Conjuncts.setFromList(File("src/main/assets/juktakkhor.txt").readText())
    }

    private fun conjuncts(): List<String> =
        File("src/main/assets/juktakkhor.txt").readText()
            .lineSequence()
            .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
            .flatMap { it.trim().split(Regex("\\s+")).asSequence() }
            .filter { it.isNotBlank() }
            .map { Normalizer.normalize(it, Normalizer.Form.NFC) }
            .toList()

    @Test fun everyConjunctRoundTripsInSmartMode() {
        val all = conjuncts()
        assertTrue("juktakkhor list should load", all.size > 200)

        val failures = all.mapNotNull { jukto ->
            val roman = BanglaReverse.toRoman(jukto)
            val got = Transliterator.transliterate(roman, smart = true)
            if (got != jukto) "\"$jukto\" <- roman \"$roman\" -> got \"$got\"" else null
        }
        assertTrue(
            "Smart-mode conjuncts that don't round-trip (${failures.size}/${all.size}):\n" +
                failures.joinToString("\n"),
            failures.isEmpty(),
        )
    }
}
