# AGY REPORT — LabelLens Wave 2.1: Ingredient-Section Segmentation Fix

- **Task**: `labellens-20261031-wave21-segmentation`
- **Branch**: `feature/v3-wave21` (created from `feature/v3-wave2 @ e805f3e`)
- **Date**: 2026-11-01
- **Engine**: Gemini 3.8 Flash (Medium)

---

## 1. Problem & Root-Cause Verification

Scanning a Vietnamese personal care product label (Dau goi Clony) previously produced ~45 detail cards instead of 18–19. Approximately 35 cards contained marketing claims ("MƯỢT TÓC", "KHÔNG CẦN DẦU XẢ", "Thế hệ mới"), usage instructions ("Hướng dẫn sử dụng..."), the "Thành phần:" header itself, and storage conditions ("Bảo quản..."). All were labelled "Common ingredient" / "Thành phần phổ biến". Duplicates appeared and line-wrapped multi-word names such as "Sodium Laureth Sulfate" were split into separate fragmented cards ("Sodium" and "Laureth Sulfate").

Four root causes in `LabelAnalyzer.kt` were resolved:
1. **Section Start Detection**: Previously only searched for literal lowercase strings like `thành phần:`, requiring an exact match with diacritics and a trailing colon. Any OCR deviation (omitted diacritics, space before colon, or absence of colon) triggered fallback to the entire OCR text.
2. **Missing End-Cutters**: End-cutting did not recognize storage, manufacturer, expiry, address, or net volume lines, causing text following the ingredient list to leak into tokens.
3. **Delimiter Mangling & Line-Splitting**: The tokeniser split on `\n` in addition to commas and semicolons, breaking line-wrapped ingredients. It also replaced periods `.` and parentheses `()` with spaces, mangling abbreviations and parenthesised chemical groupings.
4. **Detail List Dishonesty & Duplication**: Unmatched tokens defaulted to "Standard ingredient" / "Thành phần phổ biến" despite being cosmetic/chemical ingredients not in the database. Raw tokens were never deduplicated case/diacritic-insensitively.

---

## 2. Changes Implemented

### 2.1 Robust Section Start Detection (`DiacriticUtils.kt` & `LabelAnalyzer.kt`)
- Added `DiacriticUtils.stripDiacritics()` to strip Vietnamese and Latin diacritics safely across all KMP targets.
- Created `normalizeWithIndexMapping(text)`: normalizes to lowercase, strips diacritics, collapses consecutive whitespace, and builds a strict `originalIndices: IntArray` character mapping.
- Matched `START_MARKERS` (`ingredients`, `thanh phan`, `원재료명 및 함량`, `원재료명`, `原材料名`, `配料表`, `配料`) at word boundaries on the normalized text, advanced past delimiter characters (`:`, `：`, `-`, `/`, spaces), and sliced the **original** text at the exact index to preserve original casing and symbols.

### 2.2 Extended End-Cutters
- Implemented normalized matching for section boundaries using `END_CUTTERS`:
  - **Storage**: `huong dan bao quan`, `bao quan`, `storage instructions`, `storage`, `keep in a cool`
  - **Expiry**: `han su dung`, `expiry date`, `expiration date`, `expiry`, `use by`, `best before`, `exp date`, `exp:`, `hsd:`, `hsd`
  - **Manufacturer & Directions**: `huong dan su dung`, `directions for use`, `directions`, `how to use`, `usage`, `san xuat tai`, `san xuat boi`, `nha san xuat`, `noi san xuat`, `san xuat`, `manufactured by`, `manufactured in`, `manufactured for`, `produced by`, `made in`, `nsx:`, `nsx`
  - **Address**: `dia chi`, `address`
  - **Net Weight & Volume**: `the tich thuc`, `the tich`, `khoi luong tinh`, `khoi luong`, `net weight`, `net wt`, `net volume`, `net contents`
  - **Nutrition & Allergens**: `nutrition facts`, `thong tin dinh duong`, `contains:`, `chua:`, `co the chua`, `may contain`, `free of`, `알레르기`, `アレルギー`, `过敏`
- Preserved the 2% cut-line exception: `contains` / `chua` followed by `less than 2%` / `2% or less` / `it hon 2%` / `2% tro xuong` is preserved for the 2% cut-line classifier rather than being cut as an end-cutter.

### 2.3 Tokeniser Improvements
- Inside candidate section, all wrapped lines (`\r`, `\n`) are joined with a single space **before** splitting.
- Splitting occurs **only** on commas/semicolons and CJK equivalents (`,`, `;`, `、`, `，`, `；`) when outside parentheses/brackets (`()`, `[]`, `{}`).
- Trailing periods/colons/semicolons/commas at token ends are stripped, while internal periods (e.g. `dl-alpha`, abbreviations) and parenthesised sub-ingredients are kept intact.
- Multi-word wrapped tokens (such as `Sodium\nLaureth Sulfate`) remain a single unified token.

### 2.4 No-Marker Fallback (Named Constant `COMMA_DENSITY_THRESHOLD = 0.25`)
- When no section marker is detected, the analyzer **never** falls back to the full text.
- It parses paragraphs (or non-empty lines if no blank lines exist) and calculates comma density: `delimiters / words`.
- If the highest density exceeds `COMMA_DENSITY_THRESHOLD` (0.25), that candidate paragraph is selected and processed through end-cutters and tokenization.
- In **Fixture C** (Clony label with "Thành phần:" stripped entirely), the ingredients paragraph has 18 commas across 31 words (density 0.58 >= 0.25) and is correctly isolated; zero marketing or storage tokens leak.
- If no paragraph qualifies (e.g. descriptive prose only, tested in `testNoMarkerFallbackBranch2NoQualifyingParagraph`), the analyzer returns zero tokens, cleanly triggering Wave 2's `ProfileFitState.NOT_ENOUGH_INFO`.

### 2.5 Detail-List Honesty & Deduplication
- Raw tokens are deduplicated case- and diacritic-insensitively using `DiacriticUtils.stripDiacritics(token.text.lowercase().trim())` while preserving the original display text.
- Unmatched tokens that survive segmentation display:
  - English: `"Not in the database yet"`
  - Vietnamese: `"Chưa có trong cơ sở dữ liệu"`
  - Updated `AnalysisStrings.fallbackCategory()`, `fallbackPlainExplanation()`, `fallbackWhyUsed()`, `fallbackDoseContextNote()`, and Compose Resource key `ingredient_fallback_standard` in both `values/strings.xml` and `values-vi/strings.xml`.
- Only tokens originating inside the verified or candidate ingredients section are included in `fullBreakdown`.

---

## 3. Test & Quality Gate Results

Exact test counts parsed directly from Gradle XML test result files (`MobileApp/shared/build/test-results/`):

### 1. Spotless / Lint Check
- Command: `cd MobileApp && ./gradlew spotlessCheck`
- Status: **PASSED** (0 violations, clean).

### 2. Unit + Host Tests
- Command: `cd MobileApp && ./gradlew :shared:jvmTest :shared:testAndroidHostTest`
- **`:shared:jvmTest`**:
  - Suites: 33
  - Tests: 219
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **`:shared:testAndroidHostTest`**:
  - Suites: 28
  - Tests: 206
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **Total Combined Tests**: 61 suites, 425 tests, 0 failures, 0 errors, 0 skipped.
  *(Baseline was 407 tests: 211 jvm + 196 host. Wave 2.1 added 18 tests across both test targets).*

### 3. Android Debug Build
- Command: `cd MobileApp && ./gradlew :androidApp:assembleDebug`
- Status: **BUILD SUCCESSFUL** (APK: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`).

---

## 4. Screenshot Evidence (`Docs/qa-v2/wave21/`)

Both emulator proof screenshots were captured on `emulator-5554` running the assembled debug build and visually inspected:
1. `Docs/qa-v2/wave21/01_clony_fixture_result_en.png`: English result screen showing the honest full breakdown list with `Purified Water (Base Liquid)`, `Sodium Lauryl Sulfate (Not in the database yet)`, `Sodium Laureth Sulfate (Not in the database yet)` as a single token, `CAPB`, `CDE`, `Polyquats`, `Plantacare`, etc. Zero marketing, usage, header, or storage cards.
2. `Docs/qa-v2/wave21/02_clony_fixture_result_vi.png`: Vietnamese result screen showing `Nước tinh khiết (Dung dịch nền)`, `Sodium Lauryl Sulfate (Chưa có trong cơ sở dữ liệu)`, `Sodium Laureth Sulfate (Chưa có trong cơ sở dữ liệu)` as a single token, `CAPB`, `CDE`, `Polyquats`, etc. Zero marketing, usage, header, or storage cards.
