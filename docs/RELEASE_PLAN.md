# Production / Play Store Release Plan

**Status: production access granted (August 2026).** Closed testing is complete and
the app is cleared to publish to the production track. The current production
candidate is **1.0.8 (versionCode 9)**.

This file tracked the road from a working build to a publishable app. Phases 0–2
are done; what remains is the launch itself (Phase 5) and the quality work in
Phase 3 that was always "polish that can ship in a 1.0.x update".

---

## Phase 0 — Identity & branding ✅
- [x] **applicationId** `com.amerganim.banglakeyboard` — locked in by publishing.
- [x] **Adaptive launcher icon** with foreground, background and a `<monochrome>`
      layer for themed icons.
- [x] App accent color finalized (indigo `#5B5BD6`).
- [x] App display name confirmed ("Amader Bangla Keyboard").

## Phase 1 — Build hardening ✅
- [x] **Upload keystore** created and kept out of git (`release.jks`,
      `keystore.properties`, both gitignored). Mirrored into CI secrets.
- [x] `release` **signing config** reads from `keystore.properties` locally and
      from environment variables in CI.
- [x] **R8 + resource shrinking** enabled; `lintVitalRelease` passes each release.
- [x] `versionCode`/`versionName` policy in use — one bump commit per release.
- [x] `./gradlew :app:bundleRelease` produces a signed `.aab` (~3.2 MB).
- [x] Tagged releases build the signed `.aab` + `.apk` via `release.yml`.

## Phase 2 — Legal & policy ✅
- [x] **Privacy policy** hosted and linked from the Setup screen.
- [x] **Data safety form**: no data collected, no data shared, on-device only.
- [x] Only `RECORD_AUDIO` (optional, for voice typing) — **no INTERNET permission**.
- [x] Google Play IME policy review passed.
- [x] In-app disclosure that keyboards can read what you type (`security_note`),
      plus a Help & FAQ entry explaining Android's standard warning.

## Phase 3 — Quality & polish
- [x] **Don't learn from sensitive fields** — password variations hide suggestions
      entirely; `NO_SUGGESTIONS` / `IME_FLAG_NO_PERSONALIZED_LEARNING` fields still
      suggest but are never learned from.
- [x] Accessibility: TalkBack labels on every key (1.0.7).
- [ ] Accessibility: larger-text support and a contrast audit — still outstanding.
- [ ] **Device/OS matrix**: at least Android 8, 11, 14, 16; small + large screens;
      gesture + 3-button nav. Only tested on a Galaxy A15 (Android 16) so far.
- [ ] Edge cases: URL/email fields, multiline, RTL apps, very long text, rotation.
- [ ] Performance: first-keystroke latency while the 65k word list loads.
- [ ] **Instrumented/UI tests** for the IME (compose-in-IME smoke test). None exist;
      `app/src/androidTest` is empty. The unit suite covers the engine only.
- [x] Crash logging: **deliberately skipped** — it cannot be done without a network
      dependency, which would break the app's core "no internet permission" promise.

## Phase 4 — Store listing assets
- [x] App icon 512×512 and feature graphic 1024×500 in [`store-assets/`](store-assets).
- [x] Five phone screenshots captured (phonetic, Amader, emoji, setup, guide).
- [ ] **Upgrade the screenshots** — these are raw device captures. Play shows the
      first three in search results, so they carry the conversion. Annotated spec
      with bilingual captions is in [STORE_LISTING.md](STORE_LISTING.md).
- [ ] **Fix the short description** — the live one is 82 characters against Play's
      80-character cap and is being truncated. Replacement in STORE_LISTING.md.
- [ ] **Add the bn-BD localized listing** — the audience searches in Bangla script;
      an English-only listing never matches those queries.
- [x] Category (Tools), content rating questionnaire, contact email.

## Phase 5 — Rollout
- [x] Internal testing track.
- [x] Closed testing with real Bangla typists — **completed; production access granted.**
- [ ] **Fix the short description before promoting** (see Phase 4 — it is a Console
      edit, needs no release, and matters far more in production than it did in
      testing).
- [ ] Promote 1.0.8 to production as a **staged rollout**: 10% → 25% → 50% → 100%,
      pausing a day or two at each step and watching Android vitals.
- [ ] Post-launch: monitor reviews for missing phonetic mappings; extend the
      [round-trip corpus](../app/src/test/kotlin/com/amerganim/banglakeyboard/engine/RoundTripTest.kt)
      with reported failures.
- [ ] Post-launch: bump the deprecated GitHub Actions (`setup-java@v4` → `v5` and
      the Node 20 set). Deliberately deferred until after launch — nothing is
      failing, and the release pipeline should not change right before it is needed.

---

## Before promoting to production

1. Fix the short description (82 → under 80 chars).
2. Verify on a device: clear learned words, then retype a previously-learned word
   and confirm it stays gone. This is the one 1.0.8 path never exercised on hardware.
3. Promote the **1.0.8** bundle (versionCode 9) at 10%.
4. Add the bn-BD listing and upgraded screenshots — these can land after launch,
   but they are what drives discovery once you are public.
