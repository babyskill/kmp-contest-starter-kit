# AGY Report: Scan Tab Direct Camera & Auto History Persistence (`labellens-20261102-scan-history-fix`)

- **Task ID:** `labellens-20261102-scan-history-fix`
- **Branch:** `feature/scan-history-fix` (branched from `feature/ui-redesign-v2` at `4c1641b`)
- **Commit:** `53ef15581a9c26abfcbb28e1123023e6cc733b5a`
- **Status:** Complete & Verified
- **Finished At:** 2026-11-02T18:40:00+07:00

---

## 1. Overview & Objectives

Resolved two user-reported defects on the LabelLens application platform:
1. **DEFECT 1 (Direct Camera Launch on Scan Tab):** Tapping the centre Scan button in the bottom navigation bar (`AppNavigation.kt`) now immediately opens camera capture via `FileKit.openCameraPicker()`. On photo capture, the exact same analysis pipeline used by `ScanScreen` runs and navigates to the Result screen. On cancel/null, the user stays on the current tab without navigation or error. On platforms where camera capture is a no-op (`nonMobileMain`, where `isCameraCaptureSupported() == false`), navigation falls back to the Scan tab as before. `ScanScreen` remains intact for barcode and paste-text entries.
2. **DEFECT 2 (Automatic Scan History Persistence):** Completed user scan results (camera photos, barcode lookups, pasted text) are now automatically persisted to the Room local database (`ScanHistoryDao` / `ScanRepository`) when the result is first displayed. The implementation fulfills all four core invariants:
   - *(a) Idempotency:* Recomposition, navigating back/forth, or process recreation does not create duplicate entries.
   - *(b) History Re-entry:* Opening a scan from History does not create a new entry.
   - *(c) Sample Protection:* Built-in sample previews (`sampleId != null` or fallback sample) are never auto-saved.
   - *(d) Accurate Saved State:* Result screen's save button immediately reflects the persisted state ("Saved to History" / "Đã lưu vào lịch sử", disabled).

---

## 2. Root Cause Analysis

### Defect 1: Scan Tab Required Extra Tap to Capture
- **Root Cause:** In `AppNavigation.kt`, the `BottomNavigationBar` click handler previously executed `navigator.navigate(targetRoute)` unconditionally for all items. When tapping the centre Scan tab, the app merely navigated to `ScanTabScreenRoute`, where `ScanScreen` forced the user to manually locate and tap the "Take photo" (`Chụp ảnh`) button.
- **Resolution:**
  - Evaluated `isCameraCaptureSupported()`.
  - When true, tapping the Scan bottom nav item launches `FileKit.openCameraPicker()` in a coroutine.
  - If a photo is captured, `scanViewModel.processImageSource(captured)` executes the analysis pipeline, rendering a lightweight non-blocking `ScanProcessingCard` overlay showing honest steps ("Acquiring label image...", "Scanning for barcodes...", "Recognising text & multilingual characters (ML Kit OCR)...", "Parsing ingredients & nutrition metrics..."). On completion, `navigator.navigate(ResultScreenRoute(...))` opens the result.
  - If the user cancels the camera (`captured == null`), no navigation occurs, no error is raised, and the user stays on their current tab.
  - On desktop and web where `isCameraCaptureSupported()` returns false, it seamlessly falls back to navigating to `ScanTabScreenRoute`.

### Defect 2: Results Not Persisted to History Automatically
- **Root Cause:**
  - `ResultViewModel.saveToHistory()` was strictly triggered by an explicit button tap on `ResultScreen`.
  - In `ScanRepositoryImpl.saveScan()`, if `barcode` was null (as in camera photo OCR or pasted text), `saveScan()` generated a fresh random UUID on every invocation (`Uuid.random().toString()`). If called on re-display or recomposition, this would insert duplicate records into Room.
  - `LabelAnalysisResult` lacked an `id` field. When `HistoryScreen` navigated to `ResultScreenRoute`, it passed `scanId = scan.barcode ?: scan.productName`. If `barcode` was null, `getScanByIdReanalyzed(scan.productName)` failed to find the random UUID entity, falling through to re-analysis as a new scan.
- **Resolution:**
  - Added optional `id: String? = null` to domain model `LabelAnalysisResult` and preserved `entity.id` across entity mapping in `ScanHistoryEntity.toAnalysisResult()`.
  - Added `@Query("SELECT * FROM scan_history WHERE raw_label_text = :rawText LIMIT 1") suspend fun getByRawText(rawText: String): ScanHistoryEntity?` to `ScanHistoryDao`.
  - In `ScanRepositoryImpl`, `saveScan()` now looks up existing items by `id`, `barcode`, or `rawLabelText`. If found, it updates the existing entry preserving the original timestamp, favorite state, and feedback rather than inserting a duplicate record.
  - In `ResultViewModel.loadAnalysis()`, every completed user scan (camera photo, barcode lookup, or pasted text) is automatically persisted via `scanRepository.saveScan(analysis)` upon first presentation, setting `isSaved = true`.
  - Guarded `loadAnalysis()` with `lastLoadedKey` to prevent re-saving during recompositions.
  - When loading an item from History (`!scanId.isNullOrBlank()`), `loadAnalysis()` loads the scan via `scanRepository.getScanByIdReanalyzed(scanId, profile)`, sets `isSaved = true`, and returns early without saving.
  - Built-in sample previews (`!sampleId.isNullOrBlank()` or sample fallback) explicitly skip saving and retain `isSaved = false`.

---

## 3. Files Changed

| File Path | Description of Changes |
|---|---|
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/domain/model/AnalysisModels.kt` | Added `val id: String? = null` to `@Serializable data class LabelAnalysisResult`. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/data/source/local/entity/ScanHistoryEntity.kt` | Preserved `id = id` in `toAnalysisResult()` mapping. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/data/source/local/dao/ScanHistoryDao.kt` | Added `getByRawText(rawText: String)` query to support deterministic lookups for non-barcode scans. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/data/repository/ScanRepository.kt` | Implemented idempotent `saveScan()` checking existing `id`, `barcode`, and `rawLabelText`; updated `getScanById` / `getScanByIdReanalyzed` / `toggleFavorite` / `setFeedback` / `deleteScan` to fallback to barcode lookup and retain entity `id`. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/navigation/AppNavigation.kt` | Wired direct camera launch via `FileKit.openCameraPicker()` in `BottomNavigationBar` when `isCameraCaptureSupported()`; shared `scanViewModel` across navigation and tab destinations; added `ScanProcessingCard` overlay during processing; updated history navigation to pass `scan.id ?: scan.barcode ?: scan.productName`. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/scan/ScanScreen.kt` | Extracted reusable `ScanProcessingCard` composable for consistent honest step progress presentation. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/history/HistoryScreen.kt` | Updated `deleteItem` invocation to prefer `scan.id ?: scan.barcode ?: scan.productName`. |
| `MobileApp/shared/src/commonMain/kotlin/com/labellens/app/presentation/screens/result/ResultViewModel.kt` | Added auto-save on initial display for completed user scans; enforced idempotency via `lastLoadedKey` and repository content matching; prevented auto-save for sample previews; updated `saveToHistory`, `toggleFavorite`, `submitFeedback`, and `fixMisreadLabel` to preserve `id`. |
| `MobileApp/shared/src/commonTest/kotlin/com/labellens/app/presentation/screens/result/ResultViewModelTest.kt` | Created 6 unit tests covering auto-save execution, recomposition/re-entry idempotency, history re-entry idempotency, sample non-persistence, barcode auto-save, and pasted text auto-save. |
| `Docs/qa-v2/scan-history-fix/01_camera_open.png` | Proof screenshot: Camera UI opening immediately upon tapping bottom nav Scan button. |
| `Docs/qa-v2/scan-history-fix/02_result.png` | Proof screenshot: Completed scan result with save button showing "Đã lưu vào lịch sử". |
| `Docs/qa-v2/scan-history-fix/03_history_after_scan.png` | Proof screenshot: History tab showing the scanned item without any manual save tap. |
| `Docs/qa-v2/scan-history-fix/04_history_after_restart.png` | Proof screenshot: History tab still showing the scan after application force-stop and cold relaunch. |

---

## 4. Verification & Quality Gates

### Test Results (Counted from Gradle XMLs)
- **`jvmTest`**: 34 suites, 227 tests, 0 failures, 0 errors, 0 skipped
- **`testAndroidHostTest`**: 28 suites, 212 tests, 0 failures, 0 errors, 0 skipped
- **Total Combined**: 62 suites, **439 tests** (baseline >= 429), **0 failures**

### Lint & Formatting
- **`spotlessCheck`**: PASSED (0 violations)

### Build Gate
- **`:androidApp:assembleDebug`**: BUILD SUCCESSFUL

### String Parity
- **`StringParityTest`**: PASSED (100% key parity between `values/strings.xml` and `values-vi/strings.xml`, no hardcoded strings, brand/product proper nouns preserved).

---

## 5. Emulator Proof & Honest Deviations

- **Device Instance:** `Pixel_4_2`, port `5556` (`emulator-5556`).
- **Foreground Assertion:** Foreground state asserted for `com.labellens.app` before every tap/action. `emulator-5554` was never driven.
- **Proof Screenshots (`Docs/qa-v2/scan-history-fix/`):**
  1. `01_camera_open.png` (26 KB): Shows Android Camera `com.android.camera2/com.android.camera.CaptureActivity` launched immediately upon tapping the centre Scan button on the bottom bar.
  2. `02_result.png` (226 KB): Shows the completed scan result screen with the bottom button displaying `"Đã lưu vào lịch sử"` (Saved to History, disabled), confirming auto-save.
  3. `03_history_after_scan.png` (109 KB): Shows the History tab displaying `"Lịch sử quét - Đã lưu cục bộ 1 sản phẩm"` with the item `"Custom Label Text"` present without any manual save action.
  4. `04_history_after_restart.png` (108 KB): Shows the History tab after `am force-stop com.labellens.app` and relaunch, proving persistent SQLite/Room storage across app sessions.
- **Honest Deviation Note:**
  As anticipated in the task instructions, the Android emulator virtual camera captures an empty test room/checkerboard pattern containing no text, so ML Kit OCR cannot extract label text from the virtual camera frame. In accordance with the brief instructions (*"If emulator camera capture cannot complete, say so honestly in the report and prove history via the paste-text path instead"*), the camera launch is verified in `01_camera_open.png`, and the full end-to-end auto-save persistence and cold restart were verified via the label text analysis path.
