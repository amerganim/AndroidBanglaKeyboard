package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amerganim.banglakeyboard.ime.KeyboardViewModel
import com.amerganim.banglakeyboard.ui.theme.BanglaKeyboardTheme

/** Root of the keyboard: optional candidate strip above the QWERTY layout. */
@Composable
fun KeyboardScreen(vm: KeyboardViewModel) {
    BanglaKeyboardTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            // Lift the keys above the system navigation bar so the bottom row
            // isn't hidden behind / fighting touches with the phone's nav buttons.
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                // Always reserve the strip's space (both modes) so the keyboard
                // doesn't shift down when suggestions appear — a moving keyboard
                // causes mis-taps when typing fast (e.g. "amar" -> "akar").
                CandidateStrip(
                    candidates = vm.candidates,
                    onClick = vm::onCandidate,
                    onLongPress = vm::onForgetCandidate,
                    onMic = vm::onMic,
                    listening = vm.listening,
                )
                QwertyLayout(vm)
            }
        }
    }
}
