package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class KeyRow(val keys: String, val tap: String, val shift: String, val hold: String)

/** Tutorial for the "Amader" layout — our own phonetic-mnemonic fixed Bangla layout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmaderGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amader Layout — Guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "“Amader” (আমাদের) is our own Bangla layout. Unlike phonetic mode " +
                    "where you type romanized letters, here each key shows a real Bangla " +
                    "letter — placed where its English sound sits on QWERTY (k→ক, m→ম, " +
                    "a→আ). If you know QWERTY, you already know where the letters are.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("How to type", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Bullet("Tap a key to insert its Bangla letter.")
                    Bullet("Shift (⇧): the related variant — retroflex or long vowel (ত → ট, ই → ঈ).")
                    Bullet("Long-press: the aspirate form (ক → খ, গ → ঘ, ত → থ).")
                    Bullet("A vowel after a consonant becomes its kar sign automatically (ক + ই → কি).")
                    Bullet("Two consonants in a row join into a conjunct/juktakkhor (ক + ষ → ক্ষ).")
                    Bullet("Space, Backspace, suggestions and emoji work just like the other modes.")
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Key map", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    HeaderRow()
                    for (r in KEY_ROWS) KeyMapRow(r)
                    Text(
                        "Columns: key position · tap · Shift · long-press",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Examples", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Bullet("আমার:  আ , ম , আ , র   (the 2nd আ becomes the া kar)")
                    Bullet("কি:  ক , ই")
                    Bullet("খাবার:  long-press ক→খ , আ , ব , আ , র")
                    Bullet("ক্ষমা:  ক্ষ , ম , আ")
                }
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row {
        Text("•  ", color = MaterialTheme.colorScheme.primary)
        Text(text)
    }
}

@Composable
private fun HeaderRow() {
    Row(Modifier.fillMaxWidth()) {
        Cell("Keys", 1.4f, header = true)
        Cell("Tap", 1f, header = true)
        Cell("Shift", 1f, header = true)
        Cell("Hold", 1f, header = true)
    }
}

@Composable
private fun KeyMapRow(r: KeyRow) {
    Row(Modifier.fillMaxWidth()) {
        Cell(r.keys, 1.4f, mono = true)
        Cell(r.tap, 1f)
        Cell(r.shift, 1f)
        Cell(r.hold, 1f)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Cell(
    text: String,
    weight: Float,
    header: Boolean = false,
    mono: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
        fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
        color = if (header) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
    )
}

private val KEY_ROWS = listOf(
    KeyRow("q w e r t", "ক ও এ র ত", "খ ঔ ঐ ড় ট", "— — — — থ"),
    KeyRow("y u i o p", "য় উ ই অ প", "য ঊ ঈ আ ফ", "— — — — —"),
    KeyRow("a s d f g", "আ স দ ফ গ", "— ষ ড — ঘ", "অ শ ধ ভ —"),
    KeyRow("h j k l", "হ জ ক ল", "ঃ ঝ খ —", "— — — —"),
    KeyRow("z x c v", "য ক্ষ চ ভ", "— — ছ —", "— — — ব"),
    KeyRow("b n m", "ব ন ম", "ভ ণ ং", "— ঙ ঁ"),
)
