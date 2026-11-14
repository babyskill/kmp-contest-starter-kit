# AGY Report: Header Localization Leaks Fix (`labellens-20261101-header-i18n`)

- **Task ID:** `labellens-20261101-header-i18n`
- **Branch:** `feature/header-i18n-fix`
- **Commit:** `acf63cfeb2afe49be019236e6be42bef7c54e7fe`
- **Status:** Complete & Verified

---

## 1. Root Cause Analysis: What Produced the Leaked Strings

### A. The "Dau goi" Product-Type Line Leak Under Product Name
- **Origin in Seed Data:** In `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/model/SampleProducts.kt`, `sample8` was registered with:
  ```kotlin
  val sample8 = SampleProduct(
      id = "sample_clony_shampoo",
      name = "Dầu gội Clony",
      ...
  )
  ```
  The category/product-type descriptor ("Dầu gội") was hardcoded directly inside the product name string itself instead of keeping the proper noun ("Clony") separate.
- **Missing Domain Slot in Result Model:** `LabelAnalysisResult` lacked a dedicated, localized `productType` property.
- **Missing OCR Cleanup in Analyzer:** When raw label OCR or user input provided strings like "Dầu gội Clony", "Dau goi Clony", or "Clony Shampoo", `LabelAnalyzer.analyze()` lacked the logic to extract and preserve the proper noun while deriving the semantic product type.

### B. Profile-Fit Badge Raw English Enum Names in Vietnamese Mode
- **Origin in Evaluator:** In `ProfileFitEvaluator.kt`, `evaluate()` returns `FitHeaderInfo(state = state, ...)`. The presentation layer (`ResultScreen.kt`) rendered the badge text by taking `state.name.replace('_', ' ')`, producing raw uppercase enum strings (`"NO MATCHES"`, `"CONTAINS YOURS"`, `"CANNOT RULE OUT"`, etc.) regardless of whether the user was in English or Vietnamese mode.

---

## 2. Changes Implemented

### Domain Layer
1. **`AnalysisModels.kt`**:
   - Added `@Serializable enum class ProductType` with 12 canonical product types: `SHAMPOO`, `CONDITIONER`, `BODY_WASH`, `CEREAL`, `SNACK`, `BISCUITS`, `NOODLES`, `BEVERAGE`, `SUPPLEMENT`, `GRANOLA_BAR`, `FOOD`, `PERSONAL_CARE`.
   - Added `val productType: String? = null` property to `LabelAnalysisResult`.
2. **`MarketPack.kt`**:
   - Added `val badgeLabel: String = ""` to `FitHeaderInfo`.
3. **`AnalysisStrings.kt`**:
   - Added `fitStateBadge(language: AppLanguage, state: ProfileFitState): String` mapping all 6 profile-fit badge states to localized labels in both English and Vietnamese:
     - `CONTAINS_YOURS` -> `"CONTAINS YOURS"` (EN) / `"CHỨA DỊ ỨNG"` (VI)
     - `CANNOT_RULE_OUT` -> `"CANNOT RULE OUT"` (EN) / `"KHÔNG THỂ LOẠI TRỪ"` (VI)
     - `NOT_ENOUGH_INFO` -> `"NOT ENOUGH INFO"` (EN) / `"CHƯA ĐỦ THÔNG TIN"` (VI)
     - `DEPENDS_ON_PORTION` -> `"DEPENDS ON PORTION"` (EN) / `"TÙY KHẨU PHẦN"` (VI)
     - `WORTH_NOTING` -> `"WORTH NOTING"` (EN) / `"ĐÁNG LƯU Ý"` (VI)
     - `NO_MATCHES` -> `"NO MATCHES"` (EN) / `"KHÔNG TRÙNG KHỚP"` (VI)
   - Added `productTypeName(language: AppLanguage, type: ProductType): String` providing localized display strings for all 12 `ProductType` values with full EN/VI parity.
4. **`ProfileFitEvaluator.kt`**:
   - Populated `badgeLabel = AnalysisStrings.fitStateBadge(input.language, state)` across all 6 `FitHeaderInfo` return branches.
5. **`LabelAnalyzer.kt`**:
   - Added `cleanProductName(name: String): String` to strip product-type prefixes/suffixes (e.g. "Dầu gội ", "Dau goi ", " Shampoo", " Cereal", " Snack").
   - Added `deriveProductType(productName: String, rawLabelText: String, category: String): ProductType` to infer semantic product type.
   - In `analyze()`: cleans the product name to preserve proper nouns verbatim ("Clony", "Honey Oat Cereal"), derives `ProductType`, resolves localized display string `AnalysisStrings.productTypeName(language, derivedType)`, and populates both onto `LabelAnalysisResult`.
6. **`SampleProducts.kt`**:
   - Corrected `sample8.name` to `"Clony"` (proper noun verbatim, separating type from brand name).

### Presentation Layer
1. **`ResultScreen.kt`**:
   - Added localized product type line rendering directly beneath `result.productName`:
     ```kotlin
     result.productType?.let { pType ->
         Text(
             text = pType,
             style = AppTheme.typography.bodySmall.copy(
                 fontWeight = FontWeight.SemiBold,
                 color = AppTheme.colors.primary,
             ),
         )
     }
     ```
   - Updated `ProfileFitHeaderCard` to receive `language: AppLanguage` and render localized badge text (`fitHeader.badgeLabel.ifEmpty { AnalysisStrings.fitStateBadge(language, fitHeader.state) }`).

### Tests
1. **`LabelAnalyzerTest.kt`**:
   - Added `testFitStateBadgeLocalizationBothLanguages()` asserting all 6 badge states in English and Vietnamese.
   - Added `testProductTypeLineLocalizationClonyAndFoodCases()` asserting:
     - Proper noun "Clony" preserved verbatim.
     - Product type line localized: "Shampoo" (EN) vs "Dầu gội" (VI).
     - OCR prefix stripping: "Dau goi Clony" -> proper noun "Clony" + localized type.
     - Food sample: "Honey Oat Cereal" -> "Cereal" (EN) vs "Ngũ cốc" (VI).

---

## 3. Swept Header Strings and Their Disposition

| Element / Card | Language / Key | English Value | Vietnamese Value | Disposition / Mechanism |
|---|---|---|---|---|
| Screen App Bar | `result_screen_title` | "Label Analysis" | "Phân tích nhãn" | Verified (Compose `stringResource`) |
| Profile Fit Badge | `ProfileFitState` (6 states) | "NO MATCHES", "CONTAINS YOURS", etc. | "KHÔNG TRÙNG KHỚP", "CHỨA DỊ ỨNG", etc. | Fixed via `AnalysisStrings.fitStateBadge` |
| Profile Fit Fix Button | `result_profile_fit_fix_misread` | "Misread label? Fix" | "Nhận diện sai? Sửa lại" | Verified (Compose `stringResource`) |
| Profile Fit Title | `FitHeaderInfo.title` | "No Profile Matches", etc. | "Không có cảnh báo phù hợp", etc. | Verified (routed via `AnalysisStrings.fitHeaderTitle`) |
| Profile Fit Subtitle | `FitHeaderInfo.subtitle` | "All declared ingredients checked; ..." | "Đã đối chiếu các thành phần; ..." | Verified (routed via `AnalysisStrings.fitHeaderSubtitle`) |
| Profile Fit Reason | `FitHeaderInfo.reasonNotes` | Localized allergen / flag notices | Localized allergen / flag notices | Verified (routed via `AnalysisStrings`) |
| Product Title Card Name | `result.productName` | "Clony", "Honey Oat Cereal" | "Clony", "Honey Oat Cereal" | Verbatim proper noun preserved |
| Product Type Subtitle | `result.productType` | "Shampoo", "Cereal" | "Dầu gội", "Ngũ cốc" | Fixed via `AnalysisStrings.productTypeName` |
| Ingredient Count Line | `result_meta_ingredients_count` + flags | "19 ingredients parsed • No personal flags" | "19 thành phần đã phân tích • Không có cảnh báo cá nhân" | Verified (Compose `stringResource`) |
| Quick Glance Summary | `result.summarySentence` | "Wholesome formulation centered on..." | "Công thức lành mạnh tập trung vào..." | Verified (routed via `AnalysisStrings.summaryVerdictSentence`) |

---

## 4. Remaining Known Leaks
- **None in Result Header:** The entire result header area, including top app bar, profile-fit card, badge pill, title card, product name, product type subtitle, metadata line, and summary verdict card, has 100% localization parity across EN and VI.

---

## 5. Quality Gate Results & Evidence

### Test Execution (Gradle XML Reports)
- **`jvmTest`**: 33 suites, 221 tests, 0 failures, 0 errors, 0 skipped.
  - Report location: `MobileApp/shared/build/test-results/jvmTest/`
- **`testAndroidHostTest`**: 28 suites, 208 tests, 0 failures, 0 errors, 0 skipped.
  - Report location: `MobileApp/shared/build/test-results/testAndroidHostTest/`
- **Combined Suite Total**: 61 suites, 429 tests (exceeding baseline 425), 0 failures.

### Static Analysis & Build
- `spotlessCheck`: PASSED (63 actionable tasks, 0 violations).
- `assembleDebug`: BUILD SUCCESSFUL.

### Emulator Proof Screenshots
Saved under `Docs/qa-v2/header-i18n/`:
1. `01_clony_header_en.png`: Clony sample, English mode:
   - Product name: "Clony" (verbatim proper noun)
   - Product type subtitle: "Shampoo" (emerald green text)
   - Badge label: "NO MATCHES"
2. `02_clony_header_vi.png`: Clony sample, Vietnamese mode:
   - Product name: "Clony" (verbatim proper noun)
   - Product type subtitle: "Dầu gội"
   - Badge label: "KHÔNG TRÙNG KHỚP"
3. `03_food_header_en.png`: Honey Oat Cereal sample, English mode:
   - Product name: "Honey Oat Cereal" (verbatim proper noun)
   - Product type subtitle: "Cereal"
   - Badge label: "NO MATCHES"
