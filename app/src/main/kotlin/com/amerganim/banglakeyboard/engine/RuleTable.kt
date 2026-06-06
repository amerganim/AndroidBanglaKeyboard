package com.amerganim.banglakeyboard.engine

/**
 * The phonetic rule set: a curated, self-consistent Avro-style scheme.
 *
 * This is a direct Kotlin port of the table in the Windows keyboard's
 * `engine/src/Transliterator.cpp`. The scheme is **case-sensitive**
 * (`t` = ত but `T` = ট; `ng` = ং anusvara but `Ng` = ঙ the velar-nasal letter).
 *
 * The [Transliterator] consumes this table with a greedy longest-match
 * tokenizer, so multi-letter phonemes (e.g. `chh`, `Dh`, `OI`) win over their
 * single-letter prefixes.
 */
internal enum class Kind {
    /** Carries the inherent vowel; [Unit.main] is the base glyph. */
    CONSONANT,

    /** [Unit.main] = independent form, [Unit.kar] = dependent sign. */
    VOWEL,

    /** Emitted verbatim regardless of context (digits, signs, etc.). */
    DIRECT,
}

/**
 * One rule-table entry.
 *
 * @property kar only meaningful for [Kind.VOWEL]; empty means the inherent vowel.
 */
internal data class Unit(
    val kind: Kind,
    val main: String,
    val kar: String = "",
)

internal object RuleTable {

    /** The hasanta / virama that joins consonants into a conjunct (juktakkhor). */
    const val HASANTA: String = "্" // ্

    /** Longest key currently is "chh". */
    const val MAX_KEY_LEN: Int = 3

    val table: Map<String, Unit> = buildMap {
        // ---- Consonants (longest keys must exist for greedy matching) ----
        put("k", Unit(Kind.CONSONANT, "ক"))
        put("kh", Unit(Kind.CONSONANT, "খ"))
        put("g", Unit(Kind.CONSONANT, "গ"))
        put("gh", Unit(Kind.CONSONANT, "ঘ"))
        put("Ng", Unit(Kind.CONSONANT, "ঙ")) // velar-nasal letter ঙ
        put("NG", Unit(Kind.CONSONANT, "ঞ")) // letter ঞ (ny)
        put("ch", Unit(Kind.CONSONANT, "চ"))
        put("chh", Unit(Kind.CONSONANT, "ছ"))
        put("j", Unit(Kind.CONSONANT, "জ"))
        put("jh", Unit(Kind.CONSONANT, "ঝ"))
        put("T", Unit(Kind.CONSONANT, "ট"))
        put("Th", Unit(Kind.CONSONANT, "ঠ"))
        put("D", Unit(Kind.CONSONANT, "ড"))
        put("Dh", Unit(Kind.CONSONANT, "ঢ"))
        put("N", Unit(Kind.CONSONANT, "ণ"))
        put("t", Unit(Kind.CONSONANT, "ত"))
        put("th", Unit(Kind.CONSONANT, "থ"))
        put("d", Unit(Kind.CONSONANT, "দ"))
        put("dh", Unit(Kind.CONSONANT, "ধ"))
        put("n", Unit(Kind.CONSONANT, "ন"))
        put("p", Unit(Kind.CONSONANT, "প"))
        put("ph", Unit(Kind.CONSONANT, "ফ"))
        put("f", Unit(Kind.CONSONANT, "ফ"))
        put("b", Unit(Kind.CONSONANT, "ব"))
        put("w", Unit(Kind.CONSONANT, "ব")) // bo-phola use: `swopno` -> স্বপ্ন
        put("bh", Unit(Kind.CONSONANT, "ভ"))
        put("v", Unit(Kind.CONSONANT, "ভ"))
        put("m", Unit(Kind.CONSONANT, "ম"))
        put("z", Unit(Kind.CONSONANT, "য")) // antastha ya (য); জ is `j`
        put("r", Unit(Kind.CONSONANT, "র"))
        put("l", Unit(Kind.CONSONANT, "ল"))
        put("sh", Unit(Kind.CONSONANT, "শ"))
        put("S", Unit(Kind.CONSONANT, "ষ"))
        put("Sh", Unit(Kind.CONSONANT, "ষ"))
        put("s", Unit(Kind.CONSONANT, "স"))
        put("h", Unit(Kind.CONSONANT, "হ"))
        put("R", Unit(Kind.CONSONANT, "ড়"))
        put("Rh", Unit(Kind.CONSONANT, "ঢ়"))
        put("y", Unit(Kind.CONSONANT, "য়")) // ya with nukta (য়)
        put("Y", Unit(Kind.CONSONANT, "য়")) // alias of `y` -> য়

        // ---- Vowels: {independent, dependent-sign} ----
        put("o", Unit(Kind.VOWEL, "অ")) // inherent vowel: no kar after consonant
        put("a", Unit(Kind.VOWEL, "আ", "া"))
        put("rri", Unit(Kind.VOWEL, "ঋ", "ৃ"))
        put("i", Unit(Kind.VOWEL, "ই", "ি"))
        put("I", Unit(Kind.VOWEL, "ঈ", "ী"))
        put("u", Unit(Kind.VOWEL, "উ", "ু"))
        put("U", Unit(Kind.VOWEL, "ঊ", "ূ"))
        put("e", Unit(Kind.VOWEL, "এ", "ে"))
        put("O", Unit(Kind.VOWEL, "ও", "ো"))
        put("OI", Unit(Kind.VOWEL, "ঐ", "ৈ"))
        put("OU", Unit(Kind.VOWEL, "ঔ", "ৌ"))

        // ---- Combining marks (attach to the preceding cluster; emitted
        // verbatim and do not start a consonant context) ----
        put("ng", Unit(Kind.DIRECT, "ং")) // anusvara (e.g. বাংলা)
        put("^", Unit(Kind.DIRECT, "ঁ")) // chandrabindu (e.g. চাঁদ)
        put(":", Unit(Kind.DIRECT, "ঃ")) // visarga
        put(".", Unit(Kind.DIRECT, "।")) // dari (Bangla full stop)
        put("`", Unit(Kind.DIRECT, "্")) // explicit hasanta (for ref/ya-phala)
        put("t`", Unit(Kind.DIRECT, "ৎ")) // khanda-ta (U+09CE)

        // ---- Digits ----
        put("0", Unit(Kind.DIRECT, "০"))
        put("1", Unit(Kind.DIRECT, "১"))
        put("2", Unit(Kind.DIRECT, "২"))
        put("3", Unit(Kind.DIRECT, "৩"))
        put("4", Unit(Kind.DIRECT, "৪"))
        put("5", Unit(Kind.DIRECT, "৫"))
        put("6", Unit(Kind.DIRECT, "৬"))
        put("7", Unit(Kind.DIRECT, "৭"))
        put("8", Unit(Kind.DIRECT, "৮"))
        put("9", Unit(Kind.DIRECT, "৯"))
    }
}
