package com.amerganim.banglakeyboard.ui

/**
 * Emoji skin tones. People and hand emojis can carry one of the five Fitzpatrick
 * modifiers (🏻🏼🏽🏾🏿); long-pressing one in the emoji panel offers all six versions,
 * and the one picked becomes that emoji's default.
 */
object SkinTones {

    /** The five skin-tone modifiers, light to dark. */
    val MODIFIERS = listOf("🏻", "🏼", "🏽", "🏾", "🏿")

    private const val VS16 = "️" // emoji presentation selector

    /**
     * Emojis in our panel that accept a skin-tone modifier. Only single-person
     * emojis are listed: multi-person ones (🤝, couples) need per-person tones,
     * which many fonts render inconsistently.
     */
    private val SUPPORTED: Set<String> = (
        "👍 👎 👌 ✌️ 🤞 🤟 🤘 👏 🙌 👐 🙏 💪 👈 👉 👆 👇 ☝️ ✋ 🤚 🖐️ 🖖 👋 🤙 ✊ 👊 🤛 🤜 " +
            "💅 👶 🧒 👦 👧 🧑 👨 👩 🧓 👴 👵 🧔 👮 👷 💂 🕵️ 👰 🤵 👸 🤴 🦸 🦹 🤰 🤱 👼 🎅 🤶 " +
            "🤳 🙋 🙆 🙅 🤷 🤦 🙇 💁 🙎 🙍 💆 💇 🚶 🏃 💃 🕺 🧘 🛀"
        ).split(" ").filter { it.isNotBlank() }.toSet()

    fun supports(emoji: String): Boolean = emoji in SUPPORTED

    /**
     * The base (toneless) emoji of [emoji], e.g. 👍🏾 → 👍 and ✌🏽 → ✌️; null if it
     * isn't a skin-tone emoji at all. Lets a toned emoji in "recent" offer the tones too.
     */
    fun baseOf(emoji: String): String? {
        if (emoji in SUPPORTED) return emoji
        var core = emoji
        for (m in MODIFIERS) core = core.replace(m, "")
        return when {
            core in SUPPORTED -> core
            core + VS16 in SUPPORTED -> core + VS16
            else -> null
        }
    }

    /**
     * The tone variants of [base]: the default (yellow) emoji followed by one per
     * modifier, or an empty list if [base] takes no skin tone. The emoji-presentation
     * selector is dropped before the modifier, as Unicode specifies (✌️ → ✌🏽).
     */
    fun variants(base: String): List<String> {
        if (!supports(base)) return emptyList()
        val core = base.removeSuffix(VS16)
        return listOf(base) + MODIFIERS.map { core + it }
    }

    /** Persisted per-emoji choices: `base<US>variant` pairs separated by RS. */
    fun encode(choices: Map<String, String>): String =
        choices.entries.joinToString(RS) { it.key + US + it.value }

    fun decode(data: String): Map<String, String> =
        data.split(RS).mapNotNull { pair ->
            val i = pair.indexOf(US)
            if (i <= 0) null else pair.substring(0, i) to pair.substring(i + 1)
        }.filter { (base, variant) -> variant in variants(base) }.toMap()

    private const val US = "\u001F" // unit separator
    private const val RS = "\u001E" // record separator
}
