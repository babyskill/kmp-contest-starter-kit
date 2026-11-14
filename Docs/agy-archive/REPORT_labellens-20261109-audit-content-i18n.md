# Content & Localization Audit Report (`labellens-20261109-audit-content-i18n`)

- **Task ID:** `labellens-20261109-audit-content-i18n`
- **Requester:** Muse
- **Branch:** `feature/audit-content-i18n`
- **Base Branch:** `feature/ui-audit-fix2` (`69a4b14`)
- **Commit:** `77ef831`
- **Status:** Complete & Verified
- **Finished At:** 2026-11-09T13:03:09+07:00

---

## 1. Overview & Scope

This lane resolves content, naming, and string localization defects D7 through D11 identified by the independent full-screen audit of LabelLens (KMP Android). All changes are strictly within content/naming/i18n scope:
- No layout redesigns or visual restructuring.
- No changes to analyzer scoring, classification algorithms, or safety rule logic.
- All new/updated strings exist symmetrically in both English (`values/strings.xml`) and Vietnamese (`values-vi/strings.xml`).
- Stored scan history in the SQLite database is strictly preserved without running any destructive migration or deletion.

---

## 2. Changes per Defect

### D7. Product-Name Fallback Sanitization
- **Problem:** When a label scan did not detect a clean brand or product header, the fallback logic took raw OCR lines verbatim. This resulted in History, Home, Result, and Compare screens displaying raw table headers or sentence fragments such as `"Thành phần: Sữa tươi tiệt trùng 100%."`, `"Ingredients: Enriched Wheat Flour (Wheat"`, or `"Supplement Facts Serving Size: 1 Softgel."`.
- **Fix:**
  - Introduced `ProductNameSanitizer` in `domain/analyzer/ProductNameSanitizer.kt`.
  - Added `looksLikeProductName(candidate: String?): Boolean` rule enforcing:
    1. Not blank, length between 2 and 60 characters.
    2. Word count $\le 6$ words.
    3. Case- and diacritic-insensitive rejection of common non-product prefixes: `ingredients`, `thành phần`, `thanh phan`, `supplement facts`, `nutrition facts`, `giá trị dinh dưỡng`, `gia tri dinh duong`, `hướng dẫn sử dụng`, `huong dan su dung`, `chỉ tiêu chất lượng`, `chi tieu chat luong`.
    4. Rejection of unclosed opening punctuation brackets `(`, `[`, `{` indicating truncation mid-word/mid-clause.
    5. Rejection of lines ending with sentence-continuation punctuation (`:`, `,`, `-`, `/`, `...`, `…`) or percentage indicators (`100%`, `50%`, `8%`).
  - Implemented `displayProductName(candidate: String?, language: String): String`, returning the sanitized candidate or localized fallback `"Chưa đặt tên"` (VI) / `"Untitled label"` (EN).
  - Applied `ProductNameSanitizer` in:
    - `ScanViewModel`: selects candidate names from top OCR lines using `looksLikeProductName()`.
    - `ResultViewModel` & `ResultScreen`: displays sanitized name or localized untitled fallback.
    - `HistoryScreen`: renders stored scan names sanitized at display time.
    - `HomeScreen`: displays sanitized names in "Lần quét gần đây" / "Recent Scans".
    - `CompareScreen`: displays sanitized names in product selectors and candidate cards.
    - `ScanRepository`: sanitizes product names during reload and re-analysis.
  - Added unit test `ProductNameSanitizerTest.kt` verifying all audit examples, prefix rejections, word limit bounds, and punctuation boundaries.

### D8. Ingredient Detail Fallbacks & Caution Section Guard
- **Problem:** For ingredients not present in the local knowledge base, the "Định nghĩa" (What it is) and "Ai nên hạn chế" (Who should limit) sections displayed the identical generic reassuring sentence.
- **Fix:**
  - In `values/strings.xml` and `values-vi/strings.xml`, added distinct definition fallback:
    - EN: `"No detailed definition available for this ingredient in the local database."`
    - VI: `"Chưa có thông tin định nghĩa cho thành phần này trong cơ sở dữ liệu."`
  - In `IngredientDetailScreen.kt`, bound the "What it is" section to this dedicated definition fallback text.
  - Guarded the "Who may want to avoid it" section with `if (ingredient != null)`: when an ingredient is unknown (no caution or allergen data), the section is completely omitted rather than displaying a misleading generic reassurance.

### D9. Onboarding Page 2 Backslash-Escaped Quotes Removal
- **Problem:** Onboarding page 2 body text rendered literal backslashes around quotes (`\'độc hại\'` / `\'toxic\'`) because Compose Multiplatform XML string resources do not require escaping single apostrophes within XML tag values.
- **Fix:**
  - In `values/strings.xml` and `values-vi/strings.xml`, replaced `\'` with plain unescaped single quotes `'` in `onboarding_desc_2`.
  - Also cleaned remaining occurrences across `home_tip_body` and `result_exception_btn` to prevent backslash leaking.

### D10. Dynamic Scan Summary Localization at Display Time
- **Problem:** Scan summaries stored in SQLite retained their scan-time language. After switching the app language (e.g. from Vietnamese to English), History entries and Home "Recent Scans" continued showing Vietnamese summary subtitles.
- **Fix:**
  - Created `ScanSummaryLocalizer` in `domain/analyzer/ScanSummaryLocalizer.kt`.
  - Implemented `localizedSummary(result: LabelAnalysisResult, language: String): String` which dynamically re-derives the localized summary in the active app language based on:
    1. Personal alerts: returns `"Review the personal flags above..."` (EN) / `"Vui lòng xem lại các lưu ý cá nhân..."` (VI).
    2. High-sugar/sodium attention flags: returns `"Flagged for high sodium..."` (EN) / `"Có cảnh báo về natri..."` (VI).
    3. Category and verdict patterns: returns standard localized statements for whole food, packaged food, dietary supplement, and personal care formulations.
    4. If the stored summary cannot be re-derived, falls back cleanly to a generic localized subtitle (`summary_standard_formulation` in the current language) instead of leaking the opposite language's text.
  - Applied `ScanSummaryLocalizer` dynamically in `HistoryScreen`, `HomeScreen`, `CompareScreen`, and `ResultScreen`.
  - Retained all existing SQLite database history records untouched without destructive migrations.

### D11. Compare Screen Nutrition Row Localization
- **Problem:** Nutrition comparison table row labels remained hardcoded in English in Vietnamese mode (`"Calories"`, `"Sugars"`, `"Sodium"`, `"Saturated Fat"`, `"Protein"`, `"Dietary Fiber"`).
- **Fix:**
  - Added localized nutrient string resources in `values/strings.xml` and `values-vi/strings.xml`:
    - `compare_row_calories`: `"Calories"` / `"Năng lượng"`
    - `compare_row_sugars`: `"Sugars"` / `"Đường"`
    - `compare_row_sodium`: `"Sodium"` / `"Natri"`
    - `compare_row_saturated_fat`: `"Saturated Fat"` / `"Chất béo bão hòa"`
    - `compare_row_protein`: `"Protein"` / `"Chất đạm"`
    - `compare_row_dietary_fiber`: `"Dietary Fiber"` / `"Chất xơ"`
  - Updated `CompareScreen.kt` to map raw metric keys (`"Calories"`, `"Sugars"`, etc.) to their localized `stringResource()` equivalents.

---

## 3. Test & Verification Evidence

### Exact Test Counts (XML Test Reports)
All tests executed from `MobileApp/` using JDK 21:
- **`jvmTest`**:
  - Test suites: **39**
  - Total tests: **259**
  - Failures: **0**
  - Errors: **0**
  - Skipped: **0**
- **`testAndroidHostTest`**:
  - Test suites: **34**
  - Total tests: **246**
  - Failures: **0**
  - Errors: **0**
  - Skipped: **0**
- **Lint & Code Style**:
  - `spotlessCheck`: **PASSED** (all 63 tasks up-to-date)
- **Compilation**:
  - `:androidApp:assembleDebug`: **BUILD SUCCESSFUL**

### Emulator Screenshots (`Docs/qa-v2/audit-content-i18n/`)
Verified on `emulator-5560` (Pixel 9a, Android API 36, font scale 1.0):
1. `Docs/qa-v2/audit-content-i18n/01_history_names.png`:
   - History in VI mode showing fallback `"Chưa đặt tên"` for OCR fragment-named entries while preserving genuine product names (`"Honey Oat Cereal"`, `"Clony"`).
2. `Docs/qa-v2/audit-content-i18n/02_history_en.png`:
   - History in EN mode showing `"Untitled label"` for fragments and dynamically localized English summaries (`"Wholesome formulation centered on straightforward whole ingredients..."`, `"Convenient formulated product with balanced taste..."`).
3. `Docs/qa-v2/audit-content-i18n/03_compare_vi.png`:
   - Compare table in VI mode displaying localized nutrient rows (`"Năng lượng"`, `"Đường"`, `"Natri"`, `"Chất béo bão hòa"`, `"Chất đạm"`, `"Chất xơ"`) and Vietnamese summary verdicts.
4. `Docs/qa-v2/audit-content-i18n/04_detail_unknown.png`:
   - Ingredient detail for unknown ingredient (`Sodium Lauryl Sulfate`): displays dedicated definition fallback (`"Chưa có thông tin định nghĩa cho thành phần này trong cơ sở dữ liệu."`) and completely hides the `"Ai nên hạn chế"` caution section.
5. `Docs/qa-v2/audit-content-i18n/05_onboarding_p2.png`:
   - Onboarding page 2 showing clean single quotes (`'độc hại'`) with zero backslashes.
