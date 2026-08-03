# Release Notes

Play Console's "What's new" field allows **500 characters per language**. Paste the
EN text into `en-US` and the BN text into `bn-BD`.

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
