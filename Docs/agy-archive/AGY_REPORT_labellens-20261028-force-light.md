# LabelLens Fix Lane Report: Force Light Theme App-Wide

- **Task ID**: `labellens-20261028-force-light`
- **Requested By**: Muse
- **Target Branch**: `feature/v2-proof`
- **Commit**: `19a00be8dfb174d84407bc0d904a17aeb0bfa316` (short: `19a00be`)
- **Status**: COMPLETE
- **Report Date**: 2026-10-29

---

## 1. Problem & Root Cause

When a device had system dark mode turned on, LabelLens rendered an inconsistent appearance:
1. `Theme.kt` defaulted `isDarkMode` to `isSystemInDarkTheme()`, which activated `darkModeAppColors` when system dark mode was on.
2. Under `darkModeAppColors`, `AppColors.Text.DefaultDark` set `text.primary` to `Color.White`.
3. However, components and card containers across the app (such as `AppCardContainer`, result cards, sample cards) used white surface backgrounds (`#FFFFFF`) or explicit light colors, causing white text on white cards and broken visual contrast.
4. Android system configuration:
   - `AndroidManifest.xml` had `android:theme="@android:style/Theme.Material.NoActionBar"`, which is a dark base theme in the Android framework.
   - `styles.xml` used `parent="@android:style/Theme.Material.NoActionBar"` instead of the light variant.
   - `res/values-night/colors.xml` specified `windowBackgroundColor` as `#12101A`.
   - `enableEdgeToEdge()` in `AppActivity` defaulted to `SystemBarStyle.auto`, rendering light icons for a dark status bar when night mode was active.
   - `Theme.ios.kt` was passing `UIStatusBarStyleLightContent` on light backgrounds.

Per DESIGN.md, LabelLens is an intentionally light-first application (light palette: background `#F7F8F5`, surface `#FFFFFF`, heading `#14201A`, body `#3D4A42`, primary `#16794C`). The fix lane forces the light theme app-wide regardless of system dark mode settings.

---

## 2. Changes Made

### 1. Compose Theme (`MobileApp/designsystem/src/commonMain/...`)
- **`Theme.kt`**:
  - Removed import and usage of `androidx.compose.foundation.isSystemInDarkTheme`.
  - Set `LocalThemeIsDark = compositionLocalOf { false }`.
  - In `AppTheme`, ignored any incoming `isDarkMode` flag, always providing:
    - `LocalThemeIsDark provides false`
    - `LocalAppColors provides lightModeAppColors`
    - `SystemAppearance(isDark = false)`
    - `colorScheme = LocalAppColors.current.asMaterialColorScheme(isDark = false)`
  - Runtime selection of `darkModeAppColors` is completely disabled.
- **`Color.kt`**:
  - Removed `darkColorScheme` import.
  - Modified `asMaterialColorScheme` to unconditionally return `lightColorScheme` configured with the light palette tokens.

### 2. Android Platform Integration
- **`styles.xml` (`MobileApp/androidApp/src/main/res/values/styles.xml`)**:
  - Changed parent to `@android:style/Theme.Material.Light.NoActionBar`.
  - Added explicit `<item name="android:windowLightStatusBar">true</item>` and `<item name="android:windowLightNavigationBar">true</item>`.
- **`AndroidManifest.xml` (`MobileApp/androidApp/src/main/AndroidManifest.xml`)**:
  - Set `android:theme="@style/AppTheme"` on `<application>` (replacing the hardcoded dark Material theme).
- **`values/colors.xml` & `values-night/colors.xml`**:
  - Normalized `primary` to `#16794C` (from starter-kit `#7F5FFF`).
  - Normalized `windowBackgroundColor` to `#F7F8F5` in both light and night qualifiers to prevent dark window flashes or resource resolution under system dark mode.
- **`Theme.android.kt` (`MobileApp/designsystem/src/androidMain/...`)**:
  - Enforced `isAppearanceLightStatusBars = true` and `isAppearanceLightNavigationBars = true` via `WindowInsetsControllerCompat`.
  - Pinned `navigationBarColor` and `statusBarColor` to `backgroundColor.toArgb()` (`#F7F8F5`).
- **`App.android.kt` (`MobileApp/androidApp/src/main/kotlin/...`)**:
  - Configured `enableEdgeToEdge()` with `SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)` for both `statusBarStyle` and `navigationBarStyle`.
- **`App.kt` (`MobileApp/shared/src/commonMain/...`)**:
  - Explicitly specified `containerColor = AppTheme.colors.background` and `contentColor = AppTheme.colors.text.primary` on root `Scaffold`.

### 3. iOS Platform Integration
- **`Info.plist` (`MobileApp/iosApp/iosApp/Info.plist`)**:
  - Added `<key>UIUserInterfaceStyle</key><string>Light</string>` to force iOS UI appearance to Light mode app-wide.
- **`iosApp.swift` (`MobileApp/iosApp/iosApp/iosApp.swift`)**:
  - Added `.preferredColorScheme(.light)` to `WindowGroup` content view.
- **`Theme.ios.kt` (`MobileApp/designsystem/src/iosMain/...`)**:
  - Set `UIApplication.sharedApplication.setStatusBarStyle(UIStatusBarStyleDarkContent)` so status bar icons remain dark against the light background.

---

## 3. Files Changed

1. `MobileApp/designsystem/src/commonMain/kotlin/com/labellens/app/designsystem/theme/Theme.kt`
2. `MobileApp/designsystem/src/commonMain/kotlin/com/labellens/app/designsystem/theme/Color.kt`
3. `MobileApp/designsystem/src/androidMain/kotlin/com/labellens/app/designsystem/theme/Theme.android.kt`
4. `MobileApp/designsystem/src/iosMain/kotlin/com/labellens/app/designsystem/theme/Theme.ios.kt`
5. `MobileApp/androidApp/src/main/res/values/styles.xml`
6. `MobileApp/androidApp/src/main/res/values/colors.xml`
7. `MobileApp/androidApp/src/main/res/values-night/colors.xml`
8. `MobileApp/androidApp/src/main/AndroidManifest.xml`
9. `MobileApp/androidApp/src/main/kotlin/com/labellens/app/App.android.kt`
10. `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/root/App.kt`
11. `MobileApp/iosApp/iosApp/Info.plist`
12. `MobileApp/iosApp/iosApp/iosApp.swift`
13. `Docs/qa-v2/light-forced/01_home_nightmode.png`
14. `Docs/qa-v2/light-forced/02_result_nightmode.png`

---

## 4. Verification Results

### 1. Test Suites (XML Results)
- Command: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
- **`jvmTest`**:
  - Test suites: **27**
  - Total tests: **160**
  - Failures: **0**
  - Errors: **0**
  - Skipped: **0**
- **`testAndroidHostTest`**:
  - Test suites: **23**
  - Total tests: **148**
  - Failures: **0**
  - Errors: **0**
  - Skipped: **0**
- Combined totals: **50 suites, 308 tests, 0 failures**.

### 2. Spotless Formatting Check
- Command: `./gradlew spotlessCheck`
- Result: **BUILD SUCCESSFUL** (63 actionable tasks up-to-date, 0 violations).

### 3. Android Debug Build & APK
- Command: `./gradlew :androidApp:assembleDebug`
- Build time: **10s** (total execution time: 11.133s)
- Output APK Path: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- Exact Byte Size: **96,581,230** bytes (`96581230` bytes)

### 4. iOS Simulator ARM64 Compilation
- Command: `./gradlew compileKotlinIosSimulatorArm64`
- Result: **BUILD SUCCESSFUL** in 38s (61 actionable tasks).

### 5. Emulator Execution with Night Mode Forced
- Device: `emulator-5554` (Pixel_9a, Android API 35)
- Set to Night mode: `adb shell cmd uimode night yes` (Output: `Night mode: yes`)
- Launched app: `adb shell am start -n com.labellens.app/.AppActivity`
- Verified: Both the Home screen and the Result screen render with the complete light appearance (dark text `#14201A` on `#F7F8F5` light background with white `#FFFFFF` cards, green `#16794C` primary elements, and dark status/nav icons).
- Screenshots captured:
  - `Docs/qa-v2/light-forced/01_home_nightmode.png` (212,652 bytes)
  - `Docs/qa-v2/light-forced/02_result_nightmode.png` (253,107 bytes)
- Restored mode: `adb shell cmd uimode night auto` (Output: `Night mode: auto`)

---

## 5. Items Skipped & Notes

- **AppCompatDelegate**: `androidx.appcompat` is not part of the project dependencies (the app uses pure `ComponentActivity` + Compose Multiplatform). Status/navigation bars, window background, and edge-to-edge styling are directly pinned via `SystemBarStyle.light`, `WindowInsetsControllerCompat`, and `@android:style/Theme.Material.Light.NoActionBar`.
