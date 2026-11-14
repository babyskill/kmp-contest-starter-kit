# AGY_REPORT_labellens-20261031-wave1-unmasker.md
**Lane:** `labellens-20261031-wave1-unmasker`  
**Branch:** `feature/v3-wave1` (branched from `feature/v2-proof` at `5d45523`)  
**Date:** 2026-10-31  

---

## 1. Overview & Scope

Wave 1 of PRD V3 (Single-Scan Unmasker) delivers a unified, post-scan result experience for food and supplement labels (US beachhead, global-ready) operating entirely on-device (Tier 0, deterministic, zero network calls).

### Four Core Wave 1 Additions Built:
1. **Alias Roll-Up Clusters (Knowledge Base)**:
   - **SWEETENERS Cluster**: 73 distinct US label names (exceeding the >= 65 requirement). Covers cane sugar, brown sugar, raw sugar, turbinado sugar, demerara sugar, coconut sugar, date sugar, beet sugar, powdered sugar, confectioners sugar, invert sugar, cane juice, evaporated cane juice, cane syrup, brown rice syrup, corn syrup, high fructose corn syrup, corn syrup solids, glucose syrup, tapioca syrup, rice syrup, barley malt syrup, malt syrup, sorghum syrup, golden syrup, maple syrup, agave nectar, agave syrup, honey, molasses, blackstrap molasses, treacle, dextrose, glucose, fructose, sucrose, maltose, lactose, galactose, maltodextrin, dextrin, fruit juice concentrate, apple juice concentrate, grape juice concentrate, pear juice concentrate, fruit puree concentrate, caramel, ethyl maltol, barley malt, malt extract, corn sweetener, refiners syrup, sorghum, saccharose, isomaltulose, coconut nectar, date syrup, yacon syrup, monk fruit extract, stevia leaf extract, etc. *Erythritol explicitly excluded.*
   - **SODIUM SOURCES Cluster**: 28 distinct label names (exceeding the >= 25 requirement). Covers salt, sea salt, sodium chloride, table salt, kosher salt, monosodium glutamate, MSG, sodium bicarbonate, baking soda, sodium benzoate, sodium nitrite, sodium nitrate, disodium phosphate, sodium phosphate, trisodium phosphate, sodium citrate, sodium carbonate, sodium erythorbate, sodium ascorbate (with explicit explanation that it is the sodium form of vitamin C), disodium inosinate, disodium guanylate, sodium caseinate, sodium alginate, sodium propionate, sodium metabisulfite, monosodium phosphate, sodium acid pyrophosphate, brine.
   - **Canonical Resolution & 100% Vietnamese Overlay**: Added 30 new canonical ingredients to `KnowledgeBase.kt` (increasing registry from 127 to 157 entries) and mapped 100% of all 157 entries in `KnowledgeBaseVi.kt` (`displayName`, `category`, `plainExplanation`, `whyUsed`, `doseContextNote`). In Vietnamese mode, cluster items always render natural Vietnamese terms.
   - **Phrase-Boundary Matching**: Implemented word-boundary regexes in `KnowledgeClusters.kt` to prevent false positives. Verified with unit tests yielding 0 false positives on: `nutmeg`, `cocoa butter`, `lactic acid`, `oat flour`, `unsalted butter`.

2. **Portion Reality (Portion Math)**:
   - **Parser Extension**: `PortionMath.parsePortionInfo()` parses serving size and servings per container from OCR text.
   - **Interactive Segmented Toggle**: `[1 Serving]` / `[Whole Pack]` rendered in `ResultScreen.kt` if `servingsPerContainer > 1`.
   - **Default Selection Logic**: Default `Whole Pack` when servings per container is between 1.5 and 3.5 inclusive; otherwise `1 Serving`.
   - **Nutrient Scaling**: When Whole Pack is selected, all nutrient values scale by servings per container:
     - Added sugars: Grams AND teaspoons (`g / 4`, one decimal, e.g. `9.4 tsp` / `9.4 muỗng cà phê đường`).
     - Sodium: mg AND percent daily value (`mg / 2300 * 100%`, rounded integer) AND grams of salt equivalent (`mg * 2.5 / 1000`, one decimal, e.g. `20% DV • 1.2g salt eq`).
   - Pure domain functions located in `com.labellens.app.domain.analyzer.PortionMath` with unit tests covering fractional servings (e.g. 2.5) and missing container info.

3. **The 2% Cut-Line and Catch-All Badges**:
   - **2% Cut-Line Detection**: Detects `contains 2% or less of`, `contains less than 2% of`, and OCR-spaced variants (e.g. `Contains  2  %   or   less   of`). Inserts a subtle divider line labelled `Tiny amounts / Flavourings (≤ 2%)` (EN) and `Hàm lượng rất nhỏ / Hương liệu (≤ 2%)` (VI).
   - **Catch-All Badges**: Flags non-specific catch-all terms (`natural flavor`, `natural flavors`, `spices`, `spice`, `parfum`, `fragrance`, `proprietary blend`) with an amber informational badge and explanatory note: *"The label does not disclose which specific substances are inside this term."* / *"Nhãn không công khai các chất cụ thể bên trong thuật ngữ này."*
   - **Marquee Positional Note**: If a marquee ingredient (honey, olive oil, avocado, or product name keywords) appears after the 2% cut-line or after salt, generates a factual, neutral note that it is listed after salt or in the 2%-or-less section.

4. **Halo-Pattern Notes (Tier 0 Rules)**:
   - **Pattern A (Engineered Fibre Halo)**: Fires when added sugar is present (Added Sugars > 0 OR >= 2 sweetener aliases) AND an isolated fibre is present (`inulin`, `chicory root fiber`, `chicory root fibre`, `polydextrose`, `soluble corn fiber`, `resistant maltodextrin`). Factual note explains that fibre was added as an isolated ingredient alongside sweeteners.
   - **Pattern B (Fruit in Name, Concentrate in Fact)**: Fires when a fruit appears in the product name but fruit ingredients appear only as juice or puree concentrates. Factual note explains fruit content appears as concentrate.
   - *Tone & Guardrails*: 100% neutral, objective phrasing. Zero moralizing or judgment terms ("toxic", "bad", "dirty", "unhealthy") and zero scores.

---

## 2. Architecture & Design Decisions

- **Domain Isolation (`PortionMath.kt`)**: Kept all portion math and unit conversion pure and platform-independent under `commonMain/domain/analyzer/PortionMath.kt`.
- **Knowledge Clusters (`KnowledgeClusters.kt`)**: Centralized US label aliases and regex boundary matching patterns into an immutable registry.
- **Card Placement in `ResultScreen.kt`**: Placed the `AliasRollUpCard` and the `NutritionHighlights` card (with the `[1 Serving]` / `[Whole Pack]` toggle) immediately below the product title & verdict header. This allows both the roll-up clusters and the portion reality values to be fully rendered in a single viewport without being cut off.
- **Responsive Layout**: Replaced horizontal title/subtitle row with a vertically stacked title and descriptive subtitle in `AliasRollUpCard`, preventing text clipping or overflow across all screen sizes and languages.
- **Format String Parity**: Used `stringResource(Res.string.result_portion_servings_count, countStr)` in Compose Multiplatform, avoiding raw string template interpolation collisions with literal `%1$s`.
- **Sample Integration**: Added `BuiltInSamples.sample7` ("Chewy Chocolate Chip Granola Bars") to `SampleProducts.kt` featuring split sweeteners (Cane Sugar, Corn Syrup, Sugar, Honey, Molasses), 2% cut-line, 2.5 servings per container, Added Sugars (6g), Inulin (isolated fibre), and Natural Flavors (catch-all).

---

## 3. Quality Gates & Test Verification

All quality gates and test suites were executed cleanly:

### Lint / Formatting
- Command: `./gradlew spotlessCheck`
- Result: **BUILD SUCCESSFUL** (clean, 0 violations).

### Unit & Domain Tests (shared:jvmTest)
- Command: `./gradlew :shared:jvmTest`
- Result: **BUILD SUCCESSFUL**
- **Exact Counts (read from XML test reports under `shared/build/test-results/jvmTest/`)**:
  - **Suites:** 30
  - **Total Tests:** 183
  - **Failures:** 0
  - **Errors:** 0
  - **Skipped:** 0

### Host Tests (shared:testAndroidHostTest)
- Command: `./gradlew :shared:testAndroidHostTest`
- Result: **BUILD SUCCESSFUL**
- **Exact Counts (read from XML test reports under `shared/build/test-results/testAndroidHostTest/`)**:
  - **Suites:** 26
  - **Total Tests:** 171
  - **Failures:** 0
  - **Errors:** 0
  - **Skipped:** 0

### Combined Test Total
- **Total Test Suites:** 56 suites
- **Total Tests Executed:** 354 tests
- **Total Failures:** 0

### Android Debug Build
- Command: `./gradlew :androidApp:assembleDebug`
- Result: **BUILD SUCCESSFUL** (APK: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`)

---

## 4. Emulator Proof Screenshots (`Docs/qa-v2/wave1/`)

Captured on running emulator (`emulator-5554`, `Pixel_4_2` form factor, 1080x2424) using built-in sample `sample7`:
1. `Docs/qa-v2/wave1/01_result_en.png`: English result screen showing the Sweeteners (5 found) roll-up card with aliases, Sodium Sources (2 found) roll-up card, and Nutrition Highlights card with the `[1 Serving]` / `[Whole Pack]` toggle (Whole Pack active by default, scaling calories to 625 kcal, Added Sugars to 37.5g & 9.4 tsp, Sodium to 470mg & 20% DV • 1.2g salt eq).
2. `Docs/qa-v2/wave1/02_result_vi.png`: The exact same result screen in Vietnamese mode showing localized cluster titles ("Chất tạo ngọt (phát hiện 5 loại)", "Nguồn Natri (phát hiện 2 loại)"), Vietnamese ingredient chips ("Đường mía", "Siro ngô...", "Mật ong", "Mật rỉ đường", "Muối biển", "Baking soda"), localized servings count ("2.5 khẩu phần mỗi hộp"), localized toggle (`[1 Khẩu phần]` / `[Cả hộp]`), and localized secondary details ("10.9 muỗng cà phê đường", "9.4 muỗng cà phê đường", "20% Nhu cầu hằng ngày • 1.2g muối quy đổi").
3. `Docs/qa-v2/wave1/03_toggle_serving_en.png`: English result screen after tapping `1 Serving` toggle, demonstrating instant recalculation to single-serving values (Calories 250 kcal, Sugars 17.5g & 4.4 tsp, Added Sugars 15g & 3.8 tsp, Sodium 188mg & 8% DV • 0.5g salt eq).

All screenshots were visually inspected and verified: all cards, chips, and toggles are sharp and visibly rendered with zero cut-off or blank placeholders.

---

## 5. Files Changed & Added

### Added Files:
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/KnowledgeClusters.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/PortionMath.kt`
- `MobileApp/shared/src/commonTest/kotlin/com/labellens/app/domain/analyzer/PortionMathTest.kt`
- `MobileApp/shared/src/commonTest/kotlin/com/labellens/app/domain/analyzer/Wave1UnmaskerTest.kt`
- `Docs/qa-v2/wave1/01_result_en.png`
- `Docs/qa-v2/wave1/02_result_vi.png`
- `Docs/qa-v2/wave1/03_toggle_serving_en.png`
- `AGY_REPORT_labellens-20261031-wave1-unmasker.md`
- `AGY_DONE_labellens-20261031-wave1-unmasker.md`

### Modified Files:
- `MobileApp/shared/src/commonMain/composeResources/values/strings.xml`
- `MobileApp/shared/src/commonMain/composeResources/values-vi/strings.xml`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/AnalysisStrings.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/KnowledgeBase.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/KnowledgeBaseVi.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/analyzer/LabelAnalyzer.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/model/AnalysisModels.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/model/SampleProducts.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/result/ResultScreen.kt`
- `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/result/ResultViewModel.kt`
- `MobileApp/shared/src/commonTest/kotlin/com/labellens/app/domain/analyzer/LabelAnalyzerTest.kt`
