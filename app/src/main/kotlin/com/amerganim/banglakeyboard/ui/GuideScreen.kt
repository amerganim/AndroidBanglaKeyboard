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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A pair shown in the guide: how to type something and the result/example. */
private data class Entry(val type: String, val result: String)
private data class Section(val title: String, val entries: List<Entry>, val note: String? = null)

/** Full reference for the Bangla phonetic scheme (case-sensitive). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.amerganim.banglakeyboard.R.string.guide_title)) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Type a romanized word, then Space to commit. The scheme is " +
                    "case-sensitive — capitals mean different letters (t = ত but T = ট).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (section in SECTIONS) SectionCard(section)
            Text(
                "Tip: long-press a suggestion to remove it from your history. " +
                    "The keyboard learns the words you pick.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionCard(section: Section) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(section.title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            for (e in section.entries) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        e.type,
                        modifier = Modifier.weight(1f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(e.result, modifier = Modifier.weight(1.4f))
                }
            }
            section.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val SECTIONS = listOf(
    Section(
        "Vowels (independent / kar)",
        listOf(
            Entry("o", "অ — inherent, no sign after a consonant"),
            Entry("a", "আ / া  (kaj → কাজ)"),
            Entry("i", "ই / ি  (din → দিন)"),
            Entry("I", "ঈ / ী  (nodI → নদী)"),
            Entry("u", "উ / ু  (tumi → তুমি)"),
            Entry("U", "ঊ / ূ  (mUl → মূল)"),
            Entry("e", "এ / ে  (megh → মেঘ)"),
            Entry("O", "ও / ো  (nOka → নোকা)"),
            Entry("OI", "ঐ / ৈ"),
            Entry("OU", "ঔ / ৌ  (nOUka → নৌকা)"),
            Entry("rri", "ঋ / ৃ  (rriSi → ঋষি)"),
        ),
        note = "A vowel after a consonant becomes its kar sign; at a word start it is the full vowel.",
    ),
    Section(
        "Consonants",
        listOf(
            Entry("k kh g gh Ng", "ক খ গ ঘ ঙ"),
            Entry("ch chh j jh NG", "চ ছ জ ঝ ঞ"),
            Entry("T Th D Dh N", "ট ঠ ড ঢ ণ"),
            Entry("t th d dh n", "ত থ দ ধ ন"),
            Entry("p ph/f b bh/v m", "প ফ ব ভ ম"),
            Entry("z r l", "য র ল  (z = য, j = জ)"),
            Entry("sh S/Sh s h", "শ ষ স হ"),
            Entry("q c x", "ক চ ক্স"),
        ),
    ),
    Section(
        "Special consonants",
        listOf(
            Entry("R", "ড়  (rho)"),
            Entry("Rh", "ঢ়"),
            Entry("y / Y", "য়  (noy → নয়)"),
            Entry("t`", "ৎ  (khanda-ta)"),
            Entry("w", "ব — for bo-phola (swopno → স্বপ্ন)"),
        ),
    ),
    Section(
        "Signs",
        listOf(
            Entry("ng", "ং anusvara  (bangla → বাংলা)"),
            Entry("^", "ঁ chandrabindu  (cha^d → চাঁদ)"),
            Entry(":", "ঃ visarga  (du:kh → দুঃখ)"),
            Entry(".", "। dari (full stop)"),
            Entry("`", "্ hasanta (force half-form)"),
        ),
        note = "On the symbols page (?123): tap ঁ / ্ for the sign; long-press for the literal ^ / ` character.",
    ),
    Section(
        "Conjuncts (juktakkhor)",
        listOf(
            Entry("kk", "ক্ক"),
            Entry("kSh", "ক্ষ  (kShoma → ক্ষমা)"),
            Entry("nt nd", "ন্ত ন্দ"),
            Entry("st", "স্ত  (bistarito → বিস্তারিত)"),
            Entry("r`k", "র্ক (ref)"),
            Entry("k`z", "ক্য (ya-phala)"),
        ),
        note = "Type consonants with no vowel between them and they join automatically.",
    ),
    Section(
        "Digits",
        listOf(Entry("0 1 2 3 4 5 6 7 8 9", "০ ১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯")),
    ),
)
