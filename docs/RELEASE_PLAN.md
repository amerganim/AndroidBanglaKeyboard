# Production / Play Store Release Plan

Goal: take the keyboard from a working build to a polished, policy-compliant app
on Google Play. The app is **functionally complete** for v1; the work below is
packaging, polish, legal, and QA — not core features.

Estimated effort: ~2–4 focused days (excluding Play review time).

---

## Phase 0 — Identity & branding
- [ ] Finalize **applicationId** (e.g. `com.amerganim.banglakeyboard`) — cannot change after publish.
- [ ] **Adaptive launcher icon** (foreground + background, monochrome for themed icons).
- [ ] App accent color / brand finalized (currently indigo `#5B5BD6`).
- [ ] App display name confirmed ("Amader Bangla Keyboard").

## Phase 1 — Build hardening
- [ ] **Upload keystore** created; store securely (NOT in git). Add to CI secrets if used.
- [ ] `release` **signing config** wired in `app/build.gradle.kts` (read from `keystore.properties`, gitignored).
- [ ] Enable **R8/shrinking + resource shrinking** for release; verify keep rules
      (Compose, IME service referenced from manifest). Smoke-test the shrunk build.
- [ ] Set `versionCode`/`versionName` policy (e.g. start `1.0.0` / `versionCode 1`).
- [ ] Verify the assets (1.6 MB `words.tsv`, 75 KB English) compress in the bundle;
      consider per-language download later if size matters.
- [ ] `./gradlew :app:bundleRelease` produces a signed `.aab`.

## Phase 2 — Legal & policy (required for keyboards)
- [ ] **Privacy policy** page (host a URL). Key message: *all input, suggestions and
      learning stay on the device; nothing is transmitted; no analytics.* Link it in
      the app (Setup screen) and the Play listing.
- [ ] **Data safety form**: "No data collected / No data shared." Note on-device storage.
- [ ] Confirm only `BIND_INPUT_METHOD` permission is requested (no INTERNET). If
      Speech-to-Text is added later, disclose `RECORD_AUDIO` + its data handling.
- [ ] Review Google Play **IME / sensitive-input** policy; keyboards get extra scrutiny.
- [ ] Add an in-app note that the keyboard can read what you type (standard IME disclosure).

## Phase 3 — Quality & polish
- [ ] **Device/OS matrix**: at least Android 8, 11, 14, 16; small + large screens; gesture + 3-button nav.
- [ ] Edge cases: password fields (no learning/suggestions in `textPassword`/`textNoSuggestions`),
      URL/email fields, multiline, RTL apps, very long text, rotation.
- [ ] **Don't learn from sensitive fields** — check `EditorInfo.inputType` and skip
      saving words / predictions for password & no-personalized-learning flags.
- [ ] Accessibility: TalkBack labels on keys, larger-text support, sufficient contrast.
- [ ] Performance: first-keystroke latency while the 65k word list loads (already
      async; verify no jank), memory.
- [ ] Crash-free: add lightweight, **privacy-safe** crash logging *only if* it can be
      done without a network dependency, or skip it.
- [ ] Add a few **instrumented/UI tests** for the IME (compose-in-IME smoke test).

## Phase 4 — Store listing assets
- [ ] App icon (512×512), **feature graphic** (1024×500).
- [ ] **Screenshots** (phone + tablet): Bangla typing, suggestions, emoji, dark mode, guide.
- [ ] Short + full description (highlight: phonetic, offline/private, predictions, free).
- [ ] Category: Tools; content rating questionnaire; contact email.

## Phase 5 — Rollout
- [ ] Internal testing track → fix issues.
- [ ] Closed/beta testing with a few Bangla typists (real-world phonetic coverage).
- [ ] Staged production rollout (e.g. 10% → 100%).
- [ ] Post-launch: monitor reviews for missing phonetic mappings; extend the
      [round-trip corpus](../app/src/test/kotlin/com/amerganim/banglakeyboard/engine/RoundTripTest.kt)
      with reported failures.

---

## Top blockers before first submission
1. Launcher icon (adaptive).
2. Release signing config + signed `.aab`.
3. Privacy policy URL + Data safety form.
4. "Don't learn from password/sensitive fields" guard.
5. Store screenshots + descriptions.

Everything else is polish that can ship in `1.0.x` updates.
