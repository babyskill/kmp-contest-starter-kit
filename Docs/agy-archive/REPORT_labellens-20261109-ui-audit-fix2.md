# UI Audit Fix Lane Report (`labellens-20261109-ui-audit-fix2`)

- **Task ID:** `labellens-20261109-ui-audit-fix2`
- **Requester:** Muse
- **Branch:** `feature/ui-audit-fix2`
- **Base Branch:** `feature/ui-audit-fix1` (`8b3312a`)
- **Commit:** `69a4b14d542f19503b8599ce91aa50625a20f61e`
- **Status:** Complete & Verified
- **Finished At:** 2026-11-09T12:22:15+07:00

---

## 1. Overview & Scope

This lane resolves the S3/S4 UI defects identified during the independent full-screen audit of LabelLens (KMP Android) on branch `feature/ui-audit-fix1`. All changes are strictly confined to layout, composables (`HomeScreen`, `ResultScreen`, `HistoryScreen`, `ScanScreen`, `ProfileScreen`), and bilingual label strings (`values/strings.xml`, `values-vi/strings.xml`). No domain logic, ViewModel signatures, analyzer rules, or infrastructure configs were modified.

---

## 2. Changes per Defect

### D1. Home Greeting at Font Scale 1.3
- **Root Cause:** Greeting strings had standard spaces before trailing emoji icons (`☀️`, `✌`, `🌙`), allowing the emoji to wrap onto a third line alone when font scaling pushed word metrics past card column boundaries.
- **Fix:** In `values/strings.xml` and `values-vi/strings.xml`, bound emoji icons with non-breaking spaces (`&#160;`). In `HomeScreen.kt`, additionally normalized strings via non-breaking space replacement (`\u00A0`), set `maxLines = 2`, and added `TextOverflow.Ellipsis`.
- **Verification:** Greeting stays cleanly on 2 lines with the sun emoji attached to "sáng" at font scale 1.3 (`01_home_13.png`).

### D2. Result Bottom Action Cluster at Font 1.3
- **Root Cause:** Confirmation text "Đã lưu vào lịch sử" wrapped to 2 lines at font scale 1.3, making the saved indicator taller than the adjacent "So sánh" button and unbalancing the action cluster.
- **Fix:** Shortened display label string to "Đã lưu" (VI) / "Saved" (EN) while preserving the full semantic accessibility description via `result_btn_saved_history_desc` ("Đã lưu vào lịch sử" / "Saved to History") via Compose `semantics(mergeDescendants = true) { contentDescription = savedDesc }`. Formatted label with `maxLines = 1`, `softWrap = false`, and bounded the container with `defaultMinSize(minHeight = 48.dp)` inside `height(IntrinsicSize.Min)` row.
- **Verification:** Both action elements remain strictly on 1 line with balanced 48dp+ heights (`02_result_bot_13.png`).

### D3. "Xem văn bản gốc trên nhãn" Row at Font 1.3
- **Root Cause:** Row used `Arrangement.SpaceBetween` without child weight constraints, allowing the long title "Xem văn bản gốc trên nhãn" to squeeze the expand control into a narrow box where "Mở rộng" wrapped into "Mở" / "rộng".
- **Fix:** Assigned `Modifier.weight(1f)` to the section header text and added `Spacer(modifier = Modifier.width(12.dp))`. Set the toggle text ("Mở rộng" / "Thu gọn") to `maxLines = 1` and `softWrap = false`.
- **Verification:** "Mở rộng" stays on one line on the right edge while the left text wraps comfortably.

### D4. History Header at Font Scale 1.3
- **Root Cause:** The filter button previously used a heavy `AppButton` instance styled with `bodyLarge` without wrapping constraints alongside an unweighted title column.
- **Fix:** Assigned `Modifier.weight(1f)` to the header title column and replaced the heavy button with a compact, dedicated `Surface` chip (`RoundedCornerShape(16.dp)`, `AppTheme.colors.alternative`) with `maxLines = 1`, `softWrap = false`, and `overflow = TextOverflow.Ellipsis`.
- **Verification:** "Chỉ mục yêu thích" chip fits cleanly on a single line at font scale 1.3 (`03_history_13.png`).

### D5. History Delete Confirmation Dialog
- **Root Cause:** Tapping the delete icon on a history card immediately called `viewModel.deleteItem(...)` without confirmation.
- **Fix:** Introduced `var itemPendingDelete by remember { mutableStateOf<String?>(null) }` in `HistoryScreen.kt`. Tapping the card trash button sets `itemPendingDelete`. An `AlertDialog` displays matching the Result screen delete confirmation pattern using existing string resources (`result_delete_dialog_title`, `result_delete_dialog_message`, `result_delete_dialog_confirm`, `result_delete_dialog_cancel`), with cancellation and confirmation actions.
- **Verification:** Delete dialog opens and renders properly upon tapping any card's trash icon (`05_delete_dialog.png`).

### D6. Scan Screen Back Affordance
- **Root Cause:** The pushed `ScanScreen` lacked a top-left back affordance, leaving no visible navigation control back to the previous screen.
- **Fix:** Restructured the header block in `ScanScreen.kt` into a top `Row` with an `IconButton(onClick = onNavigateBack)` displaying `Icons.Default.ArrowBack` with contentDescription `stringResource(Res.string.btn_back)`, matching the Result screen top navigation pattern.
- **Verification:** Back arrow rendered at top-left and verified to navigate back to Home upon tap (`04_scan_back.png`).

### D12. Profile "Cài đặt & Miễn trừ trách nhiệm" Card Truncation & Arrow
- **Root Cause:** The card description in `ProfileScreen.kt` had `maxLines = 2` without ellipsis truncation, causing mid-sentence clipping, and used hardcoded ASCII `"->"`.
- **Fix:** Added `overflow = TextOverflow.Ellipsis` to the description text, replaced `"->"` with `"→"`, and added a 12dp spacer between the text column and the arrow.
- **Verification:** Profile screen renders proper ellipsis on the privacy description and displays clean "→" glyph.

### D13. Profile Attention Flags Label Cleanup
- **Root Cause:** Strings had dev-facing "(v1)" in both Vietnamese and English resources (`profile_attention_flags_title`).
- **Fix:** Removed "(v1)" from both resources:
  - `values/strings.xml`: `<string name="profile_attention_flags_title">Attention Flags</string>`
  - `values-vi/strings.xml`: `<string name="profile_attention_flags_title">Cờ chú ý dinh dưỡng</string>`
- **Verification:** Verified in UI hierarchy on Profile screen.

### D14. Replace ASCII "->" with "→" Glyph Across Sample Rows
- **Root Cause:** Sample action buttons used ASCII `-&gt;` in strings.
- **Fix:** Replaced `-&gt;` with `→` in `scan_sample_run_btn` ("Chạy thử →" / "Run →") and `home_sample_tap_to_test` ("Chạm để thử →" / "Tap to test →").
- **Verification:** Verified in UI hierarchy on Scan screen and Home screen.

---

## 3. Test Verification (Exact Counts from XML)

All automated tests passed with 0 failures, 0 errors, and 0 skipped tests.

### JVM Tests (`MobileApp/shared/build/test-results/jvmTest`)
- **Suites:** 38
- **Tests:** 255
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Android Host Tests (`MobileApp/shared/build/test-results/testAndroidHostTest`)
- **Suites:** 33
- **Tests:** 242
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Total Combined
- **Suites:** 71
- **Tests:** 497
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Formatting Gate
- `./gradlew spotlessCheck` passed cleanly with 0 violations.

---

## 4. Emulator Verification & Artifacts

- **Target Device:** `emulator-5560` (Pixel 9a, Android 16)
- **Foreground Assertion:** Verified `topResumedActivity=ActivityRecord{... com.labellens.app/.AppActivity ...}` before every tap.
- **Font Scale:** Exercised at `font_scale 1.3`, restored to `1.0` upon completion.
- **Screenshots:**
  1. `Docs/qa-v2/ui-audit-fix2/01_home_13.png` — Home screen at font 1.3 showing greeting on 2 lines with attached sun emoji.
  2. `Docs/qa-v2/ui-audit-fix2/02_result_bot_13.png` — Result screen bottom action cluster at font 1.3 showing single-line "✓ Đã lưu" equal height with "So sánh".
  3. `Docs/qa-v2/ui-audit-fix2/03_history_13.png` — History screen at font 1.3 showing single-line "Chỉ mục yêu thích" chip.
  4. `Docs/qa-v2/ui-audit-fix2/04_scan_back.png` — Pushed Scan screen showing the top-left back arrow.
  5. `Docs/qa-v2/ui-audit-fix2/05_delete_dialog.png` — History delete confirmation dialog ("Xóa kết quả quét?").
