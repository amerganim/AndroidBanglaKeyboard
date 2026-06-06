# How to Publish Amader Bangla Keyboard to Google Play

A step-by-step guide for putting the app on the Play Store. Allow ~1–2 hours for
first-time setup, plus Google's review time (hours to a few days).

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

Google requires a **publicly accessible** privacy-policy **URL** for keyboards.
Your GitHub repo is private, so don't link to it. The policy text lives in
[`PRIVACY_POLICY.md`](../PRIVACY_POLICY.md) and is also shown **inside the app**.
Host the text publicly using any of these free options:

- **GitHub Pages (public repo):** create a *separate, public* repo (e.g.
  `amaderbangla-privacy`), add `PRIVACY_POLICY.md` (or an `index.html`), enable
  **Settings → Pages**. You get a URL like
  `https://amerganim.github.io/amaderbangla-privacy/`.
- **Google Sites:** create a one-page site, paste the policy, publish — free, no code.
- **A public GitHub Gist** set to "public", or Netlify/Vercel/Cloudflare Pages.

Save the resulting URL — you'll paste it into the Play listing.

---

## 2. Create the app in Play Console

1. Play Console → **Create app**.
2. App name: **Amader Bangla Keyboard**; default language; type **App**; **Free**.
3. Accept the developer program policies and US export laws.

---

## 3. Build the release bundle

From the project root (with `keystore.properties` present):

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab` (this is what you upload).

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

Use the copy in [`STORE_LISTING.md`](STORE_LISTING.md):

- **App name**, **short description** (≤80 chars), **full description**.
- **App icon** 512×512 PNG (export a higher-res version of the in-app icon, or
  commission one).
- **Feature graphic** 1024×500 PNG.
- **Phone screenshots** (min 2; capture: Bangla typing + suggestions, predictions,
  emoji, dark theme, the typing guide).
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
