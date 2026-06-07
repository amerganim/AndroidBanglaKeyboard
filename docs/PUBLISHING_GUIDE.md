# How to Publish Amader Bangla Keyboard to Google Play

A step-by-step guide for putting the app on the Play Store. Allow ~1–2 hours for
first-time setup, plus Google's review time (hours to a few days).

---

## Release readiness ✅

The **code is release-ready**. Verified:
- Signed `bundleRelease` / `assembleRelease` build cleanly (R8 + resource shrinking);
  AAB ≈ 3 MB. `versionCode 1`, `versionName 1.0.0`.
- All unit tests pass (transliterator, suggester, 300+ juktakkhor, smart conjunct).
- Features complete: English + Phonetic + Amader layouts, suggestions, next-word
  prediction, personal dictionary, emoji, symbols, voice typing, dark/light theme,
  key size + sound, smart conjunct, **Bangla/English UI localization**.
- Privacy: no internet permission; no data collected/shared.
- Store assets present in [`store-assets/`](store-assets/).

**Before you can submit, only these external/account steps remain (you must do them):**
1. Host the privacy policy URL (step 1) — enable GitHub Pages on the public repo.
2. Create a Play Developer account (US$25) and the app listing (steps 2–5).
3. Back up `release.jks` securely.

The **action flow** below takes you from here to live.

---

## 0. Prerequisites

- A **Google Play Developer account** — one-time **US$25** registration at
  <https://play.google.com/console>. Use a Google account you control long-term.
- The **signed App Bundle** (`.aab`) — already produced by this project (see step 3).
- The **upload keystore** (`release.jks`) — **back this up securely.** If you lose
  it you can reset it via Play (with Play App Signing), but keep it safe anyway.
- Listing assets (see step 5).

> ⚠️ **Never commit `release.jks` or `keystore.properties` to git.** They are
> already gitignored in this project. Store a backup somewhere safe (password
> manager / encrypted drive).

---

## 1. Host the privacy policy (required)

Google requires a **publicly accessible** privacy-policy **URL** for keyboards
(the in-app copy doesn't satisfy this). A public repo already exists for this:
<https://github.com/amerganim/amader-bangla-keyboard-privacy-policy>.

To publish it with GitHub Pages:
1. Copy [`docs/privacy-policy.html`](privacy-policy.html) into that repo as
   **`index.html`** (the repo root).
2. In that repo: **Settings → Pages → Build and deployment → Source: Deploy from a
   branch → Branch: `main` / `/ (root)` → Save**.
3. After a minute the policy is live at:
   **<https://amerganim.github.io/amader-bangla-keyboard-privacy-policy/>**

Paste that URL into the Play listing (it's already used in
[`STORE_LISTING.md`](STORE_LISTING.md)). Alternatives if you prefer: Google Sites,
a public Gist, Netlify/Vercel/Cloudflare Pages.

---

## 2. Create the app in Play Console

1. Play Console → **Create app**.
2. App name: **Amader Bangla Keyboard**; default language; type **App**; **Free**.
3. Accept the developer program policies and US export laws.

---

## 3. Build the release artifact

From the project root (with `keystore.properties` present). On Windows set the JDK
first: `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`.

```bash
# For Google Play (upload this):
./gradlew :app:bundleRelease
# -> app/build/outputs/bundle/release/app-release.aab

# A signed APK, only for direct install / sideload testing:
./gradlew :app:assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

Play needs the **`.aab`**. The `.apk` is just for installing on a device yourself
(`adb install -r app-release.apk`).

> **Play App Signing (recommended):** when you first upload, opt in to **Play App
> Signing**. Google holds the *app signing key*; your `release.jks` becomes the
> *upload key*. This is the safer default.

---

## 4. Complete the required forms (App content)

In Play Console → **App content**, fill in:

- **Privacy policy:** paste the URL from step 1.
- **Data safety:** declare **No data collected** and **No data shared**. The app
  has no internet permission. Note voice typing uses the device's system speech
  service (the app itself collects nothing).
- **Permissions:** `RECORD_AUDIO` is used only for optional voice typing — explain
  this if prompted.
- **Content rating:** complete the questionnaire → expect **Everyone**.
- **Target audience:** not directed at children.
- **Ads:** No ads.
- **Government app / financial / health:** No.

---

## 5. Store listing (Main store listing)

Use the copy in [`STORE_LISTING.md`](STORE_LISTING.md). The graphics are ready in
[`docs/store-assets/`](store-assets/):

- **App name**, **short description** (≤80 chars), **full description**.
- **App icon** — [`store-assets/icon-512.png`](store-assets/icon-512.png).
- **Feature graphic** — [`store-assets/feature-1024x500.png`](store-assets/feature-1024x500.png).
- **Phone screenshots** — [`store-assets/screenshots/`](store-assets/screenshots/)
  (phonetic typing, Amader layout, emoji, the localized Bangla setup screen).
- **Category:** Tools. **Contact email** and (optional) website.

---

## 6. Release to a test track first

1. Play Console → **Testing → Internal testing → Create new release**.
2. Upload `app-release.aab`. Add **release notes**.
3. Add your own email as a tester; install via the opt-in link and verify on a
   real device (enable the keyboard, type Bangla/English, try voice + emoji).
4. Optionally promote to **Closed testing** with a few Bangla typists for feedback.

---

## 7. Promote to Production

1. **Production → Create new release** → upload the same (or a newer) `.aab`.
2. Fill release notes. Choose a **staged rollout** (e.g. 20% → 100%).
3. **Send for review.** First review can take a few days; keyboards get extra
   scrutiny (sensitive input) — the privacy policy + data-safety answers matter.

---

## 8. Future updates

For every update:
1. Bump **`versionCode`** (must increase) and **`versionName`** in
   [`app/build.gradle.kts`](../app/build.gradle.kts).
2. `./gradlew :app:bundleRelease`.
3. Upload the new `.aab` to a track and roll out.

---

## Quick checklist

- [ ] Developer account created (US$25)
- [ ] Privacy policy hosted at a public URL
- [ ] `release.jks` backed up safely
- [ ] `bundleRelease` produces a signed `.aab`
- [ ] Data safety = no data collected/shared
- [ ] Content rating completed
- [ ] Icon + feature graphic + screenshots uploaded
- [ ] Internal test passed on a real device
- [ ] Submitted for production review
