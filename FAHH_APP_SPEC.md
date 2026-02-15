# Fahh - App Specification Document

## Overview
Fahh is a satisfying sound button app with integrated camera recording functionality. Users can press a giant button to play meme sounds, record videos with sounds playing out loud, and share content to social media.

**App Name:** Fahh  
**Platform:** Android (Native)  
**Target Audience:** Meme enthusiasts, content creators, social media users  
**Monetization:** Free with ads (banner ads + rewarded video for unlocking sounds)

---

## Core Concept

### The Magic Formula
1. **Dead simple:** Press button → satisfying sound plays
2. **Content creation:** Record videos with sounds playing out loud
3. **Social sharing:** Instant share to TikTok/Instagram/etc.
4. **Zero friction:** No login, no setup, works immediately

### Why This Will Go Viral
- **Low barrier to entry:** Works instantly, no learning curve
- **Content creation tool:** Every shared video is free marketing
- **Meme culture:** Perfect for reaction videos and pranks
- **Satisfying interactions:** Haptics + animations = dopamine hit

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (modern declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Minimum SDK:** Android 24 (Android 7.0) - covers 95%+ devices
- **Target SDK:** Android 34 (Android 14)

### Key Libraries & APIs

#### Audio
- **SoundPool:** Low-latency audio playback (perfect for instant button response)
  - Preload all sounds on app start
  - Max 10 concurrent streams
  - Hardware acceleration support

#### Camera & Video
- **CameraX:** Modern camera API (handles device fragmentation)
  - PREVIEW use case for viewfinder
  - VIDEO_CAPTURE use case for recording
  - Auto-handles permissions and lifecycle
- **MediaRecorder:** Native video + audio recording
  - Records video from camera
  - Captures audio from microphone
  - Mixes sound playback naturally (internal mixing)

#### UI & Animations
- **Jetpack Compose:** All UI components
- **Compose Animation:** Spring physics for button, cubic bezier for menu
- **Material3:** Design system and components
- **Accompanist:** System UI controller (status bar colors)

#### Storage & Data
- **Room Database:** Store unlocked sounds, user preferences
- **DataStore:** Lightweight key-value storage for settings
- **SharedPreferences:** First-launch flags, simple configs

#### Ads
- **Google AdMob:**
  - Banner ads (320x50 at bottom)
  - Rewarded video ads (unlock sound packs)
  - Test ads during development

#### Media & File Management
- **MediaStore:** Save videos to camera roll
- **ShareSheet:** Native Android sharing
- **Coil:** Async image loading for sound thumbnails

#### Haptics
- **HapticFeedbackConstants:** Standard Android haptics
- **VibrationEffect:** Custom vibration patterns (Android 8.0+)

### Project Structure
```
app/
├── src/main/
│   ├── java/com/fahh/
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── MainScreen.kt         # Main button screen
│   │   │   │   ├── CameraScreen.kt       # Video recording screen
│   │   │   │   └── OnboardingScreen.kt   # First-time tutorial
│   │   │   ├── components/
│   │   │   │   ├── SoundButton.kt        # Giant satisfying button
│   │   │   │   ├── SidebarMenu.kt        # Hamburger menu drawer
│   │   │   │   ├── SoundGrid.kt          # 2x2 sound selection grid
│   │   │   │   └── FloatingButton.kt     # Camera mode sound button
│   │   │   └── theme/
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   ├── viewmodel/
│   │   │   ├── SoundViewModel.kt         # Sound state management
│   │   │   └── CameraViewModel.kt        # Camera state management
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   └── Sound.kt              # Sound data class
│   │   │   ├── repository/
│   │   │   │   └── SoundRepository.kt    # Data access layer
│   │   │   └── database/
│   │   │       ├── SoundDatabase.kt      # Room database
│   │   │       └── SoundDao.kt           # Database queries
│   │   ├── audio/
│   │   │   └── SoundManager.kt           # SoundPool wrapper
│   │   ├── camera/
│   │   │   └── CameraManager.kt          # CameraX wrapper
│   │   └── utils/
│   │       ├── PermissionManager.kt      # Runtime permissions
│   │       ├── HapticManager.kt          # Vibration wrapper
│   │       └── AdManager.kt              # AdMob integration
│   ├── res/
│   │   ├── raw/                          # Sound files (.mp3)
│   │   ├── drawable/                     # Icons, images
│   │   └── values/
│   │       ├── strings.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

---

## Feature Specifications

### 1. Main Screen

#### Layout
- **Dark theme by default** (better for meme apps, less eye strain)
- **Giant sound button:** Takes up 60% of vertical screen space
- **Camera button:** Below sound button, clearly labeled
- **Banner ad:** Small 320x50 at very bottom (non-intrusive)
- **Hamburger icon:** Top right corner (≡ symbol)

#### The Sound Button
- **Visual Design:**
  - Circular or rounded square (TBD based on design)
  - Sound name displayed on button ("Fahh")
  - Sound icon/emoji above name
  - Subtle gradient or shadow for depth
  - Current sound indicator

- **Interactions:**
  - **Tap:** Play sound + haptic feedback
  - **Animation:** Scale 1.0 → 0.92 → 1.05 → 1.0 (200ms, spring physics)
  - **Ripple effect:** Radiating circles from touch point
  - **Haptic:** 10-20ms vibration at peak compression

- **Audio Playback:**
  - Instant response (<50ms latency)
  - Plays through STREAM_MUSIC
  - Can interrupt previous playback if pressed rapidly
  - Respects volume slider setting

#### Camera Button
- **Style:** Rounded rectangle, subtle icon (📹)
- **Label:** "Camera" or "Record"
- **Animation:** Rotate + scale when pressed
- **Transition:** Cross-fade to camera view (200ms)

#### Banner Ad
- **Position:** Fixed at bottom, doesn't push content
- **Size:** 320x50 standard banner
- **Behavior:** Loads after 2 seconds (doesn't slow app launch)
- **Message above ad:** Friendly text: "Ads keep Fahh free! Thanks for your support 💛"

### 2. Hamburger Menu (Sidebar)

#### Layout & Behavior
- **Slides from right edge** (300ms cubic bezier curve with slight overshoot)
- **Width:** 80% of screen width
- **Background:** Dark with slight blur/dimming of main screen
- **Close:** Tap outside, swipe right, or tap X button

#### Header
- **Title:** "Sounds" (top left)
- **Close button:** X icon (top right)
- **Subtle divider** below header

#### Sound Grid
- **Layout:** 2 columns, vertical scroll
- **Each sound tile:**
  - Sound icon/emoji
  - Sound name (e.g., "Fahh", "Bruh", "Vine Boom")
  - Lock icon (🔒) if locked
  - Selected indicator (checkmark + glow)
  
- **Interactions:**
  - **Tap unlocked sound:** Preview plays (doesn't select yet)
  - **Hold unlocked sound:** Selects it (haptic + checkmark animation)
  - **Tap locked sound:** "Watch 5s ad to unlock?" prompt
  
- **Visual States:**
  - **Selected:** 1.08x scale, glow effect, checkmark
  - **Locked:** 80% opacity, grayscale filter, lock icon
  - **Unlocked:** Full color, 100% opacity

#### Volume Slider (Bottom)
- **Position:** Fixed at bottom of sidebar
- **Style:** Material3 slider
- **Range:** 0-100% (0 = muted, 100 = max)
- **Icons:** 🔈 (left) and 🔊 (right)
- **Behavior:**
  - Adjusts all sound playback volume
  - Persists setting (saved in DataStore)
  - Shows current volume percentage on drag

#### Sound Organization
- **Initial sounds:** 4 unlocked (including "Fahh")
- **Locked sounds:** 8-12 sounds locked
- **Unlock mechanism:** Watch 5-second rewarded video ad
- **Unlock in packs:** 3-5 sounds per ad watch (feels generous)

### 3. Camera Mode

#### Screen Layout
```
┌─────────────────────┐
│ [←]            [⤾]  │ ← Back button, Flip camera
│                     │
│   CAMERA PREVIEW    │ ← Live viewfinder (full screen)
│                     │
│      ⏺ 00:05        │ ← Recording indicator + timer
│                     │
│    ┌─────────┐      │
│    │   🔊    │      │ ← Floating sound button
│    │  Fahh   │      │   (larger than normal, always visible)
│    └─────────┘      │
│                     │
│       [⏺️]          │ ← Record button (red when recording)
└─────────────────────┘
```

#### Camera Controls
- **Back button (←):** Returns to main screen
- **Flip camera (⤾):** Switches front/back camera
- **Record button (⏺️):**
  - Tap to start recording
  - Tap again to stop
  - Changes to red square when recording
  - Haptic feedback on start/stop

#### Floating Sound Button
- **Position:** Center of screen, above record button
- **Size:** Slightly larger than main screen button
- **Always visible:** Floats over camera preview
- **Behavior:**
  - Press to play sound out loud during recording
  - Sound plays through speaker (mic captures it)
  - Visual ripple effect when pressed
  - Haptic feedback
  - Can press multiple times during one recording
  
#### Live Sound Switching
- **Hamburger menu accessible during recording**
- **User can swipe to open sidebar**
- **Select new sound → button updates immediately**
- **Seamless transition (no recording interruption)**

#### Recording Behavior
- **Video quality:** 1080p @ 30fps (balance of quality/file size)
- **Audio:** 44.1kHz stereo from microphone
- **Max duration:** 60 seconds (prevents huge files)
- **Format:** MP4 (H.264 video + AAC audio)
- **Storage:** Saved to DCIM/Fahh/ folder

#### Recording Indicators
- **Pulsing red dot:** Top center, opacity 0.6 ↔ 1.0 (1s cycle)
- **Timer:** Next to red dot, counts up (MM:SS)
- **Button flash:** Visual ripple when sound button pressed

### 4. Post-Recording Share Screen

#### Layout
```
┌─────────────────────┐
│   Video Saved! ✨    │ ← Success message
├─────────────────────┤
│  [Video Thumbnail]  │ ← Preview of recorded video
│     (tap to play)   │
├─────────────────────┤
│  Share to:          │
│  [TikTok] [Insta]   │ ← Share buttons (icons)
│  [Twitter] [More]   │
├─────────────────────┤
│  [Save to Gallery]  │ ← Explicit save button
│  [Done]             │ ← Return to main screen
└─────────────────────┘
```

#### Behavior
- **Auto-saves:** Video saved to camera roll immediately
- **Share sheet:** Native Android share with common apps
- **Video preview:** Tap thumbnail to play video
- **Animation:** Sheet slides up from bottom (400ms ease-out)
- **Icons stagger:** Each icon appears 50ms apart (feels premium)

### 5. First-Time Onboarding

#### Flow (3 Quick Steps)
1. **Screen 1:** "Press the button 👆"
   - Giant button pulsates
   - User must press to continue
   - Sound plays, haptic fires
   - Auto-advances

2. **Screen 2:** "Try the camera 📹"
   - Arrow points to camera button
   - User taps camera button
   - Camera opens briefly
   - Auto-advances

3. **Screen 3:** "That's it! Now go make magic ✨"
   - Shows sidebar preview
   - "Unlock more sounds by watching short ads"
   - Big "Let's Go!" button

#### Properties
- **Shown once:** First launch only
- **Skippable:** "Skip" button on each screen
- **Quick:** <30 seconds total
- **Friendly tone:** Casual, warm, excited

### 6. Locked Sounds & Monetization

#### Ad Implementation
- **Banner Ad:**
  - 320x50 at screen bottom
  - AdMob standard banner
  - Refreshes every 60 seconds
  - Loads after 2-second delay (app startup priority)
  
- **Rewarded Video Ad:**
  - Triggered when tapping locked sound
  - Modal dialog: "Watch a 5s ad to unlock [Sound Pack Name]?"
  - Buttons: "Watch Ad" (primary) or "Cancel"
  - On completion: Unlock 3-5 sounds, celebratory animation
  - Haptic + confetti effect on unlock

#### User Communication
- **Banner ad message:** "Ads keep Fahh free! Thanks for your support 💛"
  - Positioned right above ad
  - Tiny text (10-11sp)
  - Warm, grateful tone
  
- **Locked sound message:** "Unlock [N] sounds"
  - Shows on locked tile
  - Makes value clear (not just "watch ad")

#### Sound Pack Structure
```
Free Sounds (4):
- Fahh (default)
- Bruh
- Vine Boom
- Emotional Damage

Pack 1 - "Classic Memes" (3 sounds):
- Oof
- Yeet
- Stonks

Pack 2 - "Reaction Sounds" (4 sounds):
- Spongebob Fail
- Law & Order DUN DUN
- Record Scratch
- Crickets

Pack 3 - "Chaos Pack" (3 sounds):
- Air Horn
- Windows XP Startup
- Nokia Ringtone

... (more packs as needed)
```

---

## Performance Requirements

### Target Metrics
- **Button tap → sound playback:** <50ms
- **Button press animation:** 200ms (60fps)
- **Menu slide animation:** 300ms (60fps)
- **Camera mode transition:** <500ms
- **Recording start latency:** <300ms
- **App cold start:** <2 seconds

### Optimization Strategies

#### Audio Performance
- **Preload all sounds:** On app launch, load all MP3s into SoundPool
- **Sound file size:** Max 100KB per sound (compress to 64-96kbps MP3)
- **Total audio footprint:** ~1-2MB for 12-15 sounds
- **Stream priority:** PRIORITY_HIGH for button sounds

#### Animation Performance
- **Hardware acceleration:** Enabled by default in Compose
- **Avoid overdraw:** Single background layer, minimal overlapping views
- **Spring physics:** Native Compose animations (GPU accelerated)
- **Reduce allocations:** Reuse animation objects where possible

#### Camera Performance
- **Keep camera warm:** Initialize CameraX when entering camera mode
- **Preview resolution:** 720p for viewfinder (saves battery)
- **Recording resolution:** 1080p for final video
- **Frame rate:** 30fps (60fps unnecessary, drains battery)

#### Memory Management
- **Bitmap pooling:** Reuse bitmaps for sound thumbnails
- **Lazy loading:** Load sound thumbnails only when sidebar opens
- **Video cleanup:** Delete temp files after sharing
- **Leak prevention:** Proper lifecycle management for camera/audio

---

## UI/UX Design Specifications

### Color Palette (Dark Theme)
```kotlin
Primary:        #FF6B35  // Vibrant orange (button, accents)
Secondary:      #004E89  // Deep blue (secondary actions)
Background:     #1A1A1D  // Near black
Surface:        #2E2E32  // Slightly lighter (cards, sidebar)
OnPrimary:      #FFFFFF  // White text on orange
OnBackground:   #E0E0E0  // Light gray text
Error:          #CF6679  // Material red (errors)
Success:        #4CAF50  // Green (unlock celebration)
```

### Typography
```kotlin
Display:  Poppins Bold (button text, headers)
Body:     Inter Regular (descriptions, labels)
Label:    Inter Medium (small labels, hints)

Size Scale:
- Huge (Button): 48sp
- Title: 24sp
- Body: 16sp
- Caption: 12sp
```

### Spacing System
```kotlin
XXS: 4dp   // Tight spacing
XS:  8dp   // Small gaps
S:   12dp  // Default padding
M:   16dp  // Standard margin
L:   24dp  // Large spacing
XL:  32dp  // Section breaks
XXL: 48dp  // Major divisions
```

### Animation Specifications

#### Button Press Animation
```kotlin
Scale Animation:
- Start: 1.0
- Compression: 0.92 (at 60ms)
- Overshoot: 1.05 (at 120ms)
- Rest: 1.0 (at 200ms)
- Easing: Spring(dampingRatio = 0.6f, stiffness = 300f)

Ripple Effect:
- Start radius: 0dp
- End radius: 200dp
- Duration: 500ms
- Alpha: 1.0 → 0.0
- Color: Primary color at 30% opacity
```

#### Menu Slide Animation
```kotlin
Slide Animation:
- Distance: Screen width × 0.8
- Duration: 300ms
- Overshoot: 5% beyond target
- Settle: 50ms
- Easing: CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

Background Scrim:
- Alpha: 0.0 → 0.5
- Duration: 300ms
- Color: Black
```

#### Sound Selection Animation
```kotlin
Selected State:
- Scale: 1.0 → 1.08
- Duration: 150ms
- Glow: Drop shadow with 8dp blur, primary color
- Checkmark: Path drawing animation (100ms)

Deselection:
- Scale: 1.08 → 1.0
- Duration: 100ms
```

#### Recording Indicator
```kotlin
Pulsing Red Dot:
- Opacity: 0.6 ↔ 1.0
- Cycle: 1000ms (infinite repeat)
- Easing: Linear

Timer:
- Update: Every 100ms
- Format: MM:SS
- Font: Monospace for alignment
```

### Haptic Patterns
```kotlin
Button Press:
- Type: HapticFeedbackConstants.CONTEXT_CLICK
- Duration: 10ms
- Intensity: Medium

Menu Open:
- Type: HapticFeedbackConstants.VIRTUAL_KEY
- Duration: 5ms
- Intensity: Light

Sound Selection:
- Type: HapticFeedbackConstants.CONFIRM
- Duration: 15ms
- Intensity: Medium

Recording Start/Stop:
- Type: VibrationEffect.createOneShot(30, 200)
- Duration: 30ms
- Intensity: Strong
```

---

## Permissions & Privacy

### Required Permissions
```xml
<!-- Audio recording -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Camera access -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Save videos to gallery -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />

<!-- Vibration for haptics -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Internet for ads -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Permission Flow
1. **On first camera use:** Request CAMERA + RECORD_AUDIO together
2. **Modal dialog:** "Fahh needs camera and microphone to record videos with sounds"
3. **Rationale:** Brief, friendly explanation if user denies
4. **Graceful degradation:** If denied, explain feature won't work but main button still usable

### Privacy Policy
- **No data collection:** App doesn't collect personal data
- **Local storage only:** Sounds, settings stored on device
- **AdMob privacy:** Link to Google's AdMob privacy policy
- **No analytics:** No Firebase, no tracking (keeps it simple)

---

## Testing Strategy

### Unit Tests
- SoundManager: Verify sound loading, playback, volume control
- SoundRepository: Database operations, sound unlocking logic
- ViewModel: State management, user actions

### UI Tests (Compose)
- Button press triggers sound playback
- Menu slide animation works smoothly
- Sound selection updates button correctly
- Camera mode transitions properly

### Integration Tests
- End-to-end recording flow
- Ad unlock flow (use test ad units)
- Share functionality
- Permission handling

### Device Testing
- **Low-end:** Samsung Galaxy A10 (Android 9, 2GB RAM)
- **Mid-range:** Google Pixel 5a (Android 13, 6GB RAM)
- **High-end:** Samsung Galaxy S23 (Android 14, 8GB RAM)
- **Various screen sizes:** 5", 6", 6.7" displays

### Performance Testing
- Audio latency measurement (<50ms target)
- Animation frame rate (60fps target)
- Memory usage (< 150MB target)
- Battery drain during recording

---

## Release Plan

### Phase 1: MVP (Week 1-2)
- [ ] Main screen with one sound button
- [ ] Basic haptic feedback
- [ ] Simple hamburger menu (no animations yet)
- [ ] 4 unlocked sounds
- [ ] Volume slider
- [ ] No ads, no camera yet

### Phase 2: Polish (Week 3)
- [ ] Beautiful animations (button, menu)
- [ ] Refined haptics
- [ ] Lock/unlock UI (no real ads yet)
- [ ] Dark theme refinement
- [ ] Onboarding flow

### Phase 3: Camera (Week 4)
- [ ] CameraX integration
- [ ] Video recording with audio
- [ ] Floating sound button
- [ ] Save to camera roll
- [ ] Basic share functionality

### Phase 4: Monetization (Week 5)
- [ ] AdMob integration (test ads)
- [ ] Banner ad at bottom
- [ ] Rewarded video for unlocking
- [ ] Real ad units (production)

### Phase 5: Launch Prep (Week 6)
- [ ] Add 8-12 more sounds
- [ ] Beta testing (20-30 users)
- [ ] Bug fixes
- [ ] Privacy policy page
- [ ] Google Play Store assets (screenshots, description)

### Phase 6: Launch (Week 7)
- [ ] Soft launch (select countries)
- [ ] Monitor crashes/bugs
- [ ] Collect user feedback
- [ ] Iterate based on feedback
- [ ] Full global launch

---

## Future Enhancements (Post-Launch)

### Version 1.1
- Waveform animation overlay during recording
- Flash toggle for camera
- Grid lines for framing
- Front/back camera persist preference

### Version 1.2
- Widget support (favorite sound on home screen)
- Multiple sound buttons in one recording
- Timer countdown before recording
- Video length presets (15s/30s/60s)

### Version 2.0
- User-submitted sounds (moderated)
- Sound categories/folders
- Community voting on new sounds
- Trending sounds section

---

## Success Metrics

### Launch Goals (First Month)
- **Downloads:** 10,000+
- **Daily Active Users (DAU):** 2,000+
- **Retention (Day 1):** 40%+
- **Retention (Day 7):** 20%+
- **Videos created:** 5,000+
- **Ad revenue:** $200+

### Viral Indicators
- **Social shares:** 1,000+ videos shared to TikTok/Instagram
- **Organic traffic:** 60%+ downloads from word-of-mouth/social
- **Review score:** 4.5+ stars on Google Play
- **Engagement:** Average 3+ videos created per active user

---

## Technical Debt to Avoid

### Don't Do This
❌ Skip proper error handling (crashes kill retention)  
❌ Hardcode values (makes updates painful)  
❌ Ignore memory leaks (drains battery, causes crashes)  
❌ Use placeholder assets (looks unprofessional)  
❌ Skip accessibility (excludes users, violates Play Store policies)  
❌ Ignore different screen sizes (breaks on tablets/foldables)  
❌ Copy-paste code (tech debt accumulates fast)

### Do This Instead
✅ Centralize strings in strings.xml (easy translation later)  
✅ Use dependency injection (Hilt/Koin for testability)  
✅ Implement proper logging (helps debug user issues)  
✅ Use version control tags (easy rollback if needed)  
✅ Document complex logic (future you will thank you)  
✅ Write meaningful commit messages (track changes easily)  
✅ Test on real devices (emulators miss real-world issues)

---

## Development Timeline Estimate

### Conservative Estimate (Solo Developer)
- **Setup & Architecture:** 2 days
- **Main Screen + Button:** 3 days
- **Sidebar Menu + Sounds:** 4 days
- **Camera Integration:** 5 days
- **Recording + Sound Playback:** 4 days
- **Share Functionality:** 2 days
- **AdMob Integration:** 2 days
- **Onboarding:** 1 day
- **Polish & Animations:** 4 days
- **Testing & Bug Fixes:** 5 days
- **Play Store Prep:** 2 days

**Total:** ~34 days (~7 weeks)

### Aggressive Estimate (Experienced Developer)
- **Total:** ~20 days (~4 weeks)

---

## Key Design Decisions & Rationale

### Why Native Android (Not Flutter/React Native)?
✅ **Audio latency:** Native SoundPool = <50ms. Cross-platform = 100-300ms.  
✅ **Haptics:** Direct access to Android vibration APIs, perfect precision.  
✅ **Performance:** No JavaScript bridge overhead, buttery 60fps.  
✅ **App size:** ~5-8MB native vs 15-25MB cross-platform.  
✅ **Camera quality:** CameraX handles device fragmentation better.  

### Why Internal Audio Mixing (Not External)?
✅ **Authenticity:** Sounds feel real, like someone actually heard it.  
✅ **Simplicity:** No FFmpeg, no complex audio mixing, fewer bugs.  
✅ **Reactions:** Captures ambient reactions, which is content gold.  
✅ **File size:** Simpler encoding = smaller video files.  

### Why Dark Theme Default?
✅ **Meme culture:** Most meme apps are dark (TikTok, Discord, etc.)  
✅ **OLED battery:** Saves battery on modern phones.  
✅ **Eye comfort:** Less strain during extended use.  
✅ **Content focus:** Dark UI doesn't compete with colorful buttons/sounds.  

### Why No User Accounts?
✅ **Friction:** Every login screen loses 30% of users.  
✅ **Privacy:** No data to manage, no GDPR headaches.  
✅ **Simplicity:** App works instantly, no server costs.  
✅ **Focus:** MVP should prove concept before adding complexity.  

---

## Contact & Resources

### Useful Links
- **Android Developers:** https://developer.android.com/
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **CameraX:** https://developer.android.com/training/camerax
- **AdMob:** https://admob.google.com/
- **Material Design 3:** https://m3.material.io/

### Learning Resources
- **Compose Tutorials:** https://developer.android.com/courses/pathways/compose
- **Audio Playback:** https://developer.android.com/guide/topics/media/mediaplayer
- **Camera Best Practices:** https://developer.android.com/training/camera

---

## Final Notes

This spec represents the **complete vision** for Fahh 1.0. The app is intentionally simple and focused:

1. **Giant satisfying button** ✨
2. **Record videos with sounds** 📹
3. **Share to social media** 🚀

Every feature serves these three pillars. Resist feature creep. Ship fast, iterate based on real user feedback.

**Remember:** A polished, focused app that does ONE thing amazingly well will always beat a cluttered app that does many things poorly.

Now go build something amazing! 🎉
