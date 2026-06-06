# Amader Bangla Keyboard (Android)

A privacy-first **Bangla phonetic** keyboard for Android. Type Bangla the way it
sounds — `amar` → আমার — and switch to a full **English** QWERTY with a single
key. All suggestions, learning and prediction happen **on-device**; nothing is
sent to any server.

It is the Android port of the
[Windows Bangla Keyboard](https://github.com/amerganim/WindowsBanglaKeyboard);
the phonetic engine and word-suggestion logic were ported from C++ to Kotlin.

---

## Features

- **Bangla phonetic typing** — case-sensitive Avro-style scheme (`t`=ত, `T`=ট,
  `kSh`=ক্ষ …) with live, underlined preview and commit-on-space.
- **English mode** — full QWERTY with word suggestions, toggled by the 🌐 globe key.
- **Word suggestions** — prefix completion from a bundled Bangla dictionary
  (~65k words) and an English dictionary (~10k words).
- **Next-word prediction** — a personal model learned from your own typing
  (bigrams), per language, stored on-device.
- **Personal dictionary** — words you type are saved and suggested later;
  **long-press a suggestion to forget it**.
- **Two symbol pages** + **emoji panel** (categorised).
- **Bangla signs** — chandrabindu `ঁ`, hasanta `্`, visarga `ঃ`, dari `।`, with
  long-press for the literal `^` / `` ` `` characters.
- **Material 3** UI, **light/dark** theme following the system, **adjustable key
  size** (Small / Medium / Large).
- **In-app typing guide** with the full keymap.
- **Privacy**: no internet permission; learning data never leaves the device.

---

## Architecture

```
InputMethodService (BanglaInputMethodService)
  └─ onCreateInputView → ComposeView  (ViewTree owners set on the window decorView)
        KeyboardScreen
          ├─ CandidateStrip      suggestions / next-word predictions
          └─ QwertyLayout        English | Bangla | symbols | EmojiPanel
  KeyboardViewModel              mode, shift, buffer, candidates, key size
        │  drives InputConnection: setComposingText / commitText / deleteSurroundingText
        ▼
  engine/   (pure Kotlin, no Android deps — JVM unit-tested)
        ├─ RuleTable             case-sensitive roman→Bangla map
        ├─ Transliterator        greedy longest-match tokenizer + stateful assembler
        ├─ Suggester             Bangla prefix suggestions + user words + learning
        ├─ EnglishSuggester      English prefix suggestions + user words + learning
        └─ Bigrams               personal next-word model
  data/
        ├─ DictionaryRepository  loads assets, persists learning, exposes suggest/predict/forget
        └─ KeyboardPrefs         key-size setting (SharedPreferences)
  assets/  dictionary.tsv · words.tsv · english_words.txt
```

### How phonetic typing works
1. **Tokenizer** — a greedy longest-match over [RuleTable](app/src/main/kotlin/com/amerganim/banglakeyboard/engine/RuleTable.kt)
   (keys up to 3 chars, so `chh`/`Dh`/`OI` win over their prefixes).
2. **Assembler** — tracks whether the previous unit was a consonant:
   vowel-after-consonant → kar sign; consonant-after-consonant → hasanta (conjunct);
   the inherent vowel `o` emits no sign. Capitals with no scheme meaning fall back
   to their lowercase Bangla form (so no English letter leaks).

The `engine` package has **no Android dependencies**, so it is fully unit-tested
on the JVM, including a **round-trip corpus test** that reverse-transliterates real
Bangla text and verifies the engine reproduces it exactly.

---

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose (Material 3) |
| Build | Gradle 8.11.1, Android Gradle Plugin 8.10 |
| JDK | 17 (source/target); build runs on JDK 21 |
| SDK | `minSdk` 26 (Android 8.0), `compileSdk`/`targetSdk` 36 |
| Persistence | SharedPreferences + plain TSV files in `filesDir` |
| Tests | JUnit 4 (JVM) |

---

## Project structure

```
app/src/main/kotlin/com/amerganim/banglakeyboard/
  SetupActivity.kt          launcher: enable/select keyboard, key size, guide, try-it field
  ime/                      InputMethodService + KeyboardViewModel + KeyboardMode
  engine/                   RuleTable, Transliterator, Suggester, EnglishSuggester, Bigrams
  data/                     DictionaryRepository, KeyboardPrefs
  ui/                       KeyboardScreen, QwertyLayout, CandidateStrip, KeyButton,
                            EmojiPanel, GuideScreen, theme/
app/src/main/assets/        dictionary.tsv, words.tsv, english_words.txt
app/src/main/res/xml/method.xml   IME declaration (no subtypes — see note below)
app/src/test/               engine unit tests + BanglaReverse round-trip harness
```

> **IME subtypes:** none are declared on purpose. English/Bangla switch internally
> via the globe key, so the keyboard appears simply as "Amader Bangla Keyboard" in
> the system keyboard picker (like Samsung/Ridmik).

---

## Building & running

Prerequisites: Android SDK (platform 36), JDK 17+ (Android Studio's bundled JBR works).

```bash
# Build the debug APK
./gradlew :app:assembleDebug
# Run the engine unit tests
./gradlew :app:testDebugUnitTest
# Install on a connected device/emulator
./gradlew :app:installDebug
```

Then: open **Amader Bangla Keyboard** → *Enable keyboard in Settings* → *Choose
keyboard* → select it. Tap 🌐 to switch English ⇄ Bangla.

> On Windows, point Gradle at the bundled JDK first:
> `set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` then use `gradlew.bat`.

---

## Typing guide (quick reference)

| Type | Result | | Type | Result |
|---|---|---|---|---|
| `a i u e o` | আ ই উ এ অ | | `k kh g gh` | ক খ গ ঘ |
| `T D N S` | ট ড ণ ষ | | `ch chh j` | চ ছ জ |
| `sh / S` | শ / ষ | | `bh / v` | ভ |
| `ng` | ং (anusvara) | | `^` | ঁ (chandrabindu) |
| `:` | ঃ (visarga) | | `` ` `` | ্ (hasanta) |
| `kSh` | ক্ষ | | `0-9` | ০-৯ |

Full guide is built into the app (Setup → **Bangla typing guide**).

---

## Releasing to Google Play

See [the production checklist](#production-readiness) below. In short:

1. Set a release `applicationId`, bump `versionCode`/`versionName`.
2. Create an upload keystore and a `release` signing config (keep the keystore out
   of git).
3. Enable R8/shrinking for the release build.
4. Add an adaptive launcher icon and a Play Store feature graphic + screenshots.
5. Publish a **privacy policy** (required for keyboards) stating that no data
   leaves the device.
6. Complete the Play Console **Data safety** form ("No data collected/shared").
7. Build an **App Bundle**: `./gradlew :app:bundleRelease`.

### Production readiness

Status legend: ✅ done · ⚠️ needs work · ❌ missing

| Area | Status | Notes |
|---|---|---|
| Core typing (Bangla + English) | ✅ | Working, unit-tested, device-tested |
| Suggestions / prediction / learning | ✅ | On-device |
| Light/dark theme, key size | ✅ | |
| Privacy (no network) | ✅ | Strong selling point; only `BIND_INPUT_METHOD` |
| Unit tests | ✅ | Engine + suggester + round-trip corpus |
| Launcher icon | ❌ | Using framework default; needs adaptive icon |
| Release signing | ❌ | No keystore/signing config yet |
| R8 / shrinking | ⚠️ | Disabled; enable + verify keep rules |
| Privacy policy | ❌ | Required by Play for IMEs |
| Play listing assets | ❌ | Icon, feature graphic, screenshots, description |
| Versioning | ⚠️ | Currently `0.1.0` / `versionCode 1` |
| Instrumented/UI tests | ⚠️ | Only JVM unit tests today |
| Multi-device QA | ⚠️ | Tested on one device (Galaxy A15, Android 16) |

---

## Roadmap

- **Bangla Fixed layout** — a proposed phonetic-mnemonic fixed layout (pending UX research).
- **Speech-to-Text** — Bangla + English voice input via a mic key.
- Themes/customisation, clipboard, number row toggle.

## License

See [LICENSE](LICENSE).
