package com.amerganim.banglakeyboard.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.amerganim.banglakeyboard.data.KeyboardPrefs
import java.util.Locale

/** The app's UI language ("en" / "bn"); Bangla unless the user picked English. */
fun appLanguage(context: Context): String = KeyboardPrefs(context).appLang.ifEmpty { "bn" }

/**
 * [base]'s [config] with its locale set to [lang]. Everything that shows text —
 * the setup screens, the keyboard, its toasts — goes through this so the whole app
 * follows the language chosen in the app, not the phone's system language.
 */
fun localizedContext(base: Context, lang: String, config: Configuration = base.resources.configuration): Context {
    val c = Configuration(config)
    c.setLocale(Locale.forLanguageTag(lang))
    return base.createConfigurationContext(c)
}

/** Whether the app UI is currently showing in Bangla. */
@Composable
fun isBanglaUi(): Boolean = LocalConfiguration.current.locales[0].language == "bn"

/** Pick [bn] or [en] based on the current UI language. */
@Composable
fun tr(bn: String, en: String): String = if (isBanglaUi()) bn else en
