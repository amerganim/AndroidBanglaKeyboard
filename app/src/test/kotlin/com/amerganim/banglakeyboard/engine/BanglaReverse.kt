package com.amerganim.banglakeyboard.engine

/**
 * Test-only inverse of [Transliterator]: converts Bangla text into the canonical
 * romanization that the engine should map back to the same Bangla. Used to build
 * round-trip regression tests from arbitrary Bangla text — feed in any passage and
 * assert `transliterate(toRoman(text)) == text`.
 *
 * It models the same rules as the forward engine: a consonant carries an inherent
 * vowel (emitted as `o`) unless it is followed by a vowel sign (kar) or a hasanta
 * (which joins it into a conjunct, emitting no vowel).
 */
object BanglaReverse {

    private const val HASANTA = '্'
    private const val KHANDA_TA = 'ৎ'

    // ড় ঢ় য় via code points — typed char literals would decompose to base + nukta.
    private val RRA = Char(0x09DC)
    private val RHA = Char(0x09DD)
    private val YA_NUKTA = Char(0x09DF)

    private val consonant: Map<Char, String> = mapOf(
        'ক' to "k", 'খ' to "kh", 'গ' to "g", 'ঘ' to "gh", 'ঙ' to "Ng",
        'চ' to "ch", 'ছ' to "chh", 'জ' to "j", 'ঝ' to "jh", 'ঞ' to "NG",
        'ট' to "T", 'ঠ' to "Th", 'ড' to "D", 'ঢ' to "Dh", 'ণ' to "N",
        'ত' to "t", 'থ' to "th", 'দ' to "d", 'ধ' to "dh", 'ন' to "n",
        'প' to "p", 'ফ' to "ph", 'ব' to "b", 'ভ' to "bh", 'ম' to "m",
        'য' to "z", 'র' to "r", 'ল' to "l", 'শ' to "sh", 'ষ' to "S",
        'স' to "s", 'হ' to "h", RRA to "R", RHA to "Rh", YA_NUKTA to "y",
    )

    private val independentVowel: Map<Char, String> = mapOf(
        'অ' to "o", 'আ' to "a", 'ই' to "i", 'ঈ' to "I", 'উ' to "u",
        'ঊ' to "U", 'ঋ' to "rri", 'এ' to "e", 'ঐ' to "OI", 'ও' to "O", 'ঔ' to "OU",
    )

    private val kar: Map<Char, String> = mapOf(
        'া' to "a", 'ি' to "i", 'ী' to "I", 'ু' to "u",
        'ূ' to "U", 'ৃ' to "rri", 'ে' to "e", 'ৈ' to "OI",
        'ো' to "O", 'ৌ' to "OU",
    )

    private val sign: Map<Char, String> = mapOf('ং' to "ng", 'ঁ' to "^", 'ঃ' to ":", '।' to ".")

    private val digit: Map<Char, String> = mapOf(
        '০' to "0", '১' to "1", '২' to "2", '৩' to "3", '৪' to "4",
        '৫' to "5", '৬' to "6", '৭' to "7", '৮' to "8", '৯' to "9",
    )

    fun toRoman(bangla: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < bangla.length) {
            val ch = bangla[i]
            val c = consonant[ch]
            when {
                c != null -> {
                    sb.append(c)
                    val next = if (i + 1 < bangla.length) bangla[i + 1] else null
                    val karRoman = next?.let { kar[it] }
                    when {
                        karRoman != null -> { sb.append(karRoman); i += 2 }
                        next == HASANTA -> {
                            // Conjunct: no vowel. Khanda-ta (ৎ) doesn't auto-join, so
                            // the user must type an explicit hasanta (`) before it.
                            if (i + 2 < bangla.length && bangla[i + 2] == KHANDA_TA) sb.append("`")
                            i += 2
                        }
                        else -> { sb.append("o"); i += 1 } // inherent vowel
                    }
                }
                independentVowel[ch] != null -> { sb.append(independentVowel[ch]); i++ }
                sign[ch] != null -> { sb.append(sign[ch]); i++ }
                digit[ch] != null -> { sb.append(digit[ch]); i++ }
                ch == KHANDA_TA -> { sb.append("t`"); i++ }
                else -> { sb.append(ch); i++ } // spaces / punctuation
            }
        }
        return sb.toString()
    }
}
