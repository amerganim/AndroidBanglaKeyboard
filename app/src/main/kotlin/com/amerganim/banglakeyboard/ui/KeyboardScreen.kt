package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amerganim.banglakeyboard.ime.KeyboardMode
import com.amerganim.banglakeyboard.ime.KeyboardViewModel

/** Root of the keyboard: optional candidate strip above the QWERTY layout. */
@Composable
fun KeyboardScreen(vm: KeyboardViewModel) {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(Modifier.fillMaxWidth()) {
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
