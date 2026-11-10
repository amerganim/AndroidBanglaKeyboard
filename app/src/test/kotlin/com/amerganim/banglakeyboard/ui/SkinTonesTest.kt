package com.amerganim.banglakeyboard.ui

import com.amerganim.banglakeyboard.ui.theme.KeyboardTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinTonesTest {

    @Test fun thumbsUpHasDefaultPlusFiveTones() {
        val v = SkinTones.variants("👍")
        assertEquals(6, v.size)
        assertEquals("👍", v[0])
        assertEquals("👍🏽", v[3]) // medium tone
    }

    @Test fun presentationSelectorIsDroppedBeforeTheModifier() {
        // ✌️ is U+270C U+FE0F; the toned form is U+270C + modifier, no FE0F.
        val v = SkinTones.variants("✌️")
        assertEquals("✌🏻", v[1])
        assertTrue(v.drop(1).none { it.contains('️') })
    }

    @Test fun nonPersonEmojiHasNoTones() {
        assertTrue(SkinTones.variants("😀").isEmpty())
        assertTrue(SkinTones.variants("🤝").isEmpty()) // multi-person: deliberately excluded
        assertNull(SkinTones.baseOf("🐶"))
    }

    @Test fun baseOfRecoversTheToneless() {
        assertEquals("👍", SkinTones.baseOf("👍🏾"))
        assertEquals("✌️", SkinTones.baseOf("✌🏽"))
        assertEquals("👋", SkinTones.baseOf("👋"))
    }

    @Test fun choicesRoundTripAndBadEntriesAreDropped() {
        val choices = mapOf("👍" to "👍🏿", "✌️" to "✌🏼")
        assertEquals(choices, SkinTones.decode(SkinTones.encode(choices)))
        // A variant that doesn't belong to its base is ignored.
        assertTrue(SkinTones.decode("👍\u001F😀").isEmpty())
        assertTrue(SkinTones.decode("").isEmpty())
    }

    @Test fun everyThemeHasDistinctKeyAndTextColors() {
        for (t in KeyboardTheme.entries) {
            for (dark in listOf(false, true)) {
                val c = t.colors(dark)
                assertTrue("$t text vs key", c.onSurface != c.surfaceContainerLowest)
                assertTrue("$t accent vs onAccent", c.primary != c.onPrimary)
            }
        }
        assertEquals(KeyboardTheme.SYSTEM, KeyboardTheme.fromName("nonsense"))
        assertEquals(KeyboardTheme.OCEAN, KeyboardTheme.fromName("OCEAN"))
    }
}
