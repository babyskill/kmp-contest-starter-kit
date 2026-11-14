# UI Audit Fix Lane Report (`labellens-20261109-ui-audit-fix1`)

- **Task ID:** `labellens-20261109-ui-audit-fix1`
- **Requester:** Muse
- **Branch:** `feature/ui-audit-fix1`
- **Commit:** `8b3312a`
- **Target File:** `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/result/ResultScreen.kt`
- **Status:** Complete & Verified
- **Timestamp:** 2026-11-09T10:45:00+07:00

---

## 1. Overview & Scope Extension Confirmation

This lane implements the presentation-only layout and design-system token fixes for the Result Screen (`ResultScreen.kt`) based on design audit findings and physical verification on `emulator-5560`. All changes are strictly confined to `ResultScreen.kt` (no logic, string resources, or ViewModel/analyzer/domain mutations).

Confirmation: Received and implemented Muse's SCOPE EXTENSION covering tasks (6), (7), and (8).

---

## 2. Detailed Per-Task Account

### Task 1: `IngredientRow` Restructure
- **Full-Width Name**: The ingredient canonical name now occupies the full card width (`Modifier.weight(1f)`) on its own line at the top.
- **Top-Aligned Arrow**: The chevron/forward arrow icon (`Icons.Default.ArrowForward`) is aligned to `Alignment.Top` (first line) at the right edge of the card, eliminating the vertical centering defect against multi-line names.
- **Chips Row Below Name**: Moved all chips to a left-aligned row below the ingredient name.
- **Max 2 Chips Capping**:
  - Displays the group chip (`WATCH_OUT` / `WORTH_NOTING` / `GOOD_TO_KNOW`) when applicable.
  - On AI-generated rows (`ingredient.isAiGenerated`), shows the AI tag chip and suppresses the duplicate category chip.
  - On non-AI rows, displays group chip + category chip.
- **Typography & Truncation**: Name styled with `AppTheme.typography.bodyMedium` (`FontWeight.SemiBold`), `maxLines = 3`, and `TextOverflow.Ellipsis`.

### Task 2: Nutrition Highlight Row
- Assigned `Modifier.weight(1f)` to the label/subtitle column so ingredient names and secondary notes cannot overlap the numerical values.
- Assigned `maxLines = 1` and `softWrap = false` to context indicator chips (e.g. "Vừa phải", "Tiêu chuẩn"), preventing per-character wrapping.

### Task 3: `ProfileFitHeaderCard` Typography & Deduplication
- **Message Deduplication**: Evaluates `fitHeader.subtitle` and `fitHeader.reasonNote` against `fitHeader.title`. If subtitle matches title, it is suppressed.
- **Font Weight**: Heading font weight set to `FontWeight.SemiBold`.
- **Typography Token**: Replaced hardcoded `21.sp` heading size with standard theme typography scale `AppTheme.typography.titleMedium` (20sp).

### Task 4: Empty Detail Section Guard
- Wrapped both the "Phân tích chi tiết" section header and the ingredient items in `if (groupedIngredients.isNotEmpty())`. When the breakdown list is empty, neither the header nor the list is rendered.

### Task 5: Token Discipline
- Replaced hardcoded status container and accent colors with existing design system tokens:
  - `Color(0xFFFDECEA)` -> `AppTheme.colors.status.allergenContainer`
  - `Color(0xFFB3261E)` -> `AppTheme.colors.status.allergenRed`
  - `Color(0xFFFBF3DF)` -> `AppTheme.colors.status.amberContainer`
  - `Color(0xFFB7791F)` -> `AppTheme.colors.status.amber`
- Card corner shapes standardized to `RoundedCornerShape(16.dp)`.

### Task 6 (Scope Extension): Bottom Action Cluster
- Replaced the large disabled grey button with a non-button confirmation indicator (checkmark icon + text) when `state.isSaved` is true.
- Placed inside a unified action row with `IntrinsicSize.Min` and equal heights alongside the "Compare" button.
- Added a 36dp bottom spacer (`AppTheme.spacing.largeSpacing`) so the last element is never flush against the screen edge or navigation bar.

### Task 7 (Scope Extension): `ProfileFitHeaderCard` Single-Line Badge & Dedicated Correction Row
- The status badge row at the top now strictly contains the status icon and badge label text, configured with `maxLines = 1`, `softWrap = false`, and `overflow = TextOverflow.Ellipsis`.
- Moved the correction pill button ("Nhận diện sai? Sửa lại" / "Misread label? Fix") to its own dedicated row at the bottom of the card, preventing any horizontal crowding or wrapping onto the state label.

### Task 8 (Scope Extension): Chip Truthfulness
- Strictly capped detail rows to a maximum of 2 chips on AI rows by omitting category chips when the AI chip is active.
- Reviewed `IngredientBreakdown` model: it does not carry an explicit personal profile flag distinction directly; profile matching is resolved upstream via `groupAndOrderIngredients` against `result.watchOutForYou`. Per instructions, rendering based on `group` was kept as-is without touching domain logic.

---

## 3. Deliberately Retained Values & Rationale

- **AI Tag Chip Colors (`0xFFEDE7F6`, `0xFFB39DDB`, `0xFF5E35B1`)**:
  - `AppColors` and `AppColors.Status` currently do not define a dedicated token for AI/purple tag styling.
  - Per Task 5 ("Do not invent new tokens; if no token exists for a value, keep the value but move on — note it in the REPORT"), these values were retained.

---

## 4. Test Verification (Exact Counts from XML)

All unit and Android host test suites passed with zero failures.

- **JVM Tests (`MobileApp/shared/build/test-results/jvmTest`)**:
  - Suites: 38
  - Tests: 255
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **Android Host Tests (`MobileApp/shared/build/test-results/testAndroidHostTest`)**:
  - Suites: 32
  - Tests: 240
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- **Total Combined**:
  - Suites: 70
  - Tests: 495
  - Failures: 0
  - Errors: 0
- **Spotless / ktlint**: Passed cleanly via `./gradlew spotlessCheck`.

---

## 5. Emulator Verification & Artifacts

- **Device**: `emulator-5560` (Android 16 emulator)
- **APK Installed**: `MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- **Screenshots Captured**:
  1. `Docs/qa-v2/ui-audit-fix1/01_rows.png`: Shows Clony shampoo result screen with restructured ingredient breakdown cards: full-width names, arrow aligned to top row, chips left-aligned below name, no vertical centering defect.
  2. `Docs/qa-v2/ui-audit-fix1/02_nutrition.png`: Shows Honey Oat Cereal result screen with Nutrition Highlights section: `Modifier.weight(1f)` label column, right-aligned values without overlap, and single-line non-wrapping context chips.
