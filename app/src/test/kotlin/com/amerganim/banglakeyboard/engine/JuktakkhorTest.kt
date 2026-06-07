package com.amerganim.banglakeyboard.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Verifies the phonetic engine can produce every Bangla conjunct (juktakkhor) in
 * the bundled list. Each conjunct is reverse-transliterated to its canonical
 * romanization and run back through [Transliterator]; the result must match.
 *
 * Because the Amader layout feeds the same engine (and every roman token is on an
 * Amader key), passing here means both phonetic and Amader can type every conjunct.
 */
class JuktakkhorTest {

    private fun conjuncts(): List<String> {
        // Unit tests run with the module dir (app/) as the working directory.
        val text = File("src/main/assets/juktakkhor.txt").readText()
        return text.lineSequence()
            .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
            .flatMap { it.trim().split(Regex("\\s+")).asSequence() }
            .filter { it.isNotBlank() }
            .map { Normalizer.normalize(it, Normalizer.Form.NFC) }
            .toList()
    }

    @Test fun everyConjunctRoundTrips() {
        val all = conjuncts()
        assertTrue("juktakkhor list should load", all.size > 200)

        val failures = all.mapNotNull { jukto ->
            val roman = BanglaReverse.toRoman(jukto)
            val got = Transliterator.transliterate(roman)
            if (got != jukto) "\"$jukto\" -> roman \"$roman\" -> \"$got\"" else null
        }
        assertTrue(
            "Conjuncts that don't round-trip (${failures.size}/${all.size}):\n" +
                failures.joinToString("\n"),
            failures.isEmpty(),
        )
    }
}
