# Fahh: Feature Suggestions — March 18, 2026

Prioritized by **impact/effort ratio**. Each feature includes effort estimate and impact rating (1-5).

### Ad Metrics (as of March 18, 2026)
| Metric | Value | Change |
|--------|-------|--------|
| Estimated earnings | $1.30 | +$1.30 |
| Requests | 2.91K | +2.9K (+20,714%) |
| Impressions | 261 | +261 |
| Match rate | 94.23% | -5.77% |
| eCPM | $5.00 | +$5.00 |
| Ads ARPU | — | Requires Firebase link |

---

## 1. Quick Wins (Low Effort, High Impact)

### 1.1 Favorites with Star Icon ⭐ `DO`
**What:** Add a small star icon on each sound tile. Tap to mark as favorite. App remembers last-selected sound across sessions (currently resets to "Fahh" every launch).
**Why:** Power users always have to re-select their sound. Long-press is not discoverable — a visible star icon is much clearer.
**Effort:** Low — persist `selectedSound` in DataStore, add star icon toggle on SoundTile.
**Impact:** 5/5

### 1.2 Sound Preview in Camera Screen ⭐ `DO`
**What:** Allow locked sounds to be previewed in camera mode (currently silently blocked). Use the same 2-preview system from MainScreen.
**Why:** Users in camera mode discover sounds but can't try them — missed unlock conversions.
**Effort:** Low — copy the `lockedPreviewCounts` logic from MainScreen into CameraScreen's drawer callbacks.
**Impact:** 4/5

### 1.3 Haptic Feedback on Sound Button in Camera ⭐ `DO`
**What:** Verify haptics fire properly at the smaller 60dp size and add a subtle vibration on record start/stop too.
**Why:** Tactile feedback makes the app feel premium and responsive.
**Effort:** Low
**Impact:** 3/5

### 1.4 "New" Badge on Recently Added Sounds ⭐ `DO`
**What:** Add a `dateAdded` field to Sound model. Show a small "NEW" badge on sounds added in the last 14 days.
**Why:** Drives curiosity and exploration when you add new sound packs.
**Effort:** Low — one field + a badge composable.
**Impact:** 4/5

### ~~1.5 Share Screen: Auto-Play Video Preview~~ `SKIP`
**Reason:** Users might record in classrooms etc. Auto-playing could give them away. Keep manual play.

---

## 2. Engagement Features (Retention & Daily Usage)

### ~~2.1 Sound of the Day~~ `SKIP`

### 2.2 Usage Streaks & Stats ⭐ `DO`
**What:** Track daily usage streaks (days in a row the app was opened/sound was played). Show a small streak counter on the main screen. "You've FAHH'd 247 times!"
**Why:** Gamification drives retention. Users love seeing silly stats.
**Effort:** Medium — new DataStore keys + a small stats UI.
**Impact:** 4/5

### 2.3 Quick Sound Switcher on Main Screen ⭐ `DO`
**What:** Add a horizontal row of small sound chips below the big button (showing unlocked sounds). Tap to switch without opening the full sidebar.
**Why:** Reduces friction for the core loop: pick sound → press button → laugh.
**Effort:** Medium — a `LazyRow` of chips with selected state.
**Impact:** 4/5

### 2.4 Reaction Gallery ⭐ `DO`
**What:** A simple gallery screen showing all previously recorded videos. Users can re-share or delete old reactions.
**Why:** Users forget they have saved videos. Re-sharing = more viral spread.
**Effort:** Medium — scan output directory, show in a grid with thumbnails.
**Impact:** 3/5

### ~~2.5 Sound Combos / Multi-Tap~~ `SKIP`

---

## 3. Monetization Ideas

### ~~3.1 "Remove Ads" One-Time Purchase~~ `SKIP`
### ~~3.2 Premium Sound Packs (IAP)~~ `SKIP`

### 3.3 Custom Sound Upload — `COMING SOON` page only
**Decision:** Don't implement yet. Create a "Coming Soon" features button somewhere in the app. On tap, show a screen listing upcoming features including: "Custom Sounds — Upload your own audio clips and use them as reaction sounds." Implement the actual feature in next update.
**Effort:** Low (just the teaser page)
**Impact:** 3/5 (builds anticipation)

### 3.4 Tip Jar / "Buy Me a Coffee" ⭐ `DO`
**URL:** https://buymeacoffee.com/warriorsushi
**Design:** Classy icon (coffee cup or heart), placed at the very bottom of settings/sidebar. On tap, show a warm, humorous message explaining what the user is about to do and why it matters, then open the link.
**Effort:** Low
**Impact:** 2/5

---

## 4. Social / Viral Features

### 4.1 Watermark Branding on Videos ⭐ `DO`
**What:** Small "Made with Fahh" watermark on exported videos. **No Pro option to remove** — just a toggle in Settings that users can flip off (most won't bother lol).
**Note:** This means we need a Settings screen to house: watermark toggle, tip jar link, and other settings.
**Effort:** Medium
**Impact:** 5/5

### 4.2 Direct Share to TikTok/Instagram Reels ⭐ `DO`
**What:** Specific share buttons for TikTok, Instagram Reels, and WhatsApp Status with proper intent handling.
**Why:** Platform-specific sharing gets 2-3x more shares than generic share sheets.
**Effort:** Low-Medium
**Impact:** 4/5

### ~~4.3 Challenge a Friend Deep Link~~ `SKIP`
### ~~4.4 Reaction Duets~~ `SKIP`

---

## 5. Polish & Delight

### 5.1 Button Skin Themes ⭐ `DO` (implement last)
**What:** Different button styles (arcade, doorbell, nuclear launch, slime). Each unlockable with rewarded video. Default button is free.
**Note:** Need really premium button designs. May need design assets.
**Effort:** Medium
**Impact:** 3/5

### ~~5.2 Screen Shake on Sound Play~~ `SKIP`

### 5.3 Sound Waveform Visualization ⭐ `DO`
**What:** Animated waveform/ripple radiating from the button when sound plays (beyond current particle burst). Concentric circles with fade-out.
**Effort:** Low-Medium
**Impact:** 3/5

### 5.4 Easter Egg: Rapid-Fire Mode ⭐ `DO`
**What:** Tap button 10+ times in 3 seconds → trigger "LEGENDARY" animation (screen flash, extra confetti). **Reward: unlocks one random button skin forever for free.**
**Effort:** Low
**Impact:** 4/5

### 5.5 Unlock Celebration Enhancement ⭐ `DO`
**What:** Dramatic reveal animation when unlocking via rewarded ad: tile "shatters" its lock, glows gold, auto-previews the sound.
**Effort:** Medium
**Impact:** 3/5

---

## 6. Technical Improvements

### 6.1 Proper Navigation with Nav Component ⭐ `DO`
**What:** Replace manual `currentScreen` string-based navigation with Jetpack Navigation Compose.
**Effort:** Medium
**Impact:** 4/5

### ~~6.2 Remote Sound Catalog~~ `SKIP` (future update maybe)

### 6.3 Analytics Events (Firebase) ⭐ `DO`
**What:** Firebase Analytics events for: sound played, sound unlocked, recording started, recording shared, sound selected, ad watched, ad failed.
**Why:** Flying blind without data. Need to know which sounds are popular and where users drop off.
**Effort:** Low-Medium
**Impact:** 5/5

### 6.4 SoundManager Lazy Loading ⭐ `DO`
**What:** Lazy-load sounds instead of preloading all 12 at init. Preload free sounds, load premium on-demand when unlocked.
**Effort:** Low
**Impact:** 3/5

### 6.5 Video Export with MediaCodec ⭐ `DO`
**What:** Migrate from MediaMuxer to MediaCodec for video processing. Unlocks watermarks, filters, rotation.
**Effort:** High
**Impact:** 4/5

### 6.6 Crash Reporting (Firebase Crashlytics) ⭐ `DO`
**What:** Add Crashlytics. The `runCatching` blocks silently swallow errors.
**Setup guide:** Will need Firebase project setup — step-by-step instructions provided separately.
**Effort:** Low
**Impact:** 4/5

---

## Approved Priority Ranking

| # | Feature | Effort | Impact | Status |
|---|---------|--------|--------|--------|
| 1 | Favorites with star icon | Low | 5 | Approved |
| 2 | Analytics events (Firebase) | Low-Med | 5 | Approved |
| 3 | Watermark on videos | Medium | 5 | Approved |
| 4 | Crashlytics | Low | 4 | Approved |
| 5 | Easter egg: rapid-fire | Low | 4 | Approved |
| 6 | "New" badge on sounds | Low | 4 | Approved |
| 7 | Camera sound preview | Low | 4 | Approved |
| 8 | Direct TikTok/IG share | Low-Med | 4 | Approved |
| 9 | Usage streaks & stats | Medium | 4 | Approved |
| 10 | Quick sound switcher | Medium | 4 | Approved |
| 11 | Nav Component migration | Medium | 4 | Approved |
| 12 | Haptics on camera buttons | Low | 3 | Approved |
| 13 | Tip jar (buymeacoffee) | Low | 2 | Approved |
| 14 | Coming Soon page | Low | 3 | Approved |
| 15 | Reaction gallery | Medium | 3 | Approved |
| 16 | Sound waveform viz | Low-Med | 3 | Approved |
| 17 | Unlock celebration | Medium | 3 | Approved |
| 18 | SoundManager lazy load | Low | 3 | Approved |
| 19 | MediaCodec video export | High | 4 | Approved |
| 20 | Button skin themes | Medium | 3 | Approved (last) |
