# Report: LabelLens i18n In-App Locale Resolution & String Audit

**Lane**: `labellens-20261031-i18n-locale-fix`  
**Date**: 2026-10-31  
**Branch**: `feature/v2-proof`

---

## 1. Problem & Root Cause

Previously, `App.kt` collected `languageFlow` and wrapped UI in `key(currentLanguage)`. However, Compose Multiplatform 1.11.1 (`components-resources`) resolves strings via `LocalComposeEnvironment` / `ResourceEnvironment`. Because no custom `ResourceEnvironment` with a language qualifier was provided at the composition root, `stringResource()` calls defaulted to the system environment (system locale). On an English-locale phone (`en-US`), `values-vi/strings.xml` was never resolved, leaving ~90% of the UI in English even when the in-app language was set to Vietnamese.

---

## 2. In-App Locale Resolution Mechanism

### Compose Multiplatform 1.11.1 Mechanism
In Compose Multiplatform 1.11.1 (`org.jetbrains.compose.components:components-resources`), `LocalResourceEnvironment` is not present; instead, Compose Resources uses `LocalComposeEnvironment: ProvidableCompositionLocal<ComposeEnvironment>`.

In `App.kt`:
```kotlin
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment

// Under App() root:
val currentLanguage by viewModel.currentLanguage.collectAsState()
val systemComposeEnvironment = LocalComposeEnvironment.current
val customComposeEnvironment = remember(systemComposeEnvironment, currentLanguage) {
    object : ComposeEnvironment {
        @Composable
        override fun rememberEnvironment(): ResourceEnvironment {
            val base = systemComposeEnvironment.rememberEnvironment()
            return remember(base, currentLanguage) {
                ResourceEnvironment(
                    language = LanguageQualifier(currentLanguage.code),
                    region = RegionQualifier(""),
                    theme = base.theme,
                    density = base.density,
                )
            }
        }
    }
}

CompositionLocalProvider(
    LocalComposeEnvironment provides customComposeEnvironment,
) {
    // App content & navigation
}
```

### Android Platform Locale Synchronization
In `MobileApp/shared/src/androidMain/kotlin/com/labellens/app/util/PlatformLanguage.android.kt`, `setPlatformLanguage` was updated to also update `android.os.LocaleList.setDefault(LocaleList(locale))` and `resources.configuration.setLocales(LocaleList(locale))` on Android API 24+, ensuring both Android platform resources and Compose Resources resolve synchronously to the selected in-app language.

---

## 3. Hardcoded String Audit & Migration Parity

We audited `shared/src/commonMain/kotlin/com/labellens/app/presentation/` and `designsystem/` for literal `Text("...")` and `contentDescription = "..."` English strings.

### Audit Summary
- **Total Hardcoded UI Strings Identified & Migrated**: 79 strings
  - `HomeScreen.kt`: 16 strings (tagline, hero title & subtitle, quick actions, sample section headers, calm tip, recent scans headers & content descriptions).
  - `ScanScreen.kt`: 12 strings (analyzing banner, tips, barcode lookup, paste text header & instructions, sample cards).
  - `HistoryScreen.kt`: 13 strings (header title, subtitle pattern, safety filters, delete dialog & descriptions).
  - `ProfileScreen.kt`: 4 strings (Plus upgrade banner title, subtitle, CTA button).
  - `LabelLensPaywallScreen.kt`: 9 strings (pricing cards, demo badge, restore purchases, terms/privacy links, feature rows).
  - `LabelLensOnBoardingScreen.kt`: 11 strings (step titles, body copy, badge labels, skip/next/get started buttons).
  - `DeepAssessmentOnBoardingScreen.kt`: 1 string (back icon content description).
  - `ExitIntentDownsellDialog.kt`: 1 string (close content description).
  - `designsystem` components: 12 strings across `AppToolbar.kt`, `AppModalBottomSheet.kt`, `AppDialog.kt`, `FilterDialog.kt`, `CircleButtonWithSteps.kt`, and `AddOrChooseFileContainer.kt` (`btn_back`, `btn_next`, `content_desc_close`, `content_desc_selected`, `content_desc_delete`, `content_desc_add`).
- **Resource Key Parity**: 100% parity maintained between `values/strings.xml` and `values-vi/strings.xml` across both `:shared` and `:designsystem`.
- **Exemptions**: Brand name `"LabelLens"`, sample product names, technical log strings, and the existing `AnalysisStrings` engine were preserved unchanged.

---

## 4. Verification & Quality Gates

### Automated Test Results (from XML test suites)
- **JVM Tests (`:shared:jvmTest`)**:
  - Suites: 27
  - Total Tests: 163
  - Failures: 0, Errors: 0, Skipped: 0
- **Android Host Tests (`:shared:testAndroidHostTest`)**:
  - Suites: 23
  - Total Tests: 151
  - Failures: 0, Errors: 0, Skipped: 0
- **Total Suite Counts**: 50 test suites, 314 tests passed, 0 failures.

### Lint & Build Gates
- `./gradlew spotlessCheck`: CLEAN (all modules up-to-date)
- `./gradlew :androidApp:assembleDebug`: SUCCESS (APK: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`, 92MB)

### Headless Emulator Verification (Pixel_4_2, system locale `en-US`)
1. Booted headless emulator `Pixel_4_2` (`ro.product.locale=en-US`).
2. Installed `androidApp-debug.apk`.
3. Verified onboarding, home, settings, and result screen in Vietnamese:
   - `01_home_vi.png`: Home screen with Vietnamese navigation bar ("Trang chủ", "Quét", "Lịch sử", "So sánh", "Hồ sơ"), hero card ("Quét nhãn bất kỳ"), quick actions, sample carousel ("Thử mẫu có sẵn").
   - `02_settings_vi.png`: Settings screen with Vietnamese options ("Ngôn ngữ ứng dụng", "Tiếng Việt", "Quyền riêng tư 100% trên thiết bị", "Hỗ trợ nhãn đa ngôn ngữ châu Á").
   - `03_result_vi.png`: Result screen for Honey Oat Cereal sample with Vietnamese headers ("Phân tích nhãn", "8 thành phần đã phân tích", "Hồ sơ của bạn: không có cảnh báo nào", "Đáng lưu ý").
4. Switched language to English via Settings screen:
   - Verified immediate dynamic switch to English.
   - `04_home_en.png`: Home screen rendered in English ("Home", "Scan", "History", "Compare", "Profile", "Scan Any Label", "Quick Actions", "Try Built-in Samples").
5. Saved screenshots to `Docs/qa-v2/i18n-fix/`:
   - `Docs/qa-v2/i18n-fix/01_home_vi.png`
   - `Docs/qa-v2/i18n-fix/02_settings_vi.png`
   - `Docs/qa-v2/i18n-fix/03_result_vi.png`
   - `Docs/qa-v2/i18n-fix/04_home_en.png`
