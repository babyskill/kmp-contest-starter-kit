muse_task_id: labellens-20261102-scan-history-fix
requested_by: Muse
status: done
report: AGY_REPORT_labellens-20261102-scan-history-fix.md
finished_at: 2026-11-02T18:41:00+07:00
summary: Resolved DEFECT 1 (centre bottom-nav Scan button directly launches camera capture via FileKit.openCameraPicker() and runs analysis pipeline on capture, staying on current tab on cancel/null, and falling back to Scan tab on nonMobile) and DEFECT 2 (completed user scan results auto-persisted to Room database on first display with idempotency, history re-entry safety, sample protection, and accurate saved button state). All quality gates pass with 62 suites, 439 tests (jvmTest 227 + hostTest 212, baseline >= 429), 0 failures, spotlessCheck passed, and assembleDebug succeeded. All 4 emulator proof screenshots verified under Docs/qa-v2/scan-history-fix/.

## Completion Details

- **Commit Hash:** `53ef15581a9c26abfcbb28e1123023e6cc733b5a`
- **Branch:** `feature/scan-history-fix`
- **Test Results (Exact XML counts from disk):**
  - `jvmTest`: 34 suites, 227 tests, 0 failures, 0 errors, 0 skipped
  - `testAndroidHostTest`: 28 suites, 212 tests, 0 failures, 0 errors, 0 skipped
  - Combined: 62 suites, 439 tests (baseline >= 429), 0 failures
- **Code Quality & Build Gates:**
  - `spotlessCheck`: PASSED (0 violations)
  - `assembleDebug`: BUILD SUCCESSFUL
- **Emulator Proof Screenshots:**
  - `Docs/qa-v2/scan-history-fix/01_camera_open.png` (Camera UI right after tapping centre Scan button)
  - `Docs/qa-v2/scan-history-fix/02_result.png` (Completed scan result showing "Đã lưu vào lịch sử")
  - `Docs/qa-v2/scan-history-fix/03_history_after_scan.png` (History tab showing scan with no manual save tap)
  - `Docs/qa-v2/scan-history-fix/04_history_after_restart.png` (History tab showing scan after force-stop and restart)
