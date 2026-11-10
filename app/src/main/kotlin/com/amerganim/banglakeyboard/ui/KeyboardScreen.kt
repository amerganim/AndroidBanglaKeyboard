package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.ime.KeyboardViewModel
import com.amerganim.banglakeyboard.ui.theme.KeyboardColorTheme

/** Root of the keyboard: candidate strip above the QWERTY/Amader/symbol layout. */
@Composable
fun KeyboardScreen(vm: KeyboardViewModel) {
    // The keyboard's labels follow the language chosen in the app, not the phone's
    // system language. Rebuilt when either changes (the system config carries the
    // dark-mode flag that the System theme follows).
    val baseContext = LocalContext.current
    val systemConfig = LocalConfiguration.current
    val localized = remember(vm.uiLanguage, systemConfig) {
        localizedContext(baseContext, vm.uiLanguage, systemConfig)
    }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
    ) {
    KeyboardColorTheme(vm.theme) {
        CompositionLocalProvider(LocalKeyBorders provides vm.keyBorders) {
        Surface(color = MaterialTheme.colorScheme.background) {
            // On wide screens (tablets / landscape) cap the keys' width and centre
            // them so they don't stretch into oversized keys.
            Box(Modifier.fillMaxWidth().navigationBarsPadding(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                    // Always reserve the strip's space (both modes) so the keyboard
                    // doesn't shift down when suggestions appear — a moving keyboard
                    // causes mis-taps when typing fast (e.g. "amar" -> "akar").
                    CandidateStrip(
                        candidates = vm.candidates,
                        onClick = vm::onCandidate,
                        onLongPress = vm::onForgetCandidate,
                        onMicStart = vm::micPressStart,
                        onMicStop = vm::micPressEnd,
                        listening = vm.listening,
                        onClipboard = vm::toggleClipboard,
                        clipboardOpen = vm.clipboardPanel,
                    )
                    QwertyLayout(vm)
                }
            }
        }
        }
    }
    }
}
