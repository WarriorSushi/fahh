# Fahh Code Review — Bugs & Issues

**Date:** March 18, 2026
**Scope:** Full codebase review covering bugs, performance, error handling, architecture, security, and dependencies.

> **Ad Metrics Snapshot (March 18):** $1.30 earnings, 2.91K requests, 261 impressions, 94.23% match rate, $5.00 eCPM. ARPU requires Firebase link.

---

## 1. Bugs

### BUG-01: CoroutineScope leak in MainActivity navigation
- **Severity:** Critical
- **File:** `MainActivity.kt:130`
- **Description:** `kotlinx.coroutines.MainScope().launch` creates an unmanaged coroutine scope that is never cancelled. If the activity is destroyed while the coroutine is running, it will leak and potentially crash when trying to update `currentScreen` on a destroyed activity.
- **Fix:** Use a `rememberCoroutineScope()` inside the composable, or hoist the scope from the `setContent` block and use `scope.launch` instead of `MainScope().launch`.

### BUG-02: `startScreen` is computed once and never re-evaluated
- **Severity:** High
- **File:** `MainActivity.kt:52-53`
- **Description:** `startScreen` is calculated from `isFirstRun` which has `initialValue = false`. Since `isFirstRun` defaults to `false` and the DataStore value loads asynchronously, `startScreen` will always be `"main"` on first computation. The `currentScreen` state is initialized with this stale value and never updated when `isFirstRun` eventually emits `true`. First-time users will skip onboarding.
- **Fix:** Use a `LaunchedEffect(isFirstRun)` to navigate to onboarding when `isFirstRun` becomes `true`, or use a loading/splash state until the DataStore value is resolved.

### BUG-03: `stopRecording()` sets `isRecording = false` before `Finalize` event
- **Severity:** High
- **File:** `CameraViewModel.kt:151-158`
- **Description:** `stopRecording()` immediately sets `_isRecording.value = false` and nulls `currentRecording` in the `finally` block. However, the `VideoRecordEvent.Finalize` callback will also try to set these values and call `onVideoSaved`. Since `currentRecording` is already null, the `Finalize` event still fires on the executor, but the state is already reset. This creates a race condition where the UI briefly shows "not recording" then the finalize callback tries to update state again. More critically, if `stopRecording` throws, the recording reference is lost.
- **Fix:** Remove the state updates from `stopRecording()` and let only the `Finalize` callback handle state cleanup. Just call `currentRecording?.stop()`.

### BUG-04: Rewarded ad unlock dialog doesn't close after reward
- **Severity:** Medium
- **File:** `MainScreen.kt:205-210`
- **Description:** `onRewardEarned` callback unlocks the sound and shows confetti but never sets `soundToUnlock = null`. The dialog remains open after the reward is earned until `onDismissed` fires separately. This can cause a brief flash of the stale dialog.
- **Fix:** Add `soundToUnlock = null` inside the `onRewardEarned` block.

### BUG-05: Camera toggle doesn't restart camera preview
- **Severity:** Medium
- **File:** `CameraViewModel.kt:70-76`, `CameraScreen.kt:105`
- **Description:** `toggleCamera()` updates `_cameraSelector` state, and `CameraScreen` has a `LaunchedEffect(hasPermissions, cameraSelector, previewViewRef)` that should restart the camera. However, `startCamera` in `CameraManager` uses `cameraProvider.unbindAll()` then `bindToLifecycle()`. If the user toggles while recording, the recording will be silently lost because unbindAll stops all use cases including the active recording, with no finalize event guarantee.
- **Fix:** Disable camera toggle during recording (partially done in UI via `if (!isRecording)` on the button click, but the ViewModel function has no guard).

### BUG-06: `SoundManager.release()` called but singleton lives forever
- **Severity:** Medium
- **File:** `SoundManager.kt:63-65`, `SoundViewModel.kt:168-171`
- **Description:** `SoundManager` is a `@Singleton` scoped to the app's lifecycle. `SoundViewModel.onCleared()` calls `soundManager.release()`, which releases the underlying `SoundPool`. But since `SoundManager` is a singleton, if another ViewModel or a new instance of `SoundViewModel` tries to use it, `playSound()` will fail silently because the `SoundPool` is released. On config changes where the ViewModel is recreated, sounds will stop working.
- **Fix:** Either don't release in `onCleared()` (let the process handle it), or make `SoundManager` ViewModel-scoped, or add a re-initialization check in `playSound()`.

### BUG-07: VideoTrimUtils muxer not released on error path
- **Severity:** Medium
- **File:** `VideoTrimUtils.kt:35-116`
- **Description:** If `muxer.stop()` throws (e.g., no samples written check passes but muxer state is bad), `muxer.release()` is never called because only `extractor.release()` is in the `finally` block. The muxer should also be in the finally/cleanup path.
- **Fix:** Wrap muxer in a try-finally or use `runCatching` for `muxer.stop()` and always call `muxer.release()`.

### BUG-08: Database has no migration strategy
- **Severity:** Medium
- **File:** `SoundDatabase.kt:8`
- **Description:** The database is at version 1 with `exportSchema = false` and no migration strategy. If you ever bump the version, Room will throw `IllegalStateException` and crash because there's no `fallbackToDestructiveMigration()`. Users would lose their unlock states.
- **Fix:** Add `.fallbackToDestructiveMigration()` as a safety net, or better, define proper migrations when the schema changes. Also set `exportSchema = true` for migration validation.

---

## 2. Performance Issues

### PERF-01: All 12 sounds preloaded into SoundPool at app start
- **Severity:** Medium
- **File:** `SoundManager.kt:32-51`
- **Description:** All sound files are loaded into memory at singleton creation (app start), regardless of whether they are locked or will ever be played. This increases app startup time and memory footprint.
- **Fix:** Lazy-load sounds on first play, or at minimum defer preloading to a background thread after the UI is ready.

### PERF-02: `rememberInfiniteTransition` running continuously on MainScreen
- **Severity:** Low
- **File:** `MainScreen.kt:554-563` (SwipeEdgeTab)
- **Description:** The `SwipeEdgeTab` runs an infinite animation even when the sidebar is open or when the user is on a different screen entirely (due to `AnimatedContent` keeping state). This wastes CPU/battery on continuous recomposition.
- **Fix:** Conditionally run the animation only when the composable is actually visible.

### PERF-03: `copyRecordingToGallery` runs on main thread
- **Severity:** Medium
- **File:** `CameraViewModel.kt:122`, `CameraViewModel.kt:194-233`
- **Description:** `copyRecordingToGallery` does file I/O (copying video bytes via `inputStream().copyTo()`) inside the `VideoRecordEvent.Finalize` callback which runs on the main executor. Large videos could cause ANR.
- **Fix:** Move `copyRecordingToGallery` to a coroutine on `Dispatchers.IO`.

---

## 3. Error Handling Gaps

### ERR-01: No error handling for Room database init failure
- **Severity:** High
- **File:** `SoundViewModel.kt:58-66`
- **Description:** The `init` block launches a coroutine that calls `repository.allSounds.first()` and `repository.insertAll()`. If the database is corrupted or the query fails, the exception is unhandled and will crash the coroutine silently (swallowed by `viewModelScope`). The user would see an empty sound list with no feedback.
- **Fix:** Wrap in try-catch and provide a fallback or error state.

### ERR-02: `AdManager.initialize` failure is silently ignored
- **Severity:** Medium
- **File:** `FahhApplication.kt:11`
- **Description:** `MobileAds.initialize(context) {}` has an empty completion listener. If initialization fails, no retry or logging occurs.
- **Fix:** Add logging in the completion callback. Consider retrying on failure.

### ERR-03: No handling for file not found in ShareScreen
- **Severity:** Medium
- **File:** `ShareScreen.kt:91`
- **Description:** `VideoView.setVideoPath` is called with the file path, but if the file was deleted externally (e.g., by a storage cleaner), no error is shown. The screen will display a black video area with no feedback.
- **Fix:** Check `videoFile.exists()` before displaying, show an error state if missing.

### ERR-04: `ConsentManager.canRequestAds` never checked before loading ads
- **Severity:** Medium
- **File:** `MainScreen.kt:166`, `ConsentManager.kt:46-49`
- **Description:** `loadRewardedAd()` is called in `LaunchedEffect(Unit)` without checking `ConsentManager.canRequestAds()`. In EEA regions, this could load ads before consent is gathered, violating GDPR.
- **Fix:** Check `ConsentManager.canRequestAds(context)` before calling `loadRewardedAd()`.

---

## 4. Architecture Issues

### ARCH-01: Manual navigation with string-based screen names
- **Severity:** Medium
- **File:** `MainActivity.kt:52-181`
- **Description:** Navigation is managed with a `currentScreen` string and a giant `when` block. This is fragile (typos cause silent failures), hard to deep-link, and doesn't support proper back stack management. The `previousScreen` tracking is manual and error-prone.
- **Fix:** Consider using Jetpack Navigation Compose or at minimum use a sealed class for screen states.

### ARCH-02: Dual singleton pattern for SoundDatabase
- **Severity:** Low
- **File:** `SoundDatabase.kt:12-27`, `DatabaseModule.kt:19-21`
- **Description:** `SoundDatabase` has its own `companion object` singleton pattern, AND Hilt provides it as `@Singleton` via `DatabaseModule`. The Hilt module calls `getDatabase()` which uses the companion singleton. This works but is redundant — if Hilt ever creates two instances of the module (it won't, but still), the companion object guards it. Pick one pattern.
- **Fix:** Remove the companion object singleton and let Hilt manage the single instance directly via `Room.databaseBuilder(...).build()` inside the `@Provides` function.

### ARCH-03: `CameraManager` is a Singleton but holds lifecycle-bound state
- **Severity:** Medium
- **File:** `CameraManager.kt:19-20`
- **Description:** `CameraManager` is `@Singleton` but stores `cameraProvider` and `videoCapture` which are bound to a specific `LifecycleOwner`. If the activity is recreated, the old references become stale. `releaseCamera()` clears them, but there's a window where stale state could be accessed.
- **Fix:** Scope `CameraManager` to the activity or ViewModel lifecycle rather than the app singleton scope.

### ARCH-04: `SoundViewModel` uses `AndroidViewModel` unnecessarily
- **Severity:** Low
- **File:** `SoundViewModel.kt:22-27`
- **Description:** `SoundViewModel` extends `AndroidViewModel` but never calls `getApplication()`. All context-dependent work is done by injected dependencies. This can be a plain `ViewModel`.
- **Fix:** Change to `ViewModel` and remove the `application` constructor parameter.

---

## 5. Security Concerns

### SEC-01: Production Ad Unit ID hardcoded in source
- **Severity:** High
- **File:** `MainScreen.kt:94`
- **Description:** The production rewarded ad unit ID `ca-app-pub-1006057089920582/1635547968` is hardcoded as a constant in the source file. If this repo is public or shared, the ad unit ID is exposed. While not a secret per se (it's embedded in the APK anyway), it's best practice to externalize it.
- **Fix:** Move ad unit IDs to `BuildConfig` fields via `build.gradle.kts` or `local.properties` / environment variables.

### SEC-02: Signing config passwords fallback to empty string
- **Severity:** Medium
- **File:** `build.gradle.kts:29-31`
- **Description:** `storePassword`, `keyAlias`, and `keyPassword` fall back to empty strings if env vars aren't set. This means a release build can be attempted with invalid credentials, producing confusing errors instead of a clear build failure.
- **Fix:** Fail the build explicitly if env vars are missing for release builds (e.g., use `?: error("FAHH_KEYSTORE_PASSWORD not set")`), or only configure the signing config when env vars are present.

---

## 6. Dependency Issues

### DEP-01: Outdated Compose BOM (Oct 2023)
- **Severity:** High
- **File:** `build.gradle.kts:73`
- **Description:** `compose-bom:2023.10.01` is over 2 years old. It misses significant bug fixes, performance improvements, and new Material3 components. The `kotlinCompilerExtensionVersion = "1.5.8"` is also old and may have compatibility issues with newer Kotlin versions.
- **Fix:** Update to the latest stable Compose BOM (2025.x+) and update `kotlinCompilerExtensionVersion` accordingly. Consider migrating from `kapt` to KSP for Compose compiler.

### DEP-02: Using `kapt` instead of KSP
- **Severity:** Medium
- **File:** `build.gradle.kts:4, 100, 106`
- **Description:** The project uses `kapt` for Room and Hilt annotation processing. `kapt` is in maintenance mode and significantly slower than KSP. Hilt and Room both support KSP now.
- **Fix:** Migrate to KSP plugin and replace `kapt()` dependencies with `ksp()`.

### DEP-03: Outdated Hilt version
- **Severity:** Medium
- **File:** `build.gradle.kts:105-106`
- **Description:** Dagger Hilt `2.48` is from late 2023. Newer versions include KSP support improvements and bug fixes.
- **Fix:** Update to latest stable Hilt (2.51+).

### DEP-04: `LocalLifecycleOwner` deprecated in newer Compose
- **Severity:** Low
- **File:** `CameraScreen.kt:32`
- **Description:** `LocalLifecycleOwner` from `androidx.compose.ui.platform` is deprecated in favor of `androidx.lifecycle.compose.LocalLifecycleOwner`. This will cause a warning or compilation error when Compose BOM is updated.
- **Fix:** Import from `androidx.lifecycle.compose.LocalLifecycleOwner` instead.

### DEP-05: Java 8 target compatibility
- **Severity:** Low
- **File:** `build.gradle.kts:50-55`
- **Description:** `jvmTarget = "1.8"` and `JavaVersion.VERSION_1_8` are used. While functional, Java 11+ is now the recommended target for Android and unlocks newer APIs and better performance from the Kotlin compiler.
- **Fix:** Update to `JavaVersion.VERSION_11` and `jvmTarget = "11"`.

### DEP-06: ProGuard rules are overly broad
- **Severity:** Low
- **File:** `proguard-rules.pro:6-7, 17-18, 21-22`
- **Description:** Rules like `-keep class dagger.hilt.** { *; }` and `-keep class androidx.camera.** { *; }` keep entire libraries from being shrunk. This defeats the purpose of R8 minification, resulting in a larger APK.
- **Fix:** Use more targeted keep rules. Hilt and CameraX include their own consumer ProGuard rules via their AARs, so most of these manual rules are unnecessary. Remove the blanket keeps and only add rules if R8 actually strips something needed at runtime.

---

## Summary

| Severity | Count |
|----------|-------|
| Critical | 1     |
| High     | 4     |
| Medium   | 13    |
| Low      | 5     |
| **Total**| **23**|

**Top priorities:**
1. Fix the `MainScope()` leak in MainActivity (BUG-01)
2. Fix onboarding never showing for new users (BUG-02)
3. Fix `SoundManager` singleton release crash (BUG-06)
4. Check GDPR consent before loading ads (ERR-04)
5. Update Compose BOM and dependencies (DEP-01)
