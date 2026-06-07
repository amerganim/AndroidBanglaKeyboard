package com.amerganim.banglakeyboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** Whether the app UI is currently showing in Bangla. */
@Composable
fun isBanglaUi(): Boolean = LocalConfiguration.current.locales[0].language == "bn"

/** Pick [bn] or [en] based on the current UI language. */
@Composable
fun tr(bn: String, en: String): String = if (isBanglaUi()) bn else en
