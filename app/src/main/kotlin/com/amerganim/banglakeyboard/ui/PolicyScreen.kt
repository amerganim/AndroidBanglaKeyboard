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
            Section(tr("সারসংক্ষেপ", "Summary")) {
                Text(
                    tr(
                        "আমাদের বাংলা কিবোর্ড আপনার কোনো তথ্য সংগ্রহ, প্রেরণ বা শেয়ার করে না। " +
                            "সবকিছু আপনার ডিভাইসেই থাকে। অ্যাপটির কোনো ইন্টারনেট অনুমতি নেই, তাই এটি " +
                            "আপনার টাইপ করা কিছুই কোথাও পাঠাতে পারে না।",
                        "Amader Bangla Keyboard does not collect, transmit, or share any of " +
                            "your data. Everything stays on your device. The app has no internet " +
                            "permission, so it cannot send your typing anywhere.",
                    ),
                )
            }
            Section(tr("আপনার ডিভাইসে যা থাকে", "What stays on your device")) {
                Text(
                    tr(
                        "আপনার ব্যক্তিগত অভিধান, আপনি কত ঘন ঘন সাজেশন বেছে নেন, আপনার টাইপিং থেকে " +
                            "শেখা পরবর্তী-শব্দ পরিসংখ্যান এবং কী-এর আকারের পছন্দ শুধু অ্যাপের নিজস্ব " +
                            "সংরক্ষণে থাকে। কোনো সাজেশন মুছতে সেটির উপর দীর্ঘক্ষণ চাপুন। অ্যাপ " +
                            "আনইনস্টল করলে সবকিছু মুছে যায়।",
                        "Your personal dictionary, how often you pick suggestions, the next-word " +
                            "statistics learned from your typing, and your key-size preference are " +
                            "stored only in the app's private storage. Long-press a suggestion to " +
                            "forget it. Uninstalling the app deletes everything.",
                    ),
                )
            }
            Section(tr("সংবেদনশীল ক্ষেত্র", "Sensitive fields")) {
                Text(
                    tr(
                        "পাসওয়ার্ড ক্ষেত্রে এবং যেসব ক্ষেত্র ব্যক্তিগতকৃত শেখা বন্ধ রাখে, অ্যাপ " +
                            "কোনো সাজেশন দেখায় না এবং কিছু শেখে না।",
                        "In password fields and fields that opt out of personalized learning, the " +
                            "app shows no suggestions and learns nothing.",
                    ),
                )
            }
            Section(tr("অনুমতি", "Permissions")) {
                Text(
                    tr(
                        "• ইনপুট মেথড বাইন্ড: যেকোনো কিবোর্ডের জন্য প্রয়োজন।\n" +
                            "• মাইক্রোফোন: শুধু আপনি ভয়েস (🎤) কী চাপলেই চাওয়া হয়। ভয়েস টাইপিং " +
                            "ব্যবহার না করলে কখনো চাওয়া হয় না।\n" +
                            "• ইন্টারনেট, পরিচিতি, অবস্থান বা সংরক্ষণের কোনো অনুমতি নেই।",
                        "• Bind input method: required for any keyboard.\n" +
                            "• Microphone: requested only when you tap the voice (🎤) key. If you " +
                            "never use voice typing, it is never requested.\n" +
                            "• No internet, contacts, location, or storage permissions.",
                    ),
                )
            }
            Section(tr("ভয়েস টাইপিং", "Voice typing")) {
                Text(
                    tr(
                        "ভয়েস টাইপিং ঐচ্ছিক। 🎤 কী ব্যবহার করলে অ্যাপ আপনার ডিভাইসের সিস্টেম " +
                            "স্পিচ-রিকগনিশন সেবা দিয়ে কথা থেকে লেখা তৈরি করে। অ্যাপ নিজে কোনো অডিও " +
                            "রেকর্ড, সংরক্ষণ বা প্রেরণ করে না, তবে সিস্টেম স্পিচ সেবা তার নিজস্ব " +
                            "গোপনীয়তা নীতি অনুযায়ী অডিও প্রক্রিয়া করতে পারে এবং ডিভাইসভেদে তা তার " +
                            "সরবরাহকারীর কাছে পাঠাতে পারে। অডিও যেন কখনো আপনার ডিভাইস না ছাড়ে চাইলে " +
                            "শুধু 🎤 কী ব্যবহার করবেন না।",
                        "Voice typing is optional. When you use the 🎤 key, the app uses your " +
                            "device's system speech-recognition service to convert speech to text. " +
                            "The app itself does not record, store, or transmit audio, but the " +
                            "system speech service may process audio under its own privacy policy " +
                            "and, depending on your device, may send it to its provider. If you " +
                            "prefer audio never to leave your device, simply don't use the 🎤 key.",
                    ),
                )
            }
            Section(tr("তথ্য শেয়ারিং", "Data sharing")) {
                Text(tr(
                    "কিছুই না। কোনো অ্যানালিটিক্স নেই, বিজ্ঞাপন নেই, তৃতীয় পক্ষের তথ্য-সংগ্রাহক SDK নেই।",
                    "None. No analytics, no ads, no third-party data-collecting SDKs.",
                ))
            }
            Section(tr("যোগাযোগ", "Contact")) {
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
