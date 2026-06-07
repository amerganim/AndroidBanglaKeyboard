package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** In-app privacy policy so users can read it without leaving the app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.amerganim.banglakeyboard.R.string.policy_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.amerganim.banglakeyboard.R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Section("Summary") {
                Text(
                    "Amader Bangla Keyboard does not collect, transmit, or share any of " +
                        "your data. Everything stays on your device. The app has no internet " +
                        "permission, so it cannot send your typing anywhere.",
                )
            }
            Section("What stays on your device") {
                Text(
                    "Your personal dictionary, how often you pick suggestions, the next-word " +
                        "statistics learned from your typing, and your key-size preference are " +
                        "stored only in the app's private storage. Long-press a suggestion to " +
                        "forget it. Uninstalling the app deletes everything.",
                )
            }
            Section("Sensitive fields") {
                Text(
                    "In password fields and fields that opt out of personalized learning, the " +
                        "app shows no suggestions and learns nothing.",
                )
            }
            Section("Permissions") {
                Text(
                    "• Bind input method: required for any keyboard.\n" +
                        "• Microphone: requested only when you tap the voice (🎤) key. If you " +
                        "never use voice typing, it is never requested.\n" +
                        "• No internet, contacts, location, or storage permissions.",
                )
            }
            Section("Voice typing") {
                Text(
                    "Voice typing is optional. When you use the 🎤 key, the app uses your " +
                        "device's system speech-recognition service to convert speech to text. " +
                        "The app itself does not record, store, or transmit audio, but the " +
                        "system speech service may process audio under its own privacy policy " +
                        "and, depending on your device, may send it to its provider. If you " +
                        "prefer audio never to leave your device, simply don't use the 🎤 key.",
                )
            }
            Section("Data sharing") {
                Text("None. No analytics, no ads, no third-party data-collecting SDKs.")
            }
            Section("Contact") {
                Text("ganim09@gmail.com")
            }
        }
    }
}

@Composable
private fun Section(title: String, body: @Composable () -> Unit) {
    Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    body()
}
