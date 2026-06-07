package com.amerganim.banglakeyboard.data

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** User-selectable key size; controls the height of each keyboard row. */
enum class KeySize(val label: String, val rowHeight: Dp) {
    SMALL("Small", 46.dp),
    MEDIUM("Medium", 54.dp),
    LARGE("Large", 64.dp),
}

/** Lightweight persisted keyboard settings backed by SharedPreferences. */
class KeyboardPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    var keySize: KeySize
        get() = runCatching { KeySize.valueOf(prefs.getString(KEY_SIZE, null) ?: "") }
            .getOrDefault(KeySize.MEDIUM)
        set(value) = prefs.edit().putString(KEY_SIZE, value.name).apply()

    /** Whether a click sound plays on each key press. */
    var keySound: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    /** Smart conjunct: only join consonants that form a real juktakkhor. On by default. */
    var smartConjunct: Boolean
        get() = prefs.getBoolean(SMART_CONJUNCT, true)
        set(value) = prefs.edit().putBoolean(SMART_CONJUNCT, value).apply()

    private companion object {
        const val KEY_SIZE = "key_size"
        const val KEY_SOUND = "key_sound"
        const val SMART_CONJUNCT = "smart_conjunct"
    }
}
