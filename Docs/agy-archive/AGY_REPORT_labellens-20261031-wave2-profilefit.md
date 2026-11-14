# AGY_REPORT_labellens-20261031-wave2-profilefit.md
**Lane:** `labellens-20261031-wave2-profilefit` (RESUME)  
**Branch:** `feature/v3-wave2` (branched from `feature/v3-wave1` at `d6ba222`)  
**Date:** 2026-10-31  

---

## 1. Overview & Resume Context

Wave 2 implements PRD V3 Section 4: Per-Market Profile-Fit. All logic operates entirely on-device (Tier 0 deterministic, zero network calls, zero backend changes).

This session was a **continuation / RESUME** of the previous session which stalled due to hitting quota limits during emulator verification.
- **State at session pickup**: The implementation code (`MarketPack.kt`, `ProfileFitEvaluator.kt`, `LabelAnalyzer.kt`, `ResultScreen.kt`, `ProfileScreen.kt`, `Wave2ProfileFitTest.kt`, `StringParityTest.kt`, and resources) was completely authored in the working tree. Screenshots `01_header_contains_en.png`, `02_header_vi.png`, and `04_profile_packs_en.png` had been captured in `Docs/qa-v2/wave2/`.
- **Work completed in this session**:
  1. Complete validation of tree state and verification gates against PRD V3 requirements and standing invariants.
  2. Executed `spotlessCheck` (clean, 0 violations).
  3. Executed scoped test suites `:shared:jvmTest` and `:shared:testAndroidHostTest` and extracted exact XML counts.
  4. Executed `:androidApp:assembleDebug` (build successful, debug APK built).
  5. Booted the Android emulator (`emulator-5554`, `Pixel_4_2`), installed the newly assembled debug APK, navigated to the Scan tab, entered cut-off text (`Ingredients: Whole grain oats, sugar,`), and captured the missing proof screenshot `03_header_not_enough_info_en.png`.
  6. Visually inspected ("eyeballed") all 4 proof screenshots in `Docs/qa-v2/wave2/`.
  7. Authored completion report and `AGY_DONE_labellens-20261031-wave2-profilefit.md`.

---

## 2. Implemented Features Review

### 1. Market Allergen Packs (`MarketPack.kt`, `ProfileFitEvaluator.kt`)
- **US Pack**: Exactly 9 FALCPA/FASTER categories (Milk, Egg, Fish, Crustacean Shellfish, Tree Nuts, Peanut, Wheat, Soy, Sesame).
- **EU/UK Pack**: Exactly 14 categories (Cereals containing gluten, Crustaceans, Eggs, Fish, Peanuts, Soybeans, Milk, Tree Nuts, Celery, Mustard, Sesame, Sulphites, Lupin, Molluscs).
- **AU/NZ Pack**: Exactly 12 FSANZ Standard 1.2.3 categories (Celery and Mustard verified absent; Cereals containing gluten, Crustaceans, Egg, Fish, Milk, Peanut, Sesame, Soy, Tree Nuts, Lupin, Molluscs, Sulphites present).
- **JP & KR**: Enum entries exist as placeholders with no pack data attached.
- **Imported Food Mode**: Toggle runs allergen checks against the union of US + EU/UK + AU/NZ category sets (14 distinct categories).
- **Custom Allergens**: Free-text entries supported with token-boundary matching.
- **False-Positive Whitelist**: Unit-tested protection ensuring `nutmeg` != tree nut, `cocoa butter` != milk, `lactic acid` != milk, `cream of tartar` != milk.

### 2. Six-State Deterministic Result Header (`FitHeaderInfo`, `ProfileFitState`)
- Priority: `CONTAINS_YOURS` (6) > `CANNOT_RULE_OUT` (5) > `NOT_ENOUGH_INFO` (4) > `DEPENDS_ON_PORTION` (3) > `WORTH_NOTING` (2) > `NO_MATCHES` (1).
- Distinct icon shapes and text labels for color-blind accessibility; red color strictly reserved for `CONTAINS_YOURS`.
- `CONTAINS_YOURS`: Direct match against user's declared profile allergens.
- `CANNOT_RULE_OUT`: Precautionary statements ("may contain...", "made in a facility...") or catch-all terms ("natural flavor", "spices", "proprietary blend") matching potential allergens.
- `WORTH_NOTING` & `DEPENDS_ON_PORTION`: Driven by enabled Attention Flags v1 thresholds (per-serving vs whole-pack portion scaling).
- `NOT_ENOUGH_INFO`: Cut-off detection heuristics.
- `NO_MATCHES`: Clean profile-fit with full label capture.

### 3. Attention Flags v1
- Defined named constants: `SODIUM_HIGH_THRESHOLD_MG` = 460.0 (20% DV), `ADDED_SUGARS_HIGH_THRESHOLD_G` = 10.0 (20% DV).
- Non-nutritive sweeteners list (sugar alcohols excluded).
- Caffeine sources and artificial colours lists.

### 4. Never Silent-Pass (Cut-off Detection)
- Heuristics: trailing comma, trailing conjunctions ("and", "or"), unclosed open parenthesis, or missing ingredient list triggers `NOT_ENOUGH_INFO`.

### 5. Correction Loop
- Verbatim source line shown for every match.
- "Misread label? Fix" dialog allows immediate OCR/text editing and instant re-analysis.
- "I'm fine with this ingredient" saves personal exceptions to suppress attention flags, while strictly never suppressing a `CONTAINS_YOURS` allergen match.

### 6. Internationalization (i18n)
- Full parity between `values/strings.xml` and `values-vi/strings.xml`, verified by `StringParityTest`.
- Dynamic analysis strings translated via `AnalysisStrings.kt` with zero English leakage in Vietnamese mode.

---

## 3. Test & Quality Gate Results

Exact test counts parsed directly from Gradle XML test result files (`MobileApp/shared/build/test-results/`):

### 1. Spotless / Lint Check
- Command: `cd MobileApp && ./gradlew spotlessCheck`
- Status: **PASSED** (0 violations, clean).

### 2. Unit + Host Tests
- Command: `cd MobileApp && ./gradlew :shared:jvmTest :shared:testAndroidHostTest`
- **`:shared:jvmTest`**:
  - Suites: 32
  - Tests: 211
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **`:shared:testAndroidHostTest`**:
  - Suites: 26
  - Tests: 196
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **Total Combined Tests**: 58 suites, 407 tests, 0 failures, 0 errors, 0 skipped.
  *(Baseline was 354 tests; Wave 2 added 53 tests).*

### 3. Android Debug Build
- Command: `cd MobileApp && ./gradlew :androidApp:assembleDebug`
- Status: **BUILD SUCCESSFUL** (APK: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`).

---

## 4. Screenshot Evidence (`Docs/qa-v2/wave2/`)

All 4 screenshots visually inspected and verified:
1. `Docs/qa-v2/wave2/01_header_contains_en.png`: English result screen showing `CONTAINS YOURS` red header with octagon icon, verbatim source line citation, declared allergen identification, and "Misread label? Fix" action.
2. `Docs/qa-v2/wave2/02_header_vi.png`: Vietnamese result screen showing fully translated `CONTAINS YOURS` header ("Chứa chất gây dị ứng của bạn", "Trùng khớp trực tiếp...", "Nhận diện sai? Sửa lại", "Dòng gốc trên nhãn...") with zero English leakage.
3. `Docs/qa-v2/wave2/03_header_not_enough_info_en.png`: English result screen for truncated ingredient list (`Ingredients: Whole grain oats, sugar,`) displaying `NOT ENOUGH INFO` blue header with circle icon and explanation note that part of the ingredient list was cut off.
4. `Docs/qa-v2/wave2/04_profile_packs_en.png`: Profile screen showing the Market Allergen Standard selector (US 9, EU/UK 14, AU/NZ 12), the Imported Food Mode toggle, and the My Allergies selection chips.

---

## 5. Deviations & Notes
- Zero architectural deviations from PRD V3 Section 4.
- All non-negotiable invariants preserved.
- Untracked artifacts from previous lanes left untouched.
