# LabelLens Engineering Report: In-App CameraX & Cloud AI Assist (Session r2)

**Task ID**: `labellens-20261105-scan-ai-camera-r2` (Requested by Muse)  
**Date**: 2026-11-05T17:30:00+07:00  
**Branch**: `feature/scan-ai-camera`  
**Status**: DONE  
**Base Commit**: `cde4894`  
**Commits on Branch**:
- `6d73592` `feat(camera): add CameraX in-app camera viewfinder and routing for Android`
- `1e91677` `feat(ai): integrate live Tier 2/3 backend AI assist with pure trigger and merge policies`
- `34e3eb1` `feat(camera): add emulator camera test helper and capture proof screenshots`

---

## 1. Session Context & Scope

The previous session (r1) terminated early due to an account quota exhaustion error before producing any commits or output. Session r2 was initialized as a fresh execution starting directly on `feature/scan-ai-camera` at `cde4894`, delivering the complete combined scope of Part A (In-App CameraX camera) and Part B (Cloud AI Assist integration).

### Part A: In-App CameraX Viewfinder (Android)
- **CameraX 1.4.1** integration in `shared/build.gradle.kts` and `libs.versions.toml`.
- **UI Architecture**: Implemented pure expect/actual pattern via `CameraScreen.kt` (common) and `CameraScreen.android.kt` (CameraX `PreviewView`, lifecycle-bound `ProcessCameraProvider`, `ImageCapture` use-case). Fallback stubs for `iosMain`, `jvmMain`, and `webMain` gracefully route to existing file pickers.
- **Controls & Overlay**: High-contrast guide frame overlay (`RoundedCornerShape(20.dp)`), subtitle guide chip (`Res.string.camera_guide_hint`), top action bar with Close button (`Icons.Default.Close`) and Torch toggle (`Res.drawable.ic_flash`), and a bottom capture button.
- **Storage & Privacy**: Photos captured via CameraX are written to internal app cache (`context.cacheDir/camera_capture_<timestamp>.jpg`), strictly avoiding pollution of the user's public gallery.
- **Permission Flow**: Runtime permission request for `android.permission.CAMERA` with explanatory rationale UI and graceful fallback to FileKit photo picker on denial.
- **Routing**: Wired `CameraScreenRoute` in `Routes.kt` and `AppNavigation.kt`. Both the center "Quét" bottom navigation bar tab and the ScanScreen "Chụp ảnh" button route directly into `CameraScreenRoute` on Android.

### Part B: Cloud AI Assist (Tier 2/3 Backend)
- **Live Backend Endpoint**: Configured `LABELLENS_BACKEND_URL` (`https://labellens-backend.appdexter.workers.dev/v1/analyze`) with `x-app-key` header authentication.
- **Key Security**: Extracted app key securely from the local backend environment (`LabelLensBackend/.dev.vars`) into gitignored `MobileApp/local.properties`. Passed via Gradle `buildConfigField("LABELLENS_APP_KEY", ...)` defaulting to empty string. The key is never logged, printed, or committed to source control.
- **Pure Trigger Policy (`AiAssistTriggerPolicy`)**: Pure domain function determining when to invoke AI assist. Triggers when `aiAssistEnabled == true` and either `ocrConfidence < 0.6` or the label is incomplete (unmask count < 2 or nutrition facts missing).
- **Pure Merge Policy (`AiAssistMergePolicy`)**: Pure domain function merging backend DTO sections into `AnalysisResult`. Invariants strictly enforced:
  1. Local items are never removed.
  2. Local allergen / avoid warnings are never dropped.
  3. Normalized duplicates are deduplicated.
  4. Merged cloud items are tagged with `isAiGenerated = true` and show a distinct `AI` chip.
  5. Warning severity moves only upward (never downgrade).
  6. Source attribution line rendered: `"Trợ lý AI - Tier <tierUsed> - <model>"` (e.g. `Trợ lý AI - Tier 2 - @cf/mistralai/mistral-small-3.1-24b-instruct`).
- **Profile Toggle**: Added `aiAssistEnabled: Boolean = true` to `UserProfile`, `UserProfileRepository`, and `LabelLensProfileViewModel`. Exposed in `ProfileScreen` with full Vietnamese / English localized title and subtitle.

---

## 2. Quality Gates & Test Verification

All quality gates passed cleanly with zero regressions:

1. **Spotless & Ktlint**:
   - Command: `./gradlew spotlessCheck`
   - Result: `BUILD SUCCESSFUL` (all formatting rules conformant).
2. **JVM Unit Tests**:
   - Command: `./gradlew :shared:jvmTest`
   - Result: `BUILD SUCCESSFUL`
   - XML Results (`MobileApp/shared/build/test-results/jvmTest/`):
     - **38 suites**, **255 tests**, **0 failures**, **0 errors**, **0 skipped**.
3. **Android Host Tests (Robolectric)**:
   - Command: `./gradlew :shared:testAndroidHostTest`
   - Result: `BUILD SUCCESSFUL`
   - XML Results (`MobileApp/shared/build/test-results/testAndroidHostTest/`):
     - **32 suites**, **240 tests**, **0 failures**, **0 errors**, **0 skipped**.
4. **Total Test Suite**:
   - **70 test suites**, **495 tests**, **0 failures**, **0 errors**, **0 skipped**.
   - Net addition: **+32 new unit tests** covering trigger policy, merge policy, backend service DTO parsing, HTTP error states, and profile/result viewmodels (baseline was 463 tests).
5. **Android Debug Build**:
   - Command: `./gradlew :androidApp:assembleDebug`
   - Result: `BUILD SUCCESSFUL`
   - APK output: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk` (94 MB).

---

## 3. Live Emulator Verification & Proof Screenshots

Verification was executed on own emulator instance `emulator-5556` (`sdk_gphone16k_arm64`, port 5556, Pixel_4_2). `topResumedActivity=com.labellens.app/.AppActivity` was asserted before every interaction.

All 4 proof screenshots are committed at `Docs/qa-v2/scan-ai-camera/`:

1. `01_camera_viewfinder.png` (1.3 MB):
   - Shows the live CameraX viewfinder rendering the 3D scene from the emulator camera.
   - Overlaid with the green guide frame (`RoundedCornerShape(20.dp)`), guidance pill ("Căn chỉnh nhãn trong khung hình"), top-left close button, top-right torch button, and bottom camera shutter button.
2. `02_camera_result.png` (228 KB):
   - Captured from CameraX shutter button tap.
   - Shows OCR + LabelAnalyzer execution completing and arriving on `ResultScreen`.
   - Displays detected product ("Oat Crunch Cereal"), category ("Ngũ cốc"), ingredients, and nutrition sections.
3. `03_result_ai_assisted.png` (257 KB):
   - Result screen with AI assist triggered via live Cloudflare backend.
   - Shows source attribution line: `Trợ lý AI - Tier 2 - @cf/mistralai/mistral-small-3.1-24b-instruct`.
   - Displays merged AI sections with the purple `AI` tag chip visible on each cloud-enhanced item ("Contains artificial colour yellow 5.", "Contains sodium benzoate, a preservative.").
4. `04_profile_ai_toggle.png` (279 KB):
   - ProfileScreen displaying the new AI Assist setting item.
   - Title: "Trợ lý AI"
   - Subtitle: "Hỗ trợ phân tích khi nhãn mờ, khó đọc hoặc thông tin chưa đầy đủ"
   - Switch state: `checked="true"` (ON).

---

## 4. Key Security & Environment Status

- `LABELLENS_APP_KEY`: Successfully extracted from `LabelLensBackend/.dev.vars` (64 characters) into `MobileApp/local.properties` (gitignored).
- Built-in Fallback: Gradle `buildConfigField` injects empty string when no key is present; `DefaultLabelLensBackendAnalyzeService` verifies `appKey.isNotBlank()` before making any network request and silently falls back to local rules engine.
- No secrets or credentials were logged, printed to stdout, or committed to git.

---

## 5. Artifacts & Follow-ups

- Code changes committed to `feature/scan-ai-camera`.
- No merge or push performed per instructions.
