package com.amerganim.banglakeyboard.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.R

private data class Faq(val question: String, val answer: String)

/**
 * Support screen: an expandable FAQ plus the two ways to reach us — a private
 * email draft, or a public Play Store rating.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val emailSubject = stringResource(R.string.feedback_subject)
    val emailHeader = stringResource(R.string.feedback_body_header)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                tr(
                    "সাধারণ প্রশ্নের উত্তর নিচে দেওয়া হলো। উত্তর দেখতে প্রশ্নে চাপুন।",
                    "Answers to common questions. Tap a question to see the answer.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            for (faq in faqs()) FaqCard(faq)

            Text(
                tr("আরও সাহায্য দরকার?", "Still need help?"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                tr(
                    "সরাসরি আমাদের লিখুন — এটি শুধু আপনার ইমেইল অ্যাপে একটি খসড়া খোলে, " +
                        "অ্যাপ নিজে কিছু পাঠায় না।",
                    "Write to us directly — this only opens a draft in your email app; " +
                        "the app itself sends nothing.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(
                onClick = { sendFeedbackEmail(context, emailSubject, emailHeader) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.send_feedback)) }

            OutlinedButton(
                onClick = { openPlayStoreListing(context) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.rate_this_app)) }

            Text(
                SUPPORT_EMAIL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun FaqCard(faq: Faq) {
    var expanded by rememberSaveable(faq.question) { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(faq.question, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Text(faq.answer, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun faqs(): List<Faq> = listOf(
    Faq(
        tr("কিবোর্ডটি দেখা যাচ্ছে না — কী করব?", "The keyboard doesn't show up — what do I do?"),
        tr(
            "প্রথমে সেটআপ পর্দার “সেটিংসে কিবোর্ড চালু করুন” দিয়ে এটি চালু করুন, তারপর " +
                "“কিবোর্ড নির্বাচন করুন” দিয়ে এটিকে সক্রিয় কিবোর্ড বানান। চালু করা আর নির্বাচন " +
                "করা দুটি আলাদা ধাপ — দুটিই করতে হবে।",
            "First turn it on with “Enable keyboard in Settings” on the setup screen, then make " +
                "it the active keyboard with “Choose keyboard”. Enabling and selecting are two " +
                "separate steps — you need both.",
        ),
    ),
    Faq(
        tr(
            "Android বলছে এই কিবোর্ড আমার লেখা “সংগ্রহ করতে পারে” — এটা কি নিরাপদ?",
            "Android says this keyboard “may collect what you type” — is it safe?",
        ),
        tr(
            "এটি প্রতিটি কিবোর্ডের জন্য Android-এর স্ট্যান্ডার্ড সতর্কবার্তা, এমনকি Gboard-এর জন্যও। " +
                "এটি বলে কিবোর্ড কারিগরিভাবে কী করতে পারে, এই অ্যাপ কী করে তা নয়। আমাদের বাংলা " +
                "কিবোর্ডের কোনো ইন্টারনেট অনুমতি নেই, তাই এটি কোথাও কিছু পাঠাতে পারে না।",
            "That is Android's standard warning for every keyboard, including Gboard. It describes " +
                "what a keyboard could technically do, not what this app does. Amader Bangla " +
                "Keyboard has no internet permission, so it cannot send anything anywhere.",
        ),
    ),
    Faq(
        tr("ইংরেজি ও বাংলার মধ্যে কীভাবে বদলাব?", "How do I switch between English and Bangla?"),
        tr(
            "🌐 গ্লোব কী চাপুন। এটি তিনটি মোডের মধ্যে ঘোরে: English → বাংলা (ফোনেটিক) → " +
                "আমাদের (ফিক্সড লেআউট)।",
            "Tap the 🌐 globe key. It cycles through three modes: English → Bangla (phonetic) → " +
                "Amader (fixed layout).",
        ),
    ),
    Faq(
        tr("ট আর ত-এর পার্থক্য কীভাবে লিখব?", "How do I type ট versus ত?"),
        tr(
            "স্কিমটি বড়/ছোট-হাতের সংবেদনশীল: t = ত, T = ট। একটি বড় হাতের অক্ষরের জন্য ⇧ একবার " +
                "চাপুন — একটি কী-র পরেই তা ছেড়ে যায়। একই নিয়ম d/D, n/N, s/S-এর জন্যও।",
            "The scheme is case-sensitive: t = ত, T = ট. Tap ⇧ once for a single capital — it " +
                "releases after one key. The same applies to d/D, n/N and s/S.",
        ),
    ),
    Faq(
        tr("যুক্তাক্ষর কীভাবে লিখব?", "How do I type a juktakkhor (conjunct)?"),
        tr(
            "মাঝে স্বর ছাড়া পরপর ব্যঞ্জন লিখুন — kt → ক্ত, kSh → ক্ষ। কঠিন ক্ষেত্রে জোর করে " +
                "হসন্ত দিতে ` ব্যবহার করুন (r`k → র্ক)। পূর্ণ তালিকা বাংলা টাইপিং গাইডে আছে।",
            "Type consonants with no vowel between them — kt → ক্ত, kSh → ক্ষ. Use ` to force a " +
                "hasanta where needed (r`k → র্ক). The Bangla typing guide has the full table.",
        ),
    ),
    Faq(
        tr("ভুল সাজেশন কীভাবে সরাব?", "How do I remove a wrong suggestion?"),
        tr(
            "সাজেশন বারে সেটির উপর দীর্ঘক্ষণ চাপুন — কিবোর্ড তা ভুলে যাবে।",
            "Long-press it in the suggestion bar and the keyboard will forget it.",
        ),
    ),
    Faq(
        tr("ভয়েস টাইপিং কাজ করছে না।", "Voice typing isn't working."),
        tr(
            "মাইক্রোফোন কী-তে প্রথমবার চাপলে মাইক্রোফোনের অনুমতি চাওয়া হয় — সেটি অনুমোদন করুন। " +
                "ভয়েস টাইপিং Android-এর নিজস্ব স্পিচ সার্ভিস ব্যবহার করে, তাই এর জন্য ইন্টারনেট " +
                "লাগতে পারে এবং কিছু ডিভাইসে সেবাটি না-ও থাকতে পারে।",
            "The first tap on the microphone key asks for microphone permission — allow it. Voice " +
                "typing uses Android's own speech service, so it may need internet and may be " +
                "unavailable on some devices.",
        ),
    ),
    Faq(
        tr("অ্যাপটি কি অফলাইনে কাজ করে?", "Does the app work offline?"),
        tr(
            "হ্যাঁ। ভয়েস টাইপিং ছাড়া (যা Android-এর সেবা ব্যবহার করে) সবকিছু সম্পূর্ণ অফলাইনে চলে — " +
                "অভিধান, সাজেশন ও শেখা শব্দ সবই আপনার ডিভাইসে।",
            "Yes. Apart from voice typing (which uses Android's service), everything runs fully " +
                "offline — the dictionary, suggestions and learned words all live on your device.",
        ),
    ),
    Faq(
        tr("আমার শেখা শব্দগুলো কীভাবে মুছব?", "How do I clear what the keyboard has learned?"),
        tr(
            "একটি একটি করে সরাতে সাজেশনে দীর্ঘক্ষণ চাপুন। সব একসাথে মুছতে Android সেটিংসে " +
                "অ্যাপের স্টোরেজ থেকে ডেটা মুছে ফেলুন — এতে সব শেখা শব্দ ও সেটিংস রিসেট হবে।",
            "Long-press a suggestion to remove entries one at a time. To clear everything, clear " +
                "the app's storage from Android settings — that resets all learned words and " +
                "settings.",
        ),
    ),
)
