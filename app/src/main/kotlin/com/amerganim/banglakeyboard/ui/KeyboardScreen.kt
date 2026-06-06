package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amerganim.banglakeyboard.ime.KeyboardMode
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
                if (vm.mode == KeyboardMode.BANGLA_PHONETIC && vm.candidates.isNotEmpty()) {
                    CandidateStrip(
                        candidates = vm.candidates,
                        onClick = vm::onCandidate,
                    )
                }
                QwertyLayout(vm)
            }
        }
    }
}
