# Next update roadmap

Status: planning only. Nothing in this document belongs in the current Play Store release.

## Goal

Learn which parts of Fahh people actually love, catch real-device failures early, and bring users back only when we have something genuinely worth opening. Keep the app free, local-first, and free of account creation.

## Release order

### 1. Measurement and reliability first

Use one Firebase project on the no-cost Spark plan. Do not add a database, authentication, cloud storage, BigQuery export, or server.

Add:

- Firebase Analytics for anonymous product usage.
- Firebase Crashlytics for crashes, non-fatal failures, and Android 11+ ANRs.
- Google Play Console Android Vitals as a second free health signal.

Log these events only:

- `sound_pressed`: `sound_id`, `source` (`home` or `camera`).
- `sound_unlocked`, `reward_ad_started`, `reward_ad_completed`.
- `custom_recording_started`, `custom_recording_saved`, `custom_sound_trimmed`.
- `camera_recording_saved`, `video_shared`, `gallery_opened`.
- `new_sounds_opened`, `more_cool_opened`.

Never log custom recording names, audio files, videos, text the user types, contacts, usernames, or ad identifiers in custom events.

Before release:

- Update the in-app privacy policy.
- Complete the Google Play Data safety form for the exact Firebase SDKs included.
- Keep Analytics and Crashlytics implementation small and verify events in Firebase DebugView before publishing.
- Do not enable BigQuery export. It is unnecessary for Fahh at this size and can complicate cost control.

Success criteria after two weeks:

- We can name the top sounds, unlock conversion, custom-sound adoption, camera completion rate, share rate, and the main crash/ANR issues.
- No personal custom-audio content reaches Firebase.

### 2. Optional, respectful notifications

Only add this after the measurement release is stable.

Use Firebase Cloud Messaging (FCM), sent from Firebase Console. Do not build a notification server.

Required UX:

- Add a More cool preference: `New sound drops and rare reminders`.
- Explain the value before requesting permission.
- Ask Android notification permission only after the user opts in, never on first launch.
- Let users turn the preference off in More cool.

Allowed campaign types:

- A meaningful new-sound drop.
- One gentle reminder after roughly 14 days inactive, no more than once every 30 days.

Never send:

- Daily nagging reminders.
- Ads disguised as notifications.
- Rewarded-ad prompts outside the app.
- False urgency, “viral” claims, or countdowns.

Success criteria after four weeks:

- Notification opt-in rate, open rate, and seven-day return rate improve without an unusual uninstall or notification-disable increase.

## Product calls for later

### Button skins: yes

Why: they personalize the one object users touch most, work entirely offline, and are a visible, shareable bit of identity. They match Fahh better than a generic paid upgrade.

First version:

- Ship 4 to 6 carefully designed skins, such as classic red, arcade, doorbell, and “nuclear.”
- Keep the classic red button as the default.
- Use a small preview picker in More cool.
- Make at least several skins free. If rewards are used, they must be optional and must not affect core playback.

Do not ship a blank colour picker first. Curated skins protect contrast, tactile depth, and the app’s visual personality.

### Light theme: defer

Why not yet: Fahh’s main moment is a bright physical red button, camera preview, and glowing reactions in a dark, social setting. A light theme is not a one-line inversion. It needs separate surfaces, camera controls, drawer contrast, video-review controls, and accessibility QA.

Revisit only when Analytics or direct feedback shows a clear request. If we do it, build a deliberate `Light` skin with full contrast testing, not an automatic colour inversion.

### Custom app colours: yes, but curated

Why: a few controlled palettes make the app feel owned without making the interface chaotic or reducing readability.

First version:

- Offer 3 to 5 named palettes, for example `Classic`, `Arcade Night`, `Bubblegum`, and `Electric Lime`.
- Apply them to accent surfaces, cards, and the selected-sound state. Preserve the red hero button unless a complete button skin intentionally changes it.
- Check text and icon contrast on every palette.

Avoid arbitrary hex colour picking in the first version. It creates contrast failures and makes the app look inconsistent.

### Sharing custom sounds: yes, with local-only safeguards

Why: this is the most naturally viral extension of custom sounds. Someone can make a funny five-second reaction, send it to a friend, and that friend can use it on their own Fahh button.

First version:

- `Share sound` exports the selected local recording as an `.m4a` using Android’s normal share sheet.
- `Import sound` uses Android’s system file picker. No storage permission and no Fahh cloud upload.
- Imported clips must be previewed, named, trimmed to five seconds or less, then explicitly saved by the recipient.
- Show a short rights reminder: `Only share sounds you made or have permission to use.`

Do not build public sound uploads, a global feed, or user profiles. They create copyright, moderation, storage, and account costs that do not help Fahh’s core loop yet.

## Cheap-model implementation checklist

1. Create a Firebase project on Spark. Add the Android app `com.fahh` and download `google-services.json`.
2. Add Google Services, Firebase Analytics, and Crashlytics using the current Firebase BoM from official Firebase documentation.
3. Add only the event calls listed above. Verify with Firebase DebugView on a physical device.
4. Add Crashlytics context keys only for safe values such as app screen, selected bundled sound ID, and feature state. Never attach custom recording names or file paths.
5. Update privacy policy and Play Data safety disclosures for every included Firebase SDK.
6. Release to internal testing, then closed testing, and review Crashlytics/Analytics for at least several days before production.
7. In a later version, add the user-controlled FCM opt-in flow and create no more than the two notification campaign types above.

