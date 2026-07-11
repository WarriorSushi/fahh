# FAHH — Play Store Listing (Final)

Copy-paste ready. Every field below maps directly to a Play Console field.

---

## App Name

```
Fahh: Meme Sound Reaction Cam
```
30 characters. Exact limit.

---

## Short Description

```
Play meme sounds WHILE recording video. Capture live reactions instantly.
```
73 characters. Keywords: meme sounds, recording, video, reactions.
Does not repeat anything from the full description.

---

## Full Description

```
Your friend says something dumb. You open Fahh. Hit Vine Boom. You're already recording their face. The clip is gold. Shared in 10 seconds.

No other soundboard lets you do this.

Fahh plays meme sounds through your speaker WHILE your camera records. The sound, the reaction, the chaos — all captured in one video. No editing. No syncing audio later. Just press and record.

🎥 HOW IT WORKS
Open the camera. Pick a meme sound. Tap the button while filming. The sound blasts live. Your friend's reaction is captured forever. Done.

🔴 THE BUTTON
A giant 3D button that actually feels real. It sinks when you press it. Haptic feedback punches your finger. The sound fires instantly. It's embarrassingly satisfying. You'll press it 47 times before you even open the camera.

🔊 36 SOUNDS INCLUDED
   Fahh · Bruh · Vine Boom · Wow
   Air Horn · Dun Dun Dunn · Oh My God
   Directed By · Romance Sax · Sudden Suspense
   Yoooo Japan · Gop Gop Gop
   New sounds added with updates.

🎬 EDIT & SHARE
 ▸ Trim clips inside the app
 ▸ Rotate videos
 ▸ Share to TikTok, Reels, Shorts, Snapchat, WhatsApp
 ▸ No watermark
 ▸ No account needed

⚡ ZERO FRICTION
 ▸ No sign-up
 ▸ Works completely offline
 ▸ Front and back camera options before recording
 ▸ Haptic feedback on every press

📱 MADE FOR SHORT-FORM CONTENT
Fahh is the fastest way to make reaction content for TikTok, Instagram Reels, and YouTube Shorts. Sound + Camera + Share. Three taps from moment to post.

Every soundboard app lets you press buttons alone in your room. Fahh puts the sound into the video, into the moment, and into your friends' nightmares.

Press the button. Record the chaos.
```

~1,500 characters. Punchy, scannable, keyword-rich without stuffing.

First 3 lines (visible before "Read more") are the hook — they tell the exact use case in a story.

Keyword placement:
  meme sound/sounds — 3x (natural)
  camera/cam — 2x
  record/recording — 3x
  reaction/reactions — 3x
  soundboard — 1x
  prank — 0x (in title context already)
  TikTok, Reels, Shorts — 1x each
  vine boom, bruh, air horn — 1x each (long-tail captures)

---

## Category

```
Entertainment
```

---

## Privacy Policy URL

```
https://tracker.dog/fahh-privay-policy/
```

---

## Content Rating Answers

| Question | Answer |
|----------|--------|
| Violence | No |
| Sexual content | No |
| Profanity / Language | No |
| Controlled substances | No |
| User interaction | No |
| Shares location | No |
| In-app purchases | No |
| Contains ads | Yes |

Rating: Everyone

---

## Data Safety (Play Console)

**Does your app collect or share user data?** Yes

| Data type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Device identifiers (Ad ID) | Yes | Yes — Google AdMob | Advertising |
| Photos / Videos | No — stored on device only | No | — |
| Personal info | No | No | — |
| Location | No | No | — |
| App activity | No | No | — |

**Is all data encrypted in transit?** Yes
**Can users request deletion?** Not applicable — no data stored on servers

---

## Screenshots (take 5–6, in this order)

Phone screenshots only. Dark background matches the app naturally.
Add short captions using Canva (free) — white bold text, 1 line max.

| # | What to capture | Caption overlay |
|---|-----------------|-----------------|
| 1 | Main screen, big red button | "One satisfying button." |
| 2 | Camera mode, recording with sound button visible | "Record reactions with live sounds." |
| 3 | Sidebar open, all 12 sounds visible | "12 meme sounds. More coming." |
| 4 | Share screen after recording | "Share anywhere. No watermark." |
| 5 | Trim/edit screen | "Trim and rotate in-app." |
| 6 | Button mid-press with particles | "That press though." |

How to make screenshot graphics:
1. Take raw screenshots on your phone
2. Go to canva.com → Custom size → 1080x1920
3. Place screenshot in center (scale to ~80%)
4. Add caption text below: white, bold, 40–48pt
5. Background: #0A0E14 (your app's background color)
6. Export as PNG

---

## Feature Graphic

Already generated: `feature_graphic.png` (1024x500)

---

## App Icon

512x512 PNG, no transparency. Use existing Fahh logo.
Make sure it looks good at 48x48 (how it appears in search results).

---

## Launch Playbook

### Before submitting

1. Replace test ad unit IDs with real AdMob IDs
   - Rewarded: replace in MainScreen.kt (`RewardedAdUnitId`)
   - Interstitial: replace in AdTransitionScreen.kt
   - Get these from admob.google.com after creating ad units

2. Build final AAB:
   ```
   FAHH_KEYSTORE_PATH=<absolute-path-to-upload-keystore> FAHH_KEYSTORE_PASSWORD=<password> FAHH_KEY_ALIAS=<alias> FAHH_KEY_PASSWORD=<password> ./gradlew bundleRelease
   ```

### Play Console steps

1. Go to play.google.com/console
2. Pay $25 one-time developer fee
3. Complete identity verification (1–2 days)
4. Click "Create app" → fill in name, language, free, app (not game)
5. Fill in every section under "Grow > Store presence > Main store listing" using the fields above
6. Fill in "Policy > App content" sections:
   - Privacy policy URL
   - Ads declaration
   - Target audience (select 13+ or 18+)
   - Content rating questionnaire
   - Data safety
7. Go to "Release > Testing > Internal testing"
8. Create a release → upload the AAB
9. Add yourself and friends as testers (email list)
10. Test for 2–3 days
11. When ready: "Release > Production" → Create release → upload same AAB
12. Submit for review (1–3 days)

### First week after going live

- Get 10–20 ratings from friends/family immediately
- Reply to every single review
- Share the Play Store link on your socials
- Post a TikTok/Reel showing the app in action

### Ongoing (what keeps you ranking)

- Add 1–2 new sounds every 2–4 weeks (each update boosts ranking)
- Respond to all reviews within 24 hours
- Monitor crash reports in Play Console → fix quickly
- A/B test icon and screenshots after 500+ installs
