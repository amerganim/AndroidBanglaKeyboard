# Google Play Store Listing

Draft copy for the Play Console listing. Tweak before submission.

Play indexes the **title** and **short description** most heavily, the full
description much less. Put the words people actually search for in the first
two, and keep the full description readable for humans.

## App name (≤ 30 chars)
`Amader Bangla Keyboard` — 22 chars.

Alternative if more keyword reach is wanted (30 chars exactly):
`Bangla Keyboard: Amader বাংলা`

> Renaming an established listing resets some search equity. Only change this if
> installs are still low.

## Short description (≤ 80 chars)
```
Bangla phonetic keyboard — type banglish, get বাংলা. Offline, private, no ads.
```
78 chars. Packs the four highest-value terms (bangla, phonetic, keyboard,
banglish) into the field Play weights most.

Alternates to A/B test in Play Console's store listing experiments:
- `Bangla keyboard — type "amar", get আমার. Offline Bengali phonetic typing.` (73)
- `Type Bangla by sound. Offline Bangla & Bengali phonetic keyboard, no ads.` (73)

## Full description

```
Type Bangla the way it sounds. Write "amar" and get আমার — no fixed layout to
memorize, no internet needed, no ads.

Amader Bangla Keyboard is a fast, private Bangla (Bengali) keyboard for phonetic
typing. If you already write banglish, you already know how to use it.

━━━━━━━━━━━━━━━━━━━━
✍️ TWO WAYS TO TYPE BANGLA
━━━━━━━━━━━━━━━━━━━━
★ Phonetic (banglish) typing — a clear, case-sensitive scheme (t→ত, T→ট,
  kSh→ক্ষ) with a live preview and commit-on-space. Conjuncts (juktakkhor) form
  automatically as you type.
★ Amader fixed layout — an optional Bangla layout where every key sits at its
  English-sound QWERTY position (k→ক, m→ম, a→আ), so it is easy to learn.

━━━━━━━━━━━━━━━━━━━━
⚡ FAST, SMART TYPING
━━━━━━━━━━━━━━━━━━━━
★ Bangla word suggestions from a large built-in dictionary, plus an English
  dictionary for English typing.
★ Next-word prediction that learns your writing — the more you type, the better
  it gets. Long-press a suggestion to forget it.
★ Voice typing in Bangla and English with the microphone key.
★ Bangla and English in one keyboard — switch with a single globe key, with
  auto-capitalization in English.

━━━━━━━━━━━━━━━━━━━━
😀 EVERYTHING YOU NEED
━━━━━━━━━━━━━━━━━━━━
★ Emoji keyboard and two symbol pages.
★ Every Bangla sign — chandrabindu (ঁ), hasanta (্), visarga (ঃ), khanda-ta (ৎ),
  anusvara (ং), dari (।) and Bangla digits ০-৯.
★ Ref, ra-phala, ya-phala and bo-phala all supported.

━━━━━━━━━━━━━━━━━━━━
🎨 MAKE IT YOURS
━━━━━━━━━━━━━━━━━━━━
★ Light and dark themes that follow your system.
★ Three key sizes, plus an optional key-press sound.
★ Full Bangla and English app interface — switch the whole app language.
★ Built-in typing guides for both layouts, a quick walkthrough for new users,
  and an in-app Help & FAQ section.

━━━━━━━━━━━━━━━━━━━━
🔒 TRULY PRIVATE
━━━━━━━━━━━━━━━━━━━━
This keyboard has NO internet permission. Not "we promise not to upload" — it is
technically unable to. Everything you type, every suggestion, and everything it
learns stays on your device. In password fields it never suggests or learns.

Android shows a warning that any keyboard "may collect what you type." That is
the standard message for every keyboard, including Gboard. With no internet
permission, this one cannot send anything anywhere.

━━━━━━━━━━━━━━━━━━━━
Free. No ads. No tracking. No account.

Perfect for typing Bangla on Facebook, WhatsApp, Messenger, email and SMS — for
Bangladesh and West Bengal.

Questions or ideas? Tap Help & FAQ in the app, or write to
ganimtruthfinder@gmail.com — we read every message.
```

Around 2,400 characters, well inside Play's 4,000 limit.

## Bangla (bn-BD) localized listing

Worth adding — most of the target audience searches in Bangla, and a localized
listing surfaces for Bangla-script queries that the English one never matches.

- **Short description:** `বাংলা ফোনেটিক কিবোর্ড — "amar" লিখুন, পান আমার। অফলাইন, বিজ্ঞাপনমুক্ত।`
- **Full description:** translate the block above; the app UI strings in
  `app/src/main/res/values-bn/strings.xml` are the reference for terminology.

## Category
Tools

## Tags / keywords

Play has no keyword field — these belong in the title, short description and
naturally in the full description. Listed here so the copy stays deliberate.

**Primary (must appear in title or short description):**
bangla keyboard, bengali keyboard, bangla phonetic, banglish, bangla typing

**Secondary (work into the full description):**
bangla voice typing, bangla word suggestion, juktakkhor, offline keyboard,
privacy keyboard, bangla emoji keyboard, bengali typing keyboard, avro, bijoy,
বাংলা কিবোর্ড, বাংলা টাইপিং

> "avro" and "bijoy" are competitor/product names. They pull real search traffic,
> but do not use them in the title or in any way that implies affiliation.

## Content rating
Everyone (no objectionable content; no data collection).

## Privacy policy URL
https://amerganim.github.io/amader-bangla-keyboard-privacy-policy/

## Data safety form
- Data collected: None
- Data shared: None
- Data processed on-device only; not sent off device.

## Screenshots

Play shows the first 3 in search results — those three do the conversion work.
Plain device screenshots waste that space. Each one below should be the keyboard
screenshot placed on a solid background with a short caption above it, large
enough to read as a thumbnail.

Order matters; keep it.

| # | Shows | Caption (EN) | Caption (BN) |
|---|-------|--------------|--------------|
| 1 | "amar" typed in roman with আমার in the preview, arrow between them | Type the sound. Get Bangla. | যেভাবে শোনা যায়, সেভাবেই লিখুন |
| 2 | Suggestion bar with Bangla candidates mid-word | Smart Bangla suggestions | স্মার্ট বাংলা সাজেশন |
| 3 | No-internet / lock visual over the keyboard | No internet permission. Nothing leaves your phone. | ইন্টারনেট অনুমতি নেই |
| 4 | Globe key highlighted, Bangla and English side by side | Bangla ⇄ English in one tap | এক চাপে বাংলা ⇄ English |
| 5 | Mic key active, waveform | Voice typing in Bangla | বাংলায় ভয়েস টাইপিং |
| 6 | Amader fixed layout with k→ক, m→ম annotated | Or use the Amader layout | অথবা আমাদের লেআউট |
| 7 | Emoji panel | Emoji and symbols built in | ইমোজি ও সিম্বল |
| 8 | Dark theme keyboard | Light and dark themes | লাইট ও ডার্ক থিম |

Requirements: PNG or JPEG, 16:9 or 9:16, min 320 px, max 3840 px on the long
edge. Minimum 4 screenshots; 8 fills the carousel.

Capture on a clean device — full battery, no notifications, plausible chat
content rather than lorem ipsum.

(Optional tablet screenshots for a tablet listing.)

## Graphics needed
- App icon 512×512 (export from the adaptive icon or a higher-fidelity design).
- Feature graphic 1024×500 — app name in Bangla and English plus the one-line
  hook; no device frames, since Play crops it in places.
