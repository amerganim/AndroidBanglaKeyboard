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
                tr(
                    "রোমান অক্ষরে শব্দ লিখুন, তারপর স্পেস চেপে নিশ্চিত করুন। স্কিমটি বড়/ছোট-হাতের " +
                        "সংবেদনশীল — বড় হাতের অক্ষর ভিন্ন বর্ণ বোঝায় (t = ত কিন্তু T = ট)।",
                    "Type a romanized word, then Space to commit. The scheme is " +
                        "case-sensitive — capitals mean different letters (t = ত but T = ট).",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            for (section in guideSections()) SectionCard(section)
            Text(
                tr(
                    "টিপ: কোনো সাজেশন ইতিহাস থেকে সরাতে সেটির উপর দীর্ঘক্ষণ চাপুন। কিবোর্ড আপনার " +
                        "বেছে নেওয়া শব্দ শেখে।",
                    "Tip: long-press a suggestion to remove it from your history. " +
                        "The keyboard learns the words you pick.",
                ),
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

@Composable
private fun guideSections(): List<Section> = listOf(
    Section(
        tr("স্বরবর্ণ (স্বাধীন / কার)", "Vowels (independent / kar)"),
        listOf(
            Entry("o", tr("অ — অন্তর্নিহিত, ব্যঞ্জনের পরে কোনো চিহ্ন নয়", "অ — inherent, no sign after a consonant")),
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
        note = tr(
            "ব্যঞ্জনের পরে স্বর কার-চিহ্ন হয়ে যায়; শব্দের শুরুতে তা পূর্ণ স্বর।",
            "A vowel after a consonant becomes its kar sign; at a word start it is the full vowel.",
        ),
    ),
    Section(
        tr("ব্যঞ্জনবর্ণ", "Consonants"),
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
        tr("বিশেষ ব্যঞ্জন", "Special consonants"),
        listOf(
            Entry("R", "ড়  (rho)"),
            Entry("Rh", "ঢ়"),
            Entry("y / Y", "য়  (noy → নয়)"),
            Entry("t`", tr("ৎ  (খণ্ড-ত)", "ৎ  (khanda-ta)")),
            Entry("w", tr("ব — ব-ফলার জন্য (swopno → স্বপ্ন)", "ব — for bo-phola (swopno → স্বপ্ন)")),
        ),
    ),
    Section(
        tr("চিহ্ন", "Signs"),
        listOf(
            Entry("ng", tr("ং অনুস্বার  (bangla → বাংলা)", "ং anusvara  (bangla → বাংলা)")),
            Entry("^", tr("ঁ চন্দ্রবিন্দু  (cha^d → চাঁদ)", "ঁ chandrabindu  (cha^d → চাঁদ)")),
            Entry(":", tr("ঃ বিসর্গ  (du:kh → দুঃখ)", "ঃ visarga  (du:kh → দুঃখ)")),
            Entry(".", tr("। দাঁড়ি (পূর্ণচ্ছেদ)", "। dari (full stop)")),
            Entry("`", tr("্ হসন্ত (জোর করে অর্ধরূপ)", "্ hasanta (force half-form)")),
        ),
        note = tr(
            "সিম্বল পেজে (?123): চিহ্নের জন্য ঁ / ্ চাপুন; আক্ষরিক ^ / ` -এর জন্য দীর্ঘক্ষণ চাপুন।",
            "On the symbols page (?123): tap ঁ / ্ for the sign; long-press for the literal ^ / ` character.",
        ),
    ),
    Section(
        tr("যুক্তাক্ষর", "Conjuncts (juktakkhor)"),
        listOf(
            Entry("kk kt", "ক্ক ক্ত"),
            Entry("kSh", "ক্ষ  (kShoma → ক্ষমা)"),
            Entry("gg", tr("জ্ঞ — উচ্চারণে (biggan → বিজ্ঞান)", "জ্ঞ — by sound (biggan → বিজ্ঞান)")),
            Entry("nch nj", tr("ঞ্চ ঞ্জ — উচ্চারণে (onchol → অঞ্চল)", "ঞ্চ ঞ্জ — by sound (onchol → অঞ্চল)")),
            Entry("tt tth", "ত্ত ত্থ"),
            Entry("nt nd ndh", "ন্ত ন্দ ন্ধ"),
            Entry("st sth sp", "স্ত স্থ স্প  (bistarito → বিস্তারিত)"),
            Entry("ST STh SN", "ষ্ট ষ্ঠ ষ্ণ"),
            Entry("cc jj", "চ্চ জ্জ"),
            Entry("ll nn mm", "ল্ল ন্ন ম্ম"),
            Entry("hm hn", "হ্ম হ্ন"),
        ),
        note = tr(
            "মাঝে স্বর ছাড়া পরপর ব্যঞ্জন লিখলেই যুক্তাক্ষর তৈরি হয়। স্মার্ট যুক্তাক্ষর চালু " +
                "থাকলে শুধু প্রকৃত যুক্তাক্ষরই যুক্ত হয়, তাই “zkhn” → যখন।",
            "Type consonants with no vowel between them to form a conjunct. With Smart " +
                "conjunct on, only real juktakkhor join, so “zkhn” → যখন.",
        ),
    ),
    Section(
        tr("ফলা", "Phala forms"),
        listOf(
            Entry("rk rm", tr("র্ক র্ম — রেফ (r + ব্যঞ্জন)", "র্ক র্ম — ref (r + consonant)")),
            Entry("kr pr gr", tr("ক্র প্র গ্র — র-ফলা", "ক্র প্র গ্র — ra-phala")),
            Entry("kz bz", tr("ক্য ব্য — য-ফলা (z = য)", "ক্য ব্য — ya-phala (z = য)")),
            Entry("sw tw", tr("স্ব ত্ব — ব-ফলা (w = ব)", "স্ব ত্ব — bo-phala (w = ব)")),
        ),
        note = tr(
            "কঠিন ক্ষেত্রে জোর করে হসন্ত দিতে ` ব্যবহার করুন (r`k → র্ক)।",
            "Use ` to force a hasanta where needed (r`k → র্ক).",
        ),
    ),
    Section(
        tr("সংখ্যা", "Digits"),
        listOf(Entry("0 1 2 3 4 5 6 7 8 9", "০ ১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯")),
    ),
)
