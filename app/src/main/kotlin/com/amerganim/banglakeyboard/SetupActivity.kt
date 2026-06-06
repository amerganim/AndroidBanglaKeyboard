package com.amerganim.banglakeyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.data.KeySize
import com.amerganim.banglakeyboard.data.KeyboardPrefs
import com.amerganim.banglakeyboard.ui.GuideScreen
import com.amerganim.banglakeyboard.ui.PolicyScreen
import com.amerganim.banglakeyboard.ui.theme.BanglaKeyboardTheme

private enum class Screen { SETUP, GUIDE, POLICY }

/**
 * Launcher screen: walks the user through enabling/selecting the keyboard, lets
 * them pick a key size, links to the typing guide, and provides a try-it field.
 */
class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BanglaKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var screen by remember { mutableStateOf(Screen.SETUP) }
                    when (screen) {
                        Screen.GUIDE -> {
                            BackHandler { screen = Screen.SETUP }
                            GuideScreen(onBack = { screen = Screen.SETUP })
                        }
                        Screen.POLICY -> {
                            BackHandler { screen = Screen.SETUP }
                            PolicyScreen(onBack = { screen = Screen.SETUP })
                        }
                        Screen.SETUP -> SetupScreen(
                            onOpenGuide = { screen = Screen.GUIDE },
                            onOpenPolicy = { screen = Screen.POLICY },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(onOpenGuide: () -> Unit, onOpenPolicy: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { KeyboardPrefs(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Inset the scroll container by the system bars + keyboard so the
            // focused field stays in the visible area above the keyboard.
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Amader Bangla Keyboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Phonetic Bangla typing — type \"amar\" to get আমার.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Getting started", fontWeight = FontWeight.SemiBold)
                Text("1. Enable the keyboard in system settings.")
                Text("2. Switch to it with the keyboard picker.")
                Text("3. Tap the 🌐 globe key to switch English ⇄ Bangla.")
                Text("4. Tap ⇧ once for one capital (T → ট); it releases after one key.")
            }
        }

        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Enable keyboard in Settings") }

        FilledTonalButton(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Choose keyboard") }

        OutlinedButton(onClick = onOpenGuide, modifier = Modifier.fillMaxWidth()) {
            Text("Bangla typing guide")
        }

        OutlinedButton(onClick = onOpenPolicy, modifier = Modifier.fillMaxWidth()) {
            Text("Privacy policy")
        }

        Text("Key size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        var keySize by remember { mutableStateOf(prefs.keySize) }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            KeySize.entries.forEachIndexed { index, size ->
                SegmentedButton(
                    selected = keySize == size,
                    onClick = {
                        keySize = size
                        prefs.keySize = size
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, KeySize.entries.size),
                ) { Text(size.label) }
            }
        }
        Text(
            "The keyboard follows your system light/dark theme.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Try the keyboard here") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
