package com.amerganim.banglakeyboard.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/** One walkthrough page: a hero glyph, a title, body copy and an optional action. */
private class Page(
    val hero: String,
    val title: String,
    val body: String,
    val actionLabel: String? = null,
    val action: ((Context) -> Unit)? = null,
)

/**
 * First-run walkthrough. Swipeable, skippable at any point, and re-openable later
 * from the Setup screen. [onFinish] is called for both "skip" and "done".
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = onboardingPages()
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            // Skip is always reachable — users familiar with phonetic keyboards
            // should not have to page through the whole thing.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinish) { Text(tr("এড়িয়ে যান", "Skip")) }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { index ->
                PageContent(pages[index])
            }

            PageIndicator(count = pages.size, selected = pagerState.currentPage)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    FilledTonalButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    ) { Text(tr("আগে", "Back")) }
                }
                Button(
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isLast) tr("টাইপিং শুরু করুন", "Start typing") else tr("পরবর্তী", "Next"))
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: Page) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(page.hero, fontSize = 64.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (page.actionLabel != null && page.action != null) {
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = { page.action.invoke(context) }) { Text(page.actionLabel) }
        }
    }
}

@Composable
private fun PageIndicator(count: Int, selected: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val active = index == selected
            // The current page's dot stretches into a pill so progress is obvious
            // at a glance without counting dots.
            val width by animateDpAsState(if (active) 22.dp else 8.dp, label = "dotWidth")
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}

@Composable
private fun onboardingPages(): List<Page> = listOf(
    Page(
        hero = "আ",
        title = tr("আমাদের বাংলা কিবোর্ডে স্বাগতম", "Welcome to Amader Bangla Keyboard"),
        body = tr(
            "যেভাবে শোনা যায় সেভাবেই লিখুন — \"amar\" লিখলেই হয় আমার। কোনো লেআউট মুখস্থ করতে হবে না।",
            "Type Bangla the way it sounds — write \"amar\" and get আমার. There is no layout to memorize.",
        ),
    ),
    Page(
        hero = "⚙️",
        title = tr("ধাপ ১ — কিবোর্ডটি চালু করুন", "Step 1 — Enable the keyboard"),
        body = tr(
            "সিস্টেম সেটিংসে আমাদের বাংলা কিবোর্ড চালু করুন। Android সতর্ক করবে যে কিবোর্ড আপনার লেখা " +
                "\"সংগ্রহ করতে পারে\" — এটি প্রতিটি কিবোর্ডের জন্য স্ট্যান্ডার্ড বার্তা।",
            "Turn on Amader Bangla Keyboard in system settings. Android will warn that a keyboard " +
                "\"may collect what you type\" — that is the standard message for every keyboard.",
        ),
        actionLabel = tr("সেটিংস খুলুন", "Open settings"),
        action = { context ->
            context.startActivity(
                Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
    ),
    Page(
        hero = "⌨️",
        title = tr("ধাপ ২ — এটি নির্বাচন করুন", "Step 2 — Switch to it"),
        body = tr(
            "কিবোর্ড পিকার খুলে আমাদের বাংলা কিবোর্ড বেছে নিন। যেকোনো অ্যাপে টাইপ করার সময়ও " +
                "আপনি পিকার থেকে কিবোর্ড বদলাতে পারবেন।",
            "Open the keyboard picker and choose Amader Bangla Keyboard. You can switch back " +
                "from the picker at any time while typing in any app.",
        ),
        actionLabel = tr("কিবোর্ড নির্বাচন করুন", "Choose keyboard"),
        action = { context ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        },
    ),
    Page(
        hero = "কাজ",
        title = tr("ধাপ ৩ — শব্দ ধরে লিখুন", "Step 3 — Type the sound"),
        body = tr(
            "k + a + j লিখলেই হয় কাজ, তারপর স্পেস চেপে নিশ্চিত করুন। স্কিমটি বড়/ছোট-হাতের সংবেদনশীল " +
                "(t = ত, T = ট), আর যুক্তাক্ষর নিজে থেকেই তৈরি হয়।",
            "Type k + a + j to get কাজ, then press Space to commit. The scheme is case-sensitive " +
                "(t = ত, T = ট), and conjuncts form automatically.",
        ),
    ),
    Page(
        hero = "🌐",
        title = tr("বাংলা ⇄ English", "Bangla ⇄ English"),
        body = tr(
            "গ্লোব কী চেপে যেকোনো সময় ইংরেজি ও বাংলার মধ্যে বদলান। মাইক্রোফোন কী দিয়ে দুই ভাষাতেই " +
                "বলে লেখা যায়, আর সাজেশন বার আপনার লেখা শব্দ শিখে নেয়।",
            "Tap the globe key to switch between English and Bangla at any time. The microphone key " +
                "types by voice in either language, and the suggestion bar learns the words you pick.",
        ),
    ),
    Page(
        hero = "🔒",
        title = tr("সম্পূর্ণ ব্যক্তিগত", "Completely private"),
        body = tr(
            "এই কিবোর্ডের কোনো ইন্টারনেট অনুমতি নেই। আপনি যা লেখেন, যা সাজেশন আসে এবং যা এটি শেখে — " +
                "সবই আপনার ডিভাইসে থাকে। পাসওয়ার্ড ফিল্ডে এটি কিছুই শেখে না।",
            "This keyboard has no internet permission. Everything you type, every suggestion, and " +
                "everything it learns stays on your device. In password fields it never learns.",
        ),
    ),
)
