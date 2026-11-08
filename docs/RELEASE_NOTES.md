# Release Notes

Play Console's "What's new" field allows **500 characters per language**. Paste the
EN text into `en-US` and the BN text into `bn-BD`.

---

## 1.0.10 (versionCode 11)

Editing features asked for in a Play review.

**Changes**
- Clipboard history: the 📋 button in the suggestion bar lists recently copied
  text; tap to paste, pin to keep. On-device only and excluded from cloud backups;
  unpinned clips expire after 1 hour (max 20); clips copied in password fields or
  flagged sensitive by the source app are skipped. On by default, with a setting
  that erases it when turned off. Privacy policy updated to match.
- Long-press shortcuts: A select all, C copy, V paste, X cut, Z undo (small corner
  icons on those keys; the Amader keys in the same spots do the same).
- Spacebar cursor control: slide along the spacebar to move the cursor.
- Recently used emojis tab (opens first once you have used some).
- Emoji and clipboard panels now match the keys' height, so the keyboard no
  longer grows when they open.

### What's new — EN (`en-US`)
```
• Clipboard history: tap 📋 to paste anything you copied in the last hour. Pin clips to keep them. Stays on your phone; passwords are never saved.
• Long-press C to copy, V to paste, X to cut, A to select all, Z to undo.
• Slide on the spacebar to move the cursor.
• Recently used emojis tab.
```

### What's new — BN (`bn-BD`)
```
• ক্লিপবোর্ড ইতিহাস: 📋 চাপুন, গত এক ঘণ্টায় কপি করা যেকোনো লেখা পেস্ট করুন। দরকারি লেখা পিন করে রাখুন। সব ফোনেই থাকে; পাসওয়ার্ড কখনো সংরক্ষণ হয় না।
• দীর্ঘক্ষণ চাপুন: C = কপি, V = পেস্ট, X = কাট, A = সব সিলেক্ট, Z = আনডু।
• স্পেসবারে আঙুল টেনে কার্সর সরান।
• সাম্প্রতিক ইমোজির ট্যাব।
```

---

## 1.0.9 (versionCode 10)

Typing fixes and much smarter Bangla suggestions.

**Changes**
- Amader layout: Shift on the য় key now gives ঠ (next to ট). য is on long-press
  of that key and still on the z key.
- Phonetic: Avro-style `c` = চ and `ch` = ছ (`chh` still works). **Behavior
  change:** anyone who typed `ch` for চ now types `c`. Old `ch` spellings of
  dictionary words still appear as suggestions.
- Suggestions forgive common spelling slips (ি/ী, ু/ূ, শ/ষ/স, ট/ত, ড/দ, ণ/ন,
  জ/য, ড়/র, ক্ষ, য-ফলা, doubled letters, missing ঁ): `jibon` → জীবন,
  `taka` → টাকা, `sikkha` → শিক্ষা.
- About 700 curated everyday words with real weights, chat shorthand (`tk`,
  `kmn`, `vlo`, `tmi`…), and starter next-word predictions (কেমন → আছো).
- A word committed once by mistake no longer outranks the correct spelling
  forever.

### What's new — EN (`en-US`)
```
• Smarter Bangla suggestions: small spelling slips are forgiven (jibon → জীবন, taka → টাকা), common words come first, and chat shorthand like tk, kmn, vlo works.
• Next-word suggestions from day one (কেমন → আছো).
• Phonetic: c = চ, ch = ছ (like Avro).
• Amader layout: Shift on the য় key now gives ঠ.
```

### What's new — BN (`bn-BD`)
```
• আরও স্মার্ট বাংলা সাজেশন: ছোটখাটো বানান ভুল ধরে ঠিক শব্দ দেখায় (jibon → জীবন, taka → টাকা), প্রচলিত শব্দ আগে আসে, আর tk, kmn, vlo-র মতো শর্টকাটও কাজ করে।
• প্রথম দিন থেকেই পরের শব্দের সাজেশন (কেমন → আছো)।
• ফোনেটিক: c = চ, ch = ছ (অভ্রের মতো)।
• আমাদের লেআউট: য়-এর কী-তে Shift চাপলে এখন ঠ আসে।
```

---

## 1.0.8 (versionCode 9)

Two user-control features. No changes to the typing engine's output.

**Changes**
- "Clear learned words" in Setup, behind a confirmation dialog. Erases learned
  counts, saved words and next-word pairs; the bundled dictionaries are
  untouched, so ordinary suggestions keep working. Replaces the old advice of
  clearing the app's storage, which also wiped every setting.
- "Vibrate on key press" setting, alongside the existing key sound. Off by
  default and respects the system haptics setting.

**Implementation note:** the settings screen deletes the learned files and bumps
a generation counter in prefs. A keyboard that is already running still holds the
data in memory and would write it straight back on the next commit, so it drops
its copy in `onStartInputView` when it sees the counter change. The counter is
seeded from prefs at service start, so a normal launch never looks like a
pending clear.

### What's new — EN (`en-US`)
```
• New setting: clear everything the keyboard has learned from you, in one tap. Your built-in dictionary stays put.
• New setting: vibrate on key press.
```

### What's new — BN (`bn-BD`)
```
• নতুন সেটিং: কিবোর্ড আপনার কাছ থেকে যা কিছু শিখেছে, এক চাপে মুছে ফেলুন। বিল্ট-ইন অভিধান অক্ষত থাকবে।
• নতুন সেটিং: কী চাপলে কম্পন।
```

---

## 1.0.7 (versionCode 8)

Accessibility release. No changes to the typing engine.

**Changes**
- Screen-reader labels on every icon key. Shift, Backspace, the globe key, Enter
  and the emoji panel's space bar previously announced nothing at all under
  TalkBack, making the keyboard largely unusable with a screen reader. The Enter
  key announces its actual action (Search / Send / Done / Go / Next / Previous).
- The small corner hint on a key (the long-press digit) is now marked decorative,
  so TalkBack reads "q" instead of "q 1".
- The microphone button description and the "speech recognition unavailable"
  message are now translated; both were hardcoded English.
- Replaced a deprecated `Locale` constructor call.

### What's new — EN (`en-US`)
```
This update makes the keyboard usable with a screen reader:

• Shift, Backspace, Enter and the language key now announce themselves in TalkBack. Before, they were silent.
• The Enter key says what it will do — Search, Send, Done or Next.
• Bangla translations for the voice typing button and messages.
```

### What's new — BN (`bn-BD`)
```
এই আপডেটে স্ক্রিন রিডার দিয়ে কিবোর্ড ব্যবহার করা যাবে:

• শিফট, ব্যাকস্পেস, এন্টার ও ভাষা কী এখন TalkBack-এ নিজের নাম বলে। আগে এগুলো নীরব ছিল।
• এন্টার কী কী করবে তা বলে — খুঁজুন, পাঠান, সম্পন্ন বা পরবর্তী।
• ভয়েস টাইপিং বোতাম ও বার্তার বাংলা অনুবাদ।
```

---

## 1.0.6 (versionCode 7)

Onboarding, help and feedback release. No changes to the typing engine.

**Changes**
- Added a first-run walkthrough: 6 swipeable steps that enable the keyboard and
  select it via inline buttons, skippable at any point, re-openable from Setup.
- Added a Help & FAQ screen with 9 expandable answers (enable vs. select, the
  Android "may collect what you type" warning, ট vs ত, juktakkhor, clearing
  learned words, voice typing, offline use).
- Added "Rate this app" and "Send feedback" to Setup and to Help & FAQ. Feedback
  opens a pre-filled draft in the user's own email app — the keyboard still has
  no internet permission and sends nothing itself.
- All new screens available in both English and Bangla.

**Tester note:** the walkthrough appears once on first launch after updating,
because the "seen" flag does not exist in earlier builds. That is intended — it
is how testers get to see and comment on it.

### What's new — EN (`en-US`)
```
New in this version:

• A quick walkthrough that sets the keyboard up for you, step by step. Skip it any time.
• A Help & FAQ section answering the most common questions about Bangla typing.
• "Rate this app" and "Send feedback" buttons — tell us what to fix.

Everything still works fully offline, with no internet permission.
```
(322 characters.)

### What's new — BN (`bn-BD`)
```
এই সংস্করণে নতুন:

• ধাপে ধাপে কিবোর্ড সেট আপ করার সংক্ষিপ্ত পরিচিতি। যেকোনো সময় এড়িয়ে যেতে পারেন।
• বাংলা টাইপিং নিয়ে সাধারণ প্রশ্নের উত্তরসহ সাহায্য ও প্রশ্নোত্তর বিভাগ।
• "রেটিং দিন" ও "মতামত পাঠান" বোতাম — কী ঠিক করতে হবে আমাদের জানান।

সবকিছু আগের মতোই সম্পূর্ণ অফলাইনে চলে, কোনো ইন্টারনেট অনুমতি ছাড়াই।
```
(287 characters.)

---

## 1.0.5 (versionCode 6)
- Backspace deletes the current selection.
- Long-press the "।"/"." key for a punctuation popup (including English ".").
