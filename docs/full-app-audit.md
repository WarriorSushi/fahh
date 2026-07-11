# Fahh full app audit

**Audit date:** July 11, 2026  
**Branch audited:** `codex/fahh-wp0-baseline` at `f92d2a4`  
**Scope:** Navigation, sound and custom-audio flows, camera/video lifecycle, ads and consent, persistence, accessibility, responsive UI, release configuration, store claims, and policy readiness.

## Executive summary

Fahh's core identity is strong: the main button is immediate, physical, and memorable; the app has no accounts or cloud dependency; earned bundled-sound unlocks have a real Room migration; custom recordings stay in private app storage; and the normal unit-test task passes. The current source is not ready for the next Play release, however. The largest technical risks are the forced watermark export and its lifecycle-sensitive handoff, an on-demand `SoundPool` listener race that can play the wrong sound, active-recording back navigation, transient video navigation state, and custom recording that is not stopped when the app backgrounds. The largest release risks are insufficient rights evidence for recognizable clips, stale public privacy disclosures, missing UMP privacy-options UI, and materially inaccurate store copy.

No P0 was assigned because the audit did not prove a defect that consistently prevents every user from completing the core task. Thirteen P1 findings should be resolved or explicitly accepted before release.

| Severity | Count |
|---|---:|
| P0 | 0 |
| P1 | 13 |
| P2 | 9 |
| P3 | 2 |
| **Total** | **24** |

## Audit health score

| # | Dimension | Score | Key finding |
|---|---|---:|---|
| 1 | Accessibility | 1/4 | Image-only onboarding is silent to TalkBack; several controls are below 48 dp and normal text on the primary color is 3.10:1. |
| 2 | Performance | 2/4 | Media paths generally use IO dispatchers, but on-demand audio loading races and always-running animations remain. |
| 3 | Responsive design | 1/4 | Fixed 340/352 dp drawers and non-scrollable Share/Trim layouts do not safely support 320 dp or 200% font. |
| 4 | Theming | 3/4 | A coherent dark palette exists, but hard-coded colors and many ad hoc alpha tiers bypass it. |
| 5 | Anti-patterns | 3/4 | The physical button is distinctive; repeated glass/card treatment and dense identical sound tiles add avoidable visual noise. |
| **Total** | **10/20** | **Acceptable, significant work needed before release** |

## Anti-pattern verdict

Fahh does not look generically AI-generated. The hero control, playful copy, and camera-first loop are specific to the product. The main systemic design weaknesses are decorative glass treatment used on many unrelated controls, an identical two-column card grid for a growing catalog, tiny all-caps status text, and continuous attention-seeking animation on navigation/support elements. Preserve the button and playful physicality; simplify secondary UI around it.

## Detailed findings

### P1 findings

#### P1-01: Every new recording is watermarked even though the preference and store promise say otherwise

- **Location:** `app/src/main/java/com/fahh/MainActivity.kt:203-227`; `app/src/main/java/com/fahh/data/repository/SettingsRepository.kt:217-224`; `STORE_LISTING.md:52,146`
- **Category:** Trust / Export / Release safety
- **Reproduction:** Record a clip on a fresh or existing install, wait for save, then inspect the shared video. There is no watermark control in the UI and `watermarkEnabled` is never consulted before `FahhWatermarkExporter` runs.
- **User impact:** Every clip receives branding despite the Play listing saying “No watermark.” The saved preference is dead state, and export delays access to a clip that could otherwise be reviewed immediately.
- **Recommended fix:** Defer watermarking as planned: hand the original recording directly to review and remove the unused preference/export dependency, or expose an explicit free opt-in and honor it. Do not force branding.
- **Before next Play release:** **Yes.**

#### P1-02: Export and gallery copy race against source deletion and activity recreation

- **Location:** `app/src/main/java/com/fahh/viewmodel/CameraViewModel.kt:117-142`; `app/src/main/java/com/fahh/MainActivity.kt:203-226`; `app/src/main/java/com/fahh/utils/FahhWatermarkExporter.kt:22-51`
- **Category:** Lifecycle / Video saving
- **Reproduction:** Stop a long recording, then rotate or background the phone while “Saving” is shown. Separately, repeat while the device is under IO load. `onVideoSaved` starts watermark export and a second coroutine starts copying the same source to MediaStore; export success deletes the source.
- **User impact:** The old activity callback can target a disposed navigation graph, the new activity has no file state, and the MediaStore copy can lose its input while reading. The user may remain on a saving screen, return to camera, or miss the expected system-gallery copy even though an app-private file survives.
- **Recommended fix:** Use one lifecycle-independent save coordinator with a single source of truth. Complete and validate the durable destination before navigation or source deletion, expose progress as ViewModel state, and make the operation idempotent across recreation.
- **Before next Play release:** **Yes.**

#### P1-03: Back navigation can abandon an active camera recording

- **Location:** `app/src/main/java/com/fahh/ui/screens/CameraScreen.kt:122-127,203-210`; `app/src/main/java/com/fahh/camera/CameraManager.kt:73-76`
- **Category:** Navigation / Camera lifecycle
- **Reproduction:** Start recording, then press system Back or the top-left back button. The route pops while CameraX is still recording, and disposal immediately unbinds all use cases.
- **User impact:** A valuable reaction can be truncated or finalized through a callback owned by a screen that has already left. The resulting navigation is race-prone and the user receives no confirmation.
- **Recommended fix:** While recording, make both back paths ask the user to stop/discard, or block back with clear feedback. While saving, disable both paths. Keep finalization owned by the recording ViewModel.
- **Before next Play release:** **Yes.**

#### P1-04: Share and Trim lose their video on rotation or process recreation

- **Location:** `app/src/main/java/com/fahh/MainActivity.kt:56,240-285`
- **Category:** Navigation / State restoration
- **Reproduction:** Reach Share or Trim, rotate the device, or enable “Don’t keep activities” and background/return. `lastVideoFile` is only `remember` state and becomes null.
- **User impact:** Share/Trim immediately pop back and the user loses the current editing context. The file may still exist, but the app does not explain where it went.
- **Recommended fix:** Persist the current file path in a `SavedStateHandle` or pass a stable encoded path/ID as a route argument, validate existence on restore, and show a recoverable missing-file state.
- **Before next Play release:** **Yes.**

#### P1-05: Rapid first taps on unloaded sounds can play the wrong sample

- **Location:** `app/src/main/java/com/fahh/audio/SoundManager.kt:53-79`
- **Category:** Audio / Race condition
- **Reproduction:** Cold-start, unlock or choose two sounds that were not among the four preloads, then tap A and B quickly. Each call replaces the single global `setOnLoadCompleteListener`; callbacks play whichever `sampleId` completes under the latest listener.
- **User impact:** A different meme sound can fire from the one the user pressed, or one tap may be dropped. This directly damages Fahh's timing-critical core promise.
- **Recommended fix:** Install one listener during initialization and keep pending play requests keyed by returned sample ID. Coalesce duplicate loads, clear pending requests on failure, and test out-of-order completion.
- **Before next Play release:** **Yes.**

#### P1-06: Long bundled clips use a short-effect playback engine

- **Location:** `app/src/main/java/com/fahh/audio/SoundManager.kt:17-80`; `app/src/main/java/com/fahh/data/catalog/SoundCatalog.kt:35-45`; duration inventory in `docs/2026-07-premium-update-plan.md:311-330`
- **Category:** Audio / Reliability
- **Reproduction:** Unlock and cold-play John Cena (about 7.5 seconds), Sad Violin (about 22.5 seconds), Prowler (about 13.7 seconds), Spooderman (about 7.7 seconds), or Ultra Suspense (about 9.2 seconds) on a low-memory device. All bundled resources are loaded through `SoundPool`, which is designed for short, decoded-in-memory effects.
- **User impact:** Long clips can fail to load, silently drop the first press, consume excessive decoded memory, or behave inconsistently under rapid switching. A user may watch a rewarded ad for a sound that does not reliably play.
- **Recommended fix:** Record duration in catalog metadata, retain `SoundPool` for short latency-sensitive effects, and route long clips through one lifecycle-aware `MediaPlayer`/Media3 instance with explicit stop/restart behavior. Device-test every rewarded clip before release.
- **Before next Play release:** **Yes.**

#### P1-07: Recognizable bundled clips lack release-grade rights evidence

- **Location:** `docs/sound-rights-ledger.csv:2-25`; `app/src/main/java/com/fahh/data/catalog/SoundCatalog.kt:32-48`
- **Category:** Play policy / Intellectual property
- **Reproduction:** Inspect ledger rows for Emotional Damage, FBI Open Up, GTA Wasted, John Cena, Minecraft Hurt, Roblox Oof, Sad Violin, Prowler, Undertaker Bell, and other recognizable works. Source URL and rightsholder/creator are blank; the evidence is only “Owner confirmed commercial redistribution permission.”
- **User impact:** A Play rejection, takedown, or rightsholder complaint could block the release or threaten the developer account. Google Play's policy specifically lists soundboards using copyrighted clips as an infringement example.
- **Recommended fix:** Before shipping each clip, attach written permission or a verifiable commercial license with source, rightsholder, scope, and attribution. Remove any clip that cannot be documented. Do not restore the four intentionally removed staged files.
- **Before next Play release:** **Yes, release blocker.** See [Google Play Intellectual Property policy](https://support.google.com/googleplay/android-developer/answer/9888072).

#### P1-08: Public privacy disclosures are stale and internally contradictory

- **Location:** `app/src/main/java/com/fahh/ui/screens/PrivacyPolicyScreen.kt:62-90`; `STORE_LISTING.md:75-93`; public page `https://tracker.dog/fahh-privay-policy/`
- **Category:** Privacy / Play policy
- **Reproduction:** Compare the July in-app text, February public page, current AdMob SDK behavior, custom sounds, and Play Data Safety notes. The public page says Fahh does not transmit personal data and does not share user data, then says AdMob may collect identifiers, usage data, and approximate location. It omits custom-audio retention/deletion and rewarded unlock scope.
- **User impact:** Users receive conflicting explanations, and Play requires the public policy, in-app disclosure, and Data Safety form to remain accurate and consistent.
- **Recommended fix:** Update the public page and store declaration together. Identify the developer and privacy contact, describe AdMob collection/sharing and purposes, local custom audio/video retention and deletion, permission use, and current SDKs.
- **Before next Play release:** **Yes.** See [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) and [Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469).

#### P1-09: UMP privacy options cannot be reopened when required

- **Location:** `app/src/main/java/com/fahh/utils/ConsentManager.kt:19-57`; `app/src/main/java/com/fahh/ui/components/SettingsSheet.kt:58-354`
- **Category:** Consent / Ads
- **Reproduction:** Configure an EEA or regulated-region test device where `privacyOptionsRequirementStatus` is `REQUIRED`, complete the initial form, then search More cool and Privacy & Terms for a privacy-options entry point. None exists.
- **User impact:** A user cannot revise privacy choices at any time when the configured message requires it.
- **Recommended fix:** Expose observable privacy-options requirement state, show a visible “Ad privacy choices” row only when required, and call `UserMessagingPlatform.showPrivacyOptionsForm()` from the current Activity.
- **Before next Play release:** **Yes.** See the current [UMP Android setup](https://developers.google.com/admob/android/privacy).

#### P1-10: Core accessibility targets and contrast do not meet the product standard

- **Location:** `app/src/main/java/com/fahh/ui/theme/Color.kt:6,10`; `app/src/main/java/com/fahh/ui/components/SoundGrid.kt:147-164,195-212`; `app/src/main/java/com/fahh/ui/components/SidebarMenu.kt:170-179`; `app/src/main/java/com/fahh/ui/screens/MainScreen.kt:1026-1060`
- **Category:** Accessibility
- **Reproduction:** Enable TalkBack and large text, then navigate sound previews, notice dismiss, and edge tabs. Measure white on `Primary` (`#FF5A3B`): 3.10:1. Preview is 32 dp, notice dismiss is 24 dp, and edge tabs are roughly 20 by 26 dp. Status text is 7 sp.
- **User impact:** Low-vision and motor-impaired users cannot reliably read or hit essential sound controls. White normal text on the primary background fails the 4.5:1 target.
- **Recommended fix:** Use a primary/on-primary pairing that meets contrast, make all interactive bounds at least 48 dp, raise status text to a readable Material label size, and announce selected/locked state in semantics.
- **Before next Play release:** **Yes.**

#### P1-11: Image-only onboarding is inaccessible to TalkBack

- **Location:** `app/src/main/java/com/fahh/ui/screens/OnboardingScreen.kt:31-71`; `app/src/main/java/com/fahh/ui/screens/UpdateOnboardingScreen.kt:24-45`
- **Category:** Accessibility / Onboarding
- **Reproduction:** Clear app data, enable TalkBack, and launch. The six primary onboarding pages are JPGs with `contentDescription = null`; update pages repeat only “What's new.”
- **User impact:** A screen-reader user hears navigation controls but none of the product explanation, permission context, or new-feature content.
- **Recommended fix:** Prefer real Compose text over text baked into images. As an immediate fix, provide page-specific descriptions and announce page position; preserve images as decorative support.
- **Before next Play release:** **Yes.**

#### P1-12: Narrow phones and large fonts can clip drawers and primary flows

- **Location:** `app/src/main/java/com/fahh/ui/components/SidebarMenu.kt:76-88`; `app/src/main/java/com/fahh/ui/components/SettingsSheet.kt:75-88`; `app/src/main/java/com/fahh/ui/screens/ShareScreen.kt:61-271`; `app/src/main/java/com/fahh/ui/screens/TrimScreen.kt:57-245`
- **Category:** Responsive design
- **Reproduction:** Use a 320 dp emulator or 200% font. Open either drawer, then Share and Trim. Drawers demand 352/340 dp. Share and Trim use fixed vertical stacks without scrolling, including a 300 dp trim preview.
- **User impact:** Controls and copy can be off-screen or overlap system insets, blocking share, delete, trim, or navigation.
- **Recommended fix:** Constrain drawers to the available width, use adaptive grid/list columns, add vertical scrolling or constraint-aware preview sizing, and test 320 dp plus 200% font.
- **Before next Play release:** **Yes.**

#### P1-13: Custom recording can continue unexpectedly through backgrounding and has a rotation-dependent timer

- **Location:** `app/src/main/java/com/fahh/audio/CustomSoundRecorder.kt:12-57`; `app/src/main/java/com/fahh/ui/screens/MySoundsScreen.kt:105-116,140-144`
- **Category:** Audio recording / Lifecycle / Privacy
- **Reproduction:** Start a custom recording, background the app, or rotate before five seconds. The recorder has no platform maximum duration and is cancelled only when the composable leaves via its own back path or when the app-scoped SoundViewModel is cleared. Rotation restarts the composable countdown from five seconds.
- **User impact:** The microphone can remain active longer than the UI promises, and a saved clip can exceed five seconds. Background recording is surprising for a local privacy-first feature.
- **Recommended fix:** Enforce maximum duration in the recorder layer, observe lifecycle stop to cancel or finalize explicitly, derive countdown from an elapsed-time deadline, and restore/announce the active state safely.
- **Before next Play release:** **Yes.**

### P2 findings

#### P2-01: Deleting from Fahh does not delete the separate MediaStore copy

- **Location:** `app/src/main/java/com/fahh/viewmodel/CameraViewModel.kt:205-243`; `app/src/main/java/com/fahh/MainActivity.kt:257-260,307-309`; `app/src/main/java/com/fahh/ui/screens/GalleryScreen.kt:169-191`
- **Reproduction:** Record, confirm the clip in the system gallery, then delete it from Share or Reaction Gallery.
- **User impact:** Fahh says deletion cannot be undone, but the public-gallery duplicate can remain. Users may believe a sensitive reaction was removed when it was not.
- **Recommended fix:** Store the MediaStore URI with the app item and delete both representations, or use one canonical MediaStore destination that Share/Gallery operate on.
- **Before next Play release:** **Prefer yes; mandatory before strengthening deletion/privacy claims.**

#### P2-02: Trim can be abandoned mid-write and may leave corrupt gallery entries

- **Location:** `app/src/main/java/com/fahh/ui/screens/TrimScreen.kt:44,64-66,202-230`; `app/src/main/java/com/fahh/utils/VideoTrimUtils.kt:108-117`
- **Reproduction:** Tap Finish Trimming, then immediately press system Back or top Back. Also force a muxer failure with a boundary/corrupt input.
- **User impact:** The composition scope is cancelled while blocking IO may continue, leaving an orphaned output. `VideoTrimUtils` does not delete a failed output file.
- **Recommended fix:** Disable/confirm back while saving, use ViewModel-owned work if it must survive recreation, write to a temporary suffix, validate, atomically publish, and delete on all failures.
- **Before next Play release:** **Yes if the change remains low risk.**

#### P2-03: Rewarded offline/consent-pending taps can appear to do nothing

- **Location:** `app/src/main/java/com/fahh/ui/screens/MainScreen.kt:192-208,246-289`; `app/src/main/java/com/fahh/ui/screens/MySoundsScreen.kt:75-83,123-137`
- **Reproduction:** Launch offline or while consent is unresolved, open an unlock, and tap Watch Ad. `loadRewardedAd()` returns immediately when ads cannot be requested; My Sounds does not always set new explanatory state.
- **User impact:** Optional unlocks look broken and users cannot distinguish offline, consent-pending, no-fill, and retrying states.
- **Recommended fix:** Model `consent_pending`, `offline`, `loading`, `ready`, `no_fill`, and `show_failed`; retain the draft and provide a retry action without blocking core playback.
- **Before next Play release:** **Recommended.**

#### P2-04: Direct platform shares are not counted and URI grants are less robust than the generic path

- **Location:** `app/src/main/java/com/fahh/ui/screens/ShareScreen.kt:158-188,313-334`; `app/src/main/java/com/fahh/utils/ShareUtils.kt:17-31`
- **Reproduction:** Share successfully to installed WhatsApp, Instagram, or TikTok. `shareToApp` never calls the supplied completion callback on success and does not attach `ClipData` or explicit per-handler grants like `ShareUtils`.
- **User impact:** Rating/share milestones undercount direct shares, and some destination versions may reject the URI grant.
- **Recommended fix:** Route all video sharing through one helper, add `ClipData`, grant read permission, and record “share sheet opened” only after an intent launches successfully.
- **Before next Play release:** **No, unless reports show destination failures.**

#### P2-05: Data initialization failures have no recoverable UI state

- **Location:** `app/src/main/java/com/fahh/viewmodel/SoundViewModel.kt:121-143`; `app/src/main/java/com/fahh/data/repository/SettingsRepository.kt:243-258`
- **Reproduction:** Corrupt the Room database or `last_active_date`, then launch. Catalog sync and `LocalDate.parse` run in an unguarded ViewModel coroutine.
- **User impact:** The app can crash or remain with an empty sound list without a useful recovery path.
- **Recommended fix:** Validate persisted dates with `runCatching`, expose initialization loading/error state, retry safe catalog sync, and never destroy unlock data as a recovery shortcut.
- **Before next Play release:** **Recommended.**

#### P2-06: Store listing is materially stale beyond the watermark claim

- **Location:** `STORE_LISTING.md:40-66`; `README.md:38-49`; `app/build.gradle.kts:31-32`; `app/src/main/java/com/fahh/ui/components/SettingsSheet.kt:343-350`
- **Reproduction:** Compare source and listing. The app bundles 36 sounds, not 12; camera switching is disabled during recording; the app version is 1.1.0 while More cool says 1.0.6; the under-10-MB claim has not been verified after Media3 and new audio.
- **User impact:** Users and Play reviewers receive promises the binary does not match.
- **Recommended fix:** Generate counts/version labels from source where possible, remove unverified size and in-recording-switch claims, and update Play copy/screenshots only after the release candidate is final.
- **Before next Play release:** **Yes.**

#### P2-07: Room migration coverage exists but is not part of normal verification

- **Location:** `app/src/androidTest/java/com/fahh/SoundDatabaseMigrationTest.kt:19-106`; `app/src/test/java/com/fahh/SoundCatalogTest.kt:8-22`
- **Reproduction:** Run `testDebugUnitTest`; only four JVM tests execute. The v1-to-v2 preservation test is an instrumented test and is not run by the requested normal task.
- **User impact:** A future schema/catalog change can pass CI without executing the only upgrade preservation check.
- **Recommended fix:** Keep the instrumented migration test for SQLite fidelity, add DAO/repository JVM tests where practical, and make connected migration testing an explicit pre-release gate.
- **Before next Play release:** **Recommended.**

#### P2-08: Minified release behavior is not verified and keep rules hide shrinker problems

- **Location:** `app/build.gradle.kts:49-60`; `app/proguard-rules.pro:5-50`
- **Reproduction:** Inspect configuration: release minification is enabled, while entire Hilt, Room, CameraX, Ads, coroutines, model, and entry-point packages are kept. No release bundle/minification verification was run because signing credentials are intentionally external.
- **User impact:** Blanket keeps inflate download size and can mask missing consumer rules; removing them later without tests can introduce release-only failures.
- **Recommended fix:** Do not change rules blindly. Build a signed internal AAB with external credentials, exercise camera/export/ads/Room, then remove only redundant keeps in small verified steps.
- **Before next Play release:** **Signed internal test required; rule cleanup can follow.**

#### P2-09: Continuous decorative motion ignores reduced-motion preference

- **Location:** `app/src/main/java/com/fahh/ui/screens/MainScreen.kt:913-920,1026-1040,1064-1075`; `app/src/main/java/com/fahh/ui/components/SidebarMenu.kt:69-75`
- **Reproduction:** Enable Android “Remove animations,” open Home and the sound drawer. Color cycling, edge nudging, bubble float, and support pulse are built from infinite transitions without a reduced-motion branch.
- **User impact:** Motion-sensitive users still see unnecessary animation and low-end devices continuously recompose decorative elements.
- **Recommended fix:** Disable infinite decorative transitions when system animator duration is zero, and reserve motion for press, recording, save, and unlock state.
- **Before next Play release:** **No, low-risk P2 candidate.**

### P3 findings

#### P3-01: Theme tokens are bypassed by many hard-coded colors and alpha tiers

- **Location:** `app/src/main/java/com/fahh/ui/theme/Color.kt:5-23`; examples in `MainScreen.kt:238-240`, `ShareScreen.kt:148-149,216-222`, `SidebarMenu.kt:82-85,209-217`
- **Reproduction:** Search for `Color(0x` and `Color.White.copy(alpha` across UI files.
- **User impact:** Contrast and dark-theme adjustments require error-prone screen-by-screen edits, and visual hierarchy drifts.
- **Recommended fix:** Define semantic surface/text/action/status tokens and migrate opportunistically when touching each component.
- **Before next Play release:** **No.**

#### P3-02: Gallery thumbnail extraction can retain a retriever on an exception path

- **Location:** `app/src/main/java/com/fahh/ui/screens/GalleryScreen.kt:203-215`; similar helper in `MySoundsScreen.kt:452-458`
- **Reproduction:** Open Gallery with a corrupt MP4 or edit a custom sound whose file becomes unreadable while metadata is read. `release()` is not in `finally` in these helpers.
- **User impact:** Repeated corrupt files can leak native media resources until the process exits.
- **Recommended fix:** Always release `MediaMetadataRetriever` in `finally`.
- **Before next Play release:** **No, safe cleanup when nearby code changes.**

## Navigation and state-flow assessment

- Navigation Compose replaced the old string router and normal Main, Privacy, More cool, Gallery, Camera, Share, Trim, and Custom sounds back-stack paths are structurally sound.
- Onboarding and update onboarding correctly wait for resolved DataStore values and remove Loading/Onboarding from the back stack.
- Gallery back explicitly returns to Main, but Share/Trim rely on transient in-memory file state and camera callbacks can navigate after their owner is gone.
- Drawer back handling is mostly correct. The custom RTL wrapper used to place a right drawer should receive TalkBack/RTL device testing because visual and accessibility traversal direction can diverge.

## Persistence and migration assessment

- Positive: Room v1-to-v2 migration replaces generated IDs with stable sound IDs and preserves old unlock state, including the Romance Sax rename.
- Positive: Catalog metadata refresh does not relock earned sounds, and there is no destructive migration fallback.
- Positive: DataStore edits are atomic and custom audio files live under private app storage.
- Risk: custom sound metadata uses an unversioned SharedPreferences JSON blob. Missing files are silently dropped, which is safe for crashes but gives no user explanation. This is acceptable for 1.1.0 if backup remains disabled and file deletion is tested.
- Risk: no normal JVM test covers custom-slot grandfathering, custom metadata deletion, selected custom-sound fallback, or DataStore edge cases.

## Release checks performed

- `git status --short --branch`: correct branch, clean except protected untracked `awddx4.jpg` and `new_onboarding.jpg`.
- `./gradlew.bat testDebugUnitTest`: **passed**, 4 tests, 34 tasks (33 up to date).
- `./gradlew.bat lintDebug`: **did not complete within 120 seconds** and produced no report. This is an unresolved tooling/release signal, not a test pass.
- No debug APK and no release artifact were generated.
- `targetSdk 35` currently meets the Play update requirement. See [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878).
- Public privacy URL returned HTTP 200 on July 11, 2026, but its content is stale as described above.

## Remediation completed after the baseline audit

The findings above intentionally describe the audited baseline at `f92d2a4`. The following safe fixes were implemented afterward:

- **P1-01 fixed:** removed forced watermark export, its dead preference, and the Media3 export dependency. New clips go directly to review without branding.
- **P1-02 partially fixed:** removed the source-deleting export race and moved finalized-file delivery into retained CameraViewModel state. A single canonical MediaStore/app-file design remains future work.
- **P1-03 fixed:** system and top-bar Back now provide feedback instead of leaving during recording or saving.
- **P1-04 fixed for configuration/process recreation:** current video path uses saveable state and is validated by existing Share/Trim guards.
- **P1-05 fixed:** a single `SoundPool` listener now queues pending taps by sample ID and handles out-of-order loads deterministically.
- **P1-09 fixed in code:** More cool exposes UMP privacy options when the SDK says the entry point is required.
- **P1-10 substantially improved:** action red now supports white text at 4.62:1, preview/dismiss/edge targets are at least 48 dp, and tiny sound status labels were raised to 10 sp.
- **P1-11 fixed for current image onboarding:** every page now has a page-specific TalkBack description. Converting baked-in image text to native text remains preferred.
- **P1-12 substantially improved:** drawers constrain to available width, large-font sound grids become one column, and Share/Trim can scroll.
- **P1-13 substantially improved:** the recorder enforces a five-second maximum and active custom recording is cancelled when the screen lifecycle stops.
- **P2-03 improved:** consent-pending/ad-loading states now produce user-visible retry guidance.
- **P2-05 partially fixed:** malformed persisted activity dates no longer crash parsing.
- **P2-06 fixed in repository copy:** sound count, camera-switch wording, size claim, README ad model, and in-app version label now match source.
- **P3-02 fixed:** gallery and custom-audio metadata retrievers now release on exception paths.

Still release-blocking after code remediation: P1-06 long-clip playback, P1-07 rights evidence, P1-08 public privacy/Data Safety synchronization, real-device camera/media verification, and the signed minified internal release check.

## Positive findings to preserve

- The hero button has a clear button role and a sound-specific accessibility label.
- Core playback, camera, trim, sharing, and local gallery are not interrupted by forced interstitial ads.
- Reward callbacks unlock only after `onUserEarnedReward`; dismissal alone does not grant an entitlement.
- Rewarded sound and custom-slot copy states that unlocks remain on this device.
- Camera file copy is off the main thread, trim work is dispatched to IO, and media cleanup generally uses `finally`.
- Custom deletion removes the private audio file and resets selection if the deleted sound was active.
- The two intentionally removed risky staged sounds plus the other two removed staged files remain absent from the bundled catalog.

## Recommended release order

1. Fix deterministic audio playback and active-recording/save navigation.
2. Remove or make watermark export truly opt-in, then make video state recreation-safe.
3. Fix the highest-impact accessibility/responsive issues and custom-recorder lifecycle.
4. Update public privacy policy, add UMP privacy options, and synchronize Data Safety.
5. Obtain release-grade rights evidence or remove undocumented recognizable clips.
6. Update store copy/version/counts, then run unit tests, lint, connected migration tests, and a signed internal AAB on real devices.
7. Finish with a focused UI polish pass and repeat this audit.
