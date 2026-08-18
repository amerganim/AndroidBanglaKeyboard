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

Production access is granted once closed testing has met Google's requirement
(12 testers opted in for 14 continuous days, for personal developer accounts).

### Before you promote

- **Fix the short description.** The live one is 82 characters against Play's
  80-character cap and is being truncated. Store presence → Main store listing.
  This is a Console edit and needs no release. Replacement copy is in
  [STORE_LISTING.md](STORE_LISTING.md).
- Confirm every **App content** declaration is still green (they carry over from
  testing, but Play adds new questionnaires over time).
- Check **Countries / regions** on the Production track — it is configured
  separately from your test tracks.

### Promoting a build that is already in closed testing

Do **not** re-upload the `.aab`. Play rejects a second upload of the same
`versionCode`, and re-building would produce a different artifact from the one
your testers actually used.

1. **Test and release → Production → Create new release**.
2. Click **Add from library** and pick the bundle already uploaded to closed
   testing (match on `versionCode`).
3. Paste the **release notes** for that version — `en-US` and `bn-BD` text lives
   in [RELEASE_NOTES.md](RELEASE_NOTES.md).
4. Under **Rollout percentage**, choose a **staged rollout** — start at 10%.
5. **Save → Review release → Start rollout to Production**.

Alternatively, from the closed testing release itself: **Promote release →
Production**, which carries the bundle across without a re-upload.

### After you submit

- **Review takes longer than it did for testing.** The first production review of
  a keyboard gets extra scrutiny, because an IME can read everything the user
  types. Your no-INTERNET-permission posture and the data-safety answers are what
  carry that review — do not change them casually.
- Rollout only begins once review passes. Watch **Android vitals** (crash rate,
  ANR rate) at each step before increasing the percentage.
- **Halt rollout** is available at any percentage if something looks wrong. A
  staged rollout means a bad surprise reaches a fraction of installs, not all.
- Increase in steps — 10% → 25% → 50% → 100% — pausing a day or two at each.
- Your closed testing track can keep running alongside production; it is a useful
  place to stage the next version.

---

## 8. Future updates

Releases are built by CI from a version tag — see
[CI_RELEASE.md](CI_RELEASE.md). For every update:

1. Land the change on `main`.
2. Bump **`versionCode`** (must increase) and **`versionName`** in
   [`app/build.gradle.kts`](../app/build.gradle.kts) as its own commit.
3. Push `main`, then tag and push: `git tag -a vX.Y.Z && git push origin vX.Y.Z`.
4. `release.yml` builds the signed `.aab` + `.apk` and publishes a GitHub Release.
5. **Upload the `.aab` from that GitHub Release**, not a local build — it is the
   artifact CI tested against the tagged commit, so it is the one that is
   traceable if something goes wrong later.
6. Add release notes from [RELEASE_NOTES.md](RELEASE_NOTES.md) and roll out.

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
- [ ] Short description under 80 characters
- [ ] Staged rollout started at 10%, Android vitals watched at each step
