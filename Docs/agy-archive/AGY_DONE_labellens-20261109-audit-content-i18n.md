muse_task_id: labellens-20261109-audit-content-i18n
requested_by: Muse
status: done
report: REPORT_labellens-20261109-audit-content-i18n.md
finished_at: 2026-11-09T13:04:00+07:00
summary: Resolved content and i18n defects D7-D11: sanitized OCR product-name fallbacks with localized 'Untitled label' ('Chưa đặt tên'), isolated ingredient definition fallback and hid caution section for unknown ingredients, removed backslash-escaped quotes in onboarding, localized scan summaries dynamically at display time without migrating SQLite data, and localized Compare nutrition row labels in VI and EN. Verified on emulator-5560 with 5 proof screenshots; spotless passed and test suites succeeded with exact counts jvmTest (39 suites, 259 tests, 0 failures) and testAndroidHostTest (34 suites, 246 tests, 0 failures).

Commit hashes:
- 77ef831 (feature/audit-content-i18n) fix(i18n): resolve content and localization defects D7-D11
- 9b0fe34 (feature/audit-content-i18n) docs: add audit content and i18n report

Exact Test Counts (from XML test reports):
- jvmTest: 39 suites, 259 tests, 0 failures, 0 errors, 0 skipped
- testAndroidHostTest: 34 suites, 246 tests, 0 failures, 0 errors, 0 skipped
- Total: 73 suites, 505 tests, 0 failures, 0 errors, 0 skipped

Verification Screenshots (in Docs/qa-v2/audit-content-i18n/):
- Docs/qa-v2/audit-content-i18n/01_history_names.png (History in VI showing fallback names for raw OCR fragments and preserving genuine product names)
- Docs/qa-v2/audit-content-i18n/02_history_en.png (History in EN mode with 'Untitled label' and dynamically localized English summaries)
- Docs/qa-v2/audit-content-i18n/03_compare_vi.png (Compare table in VI mode with localized nutrient names: Năng lượng, Đường, Natri, Chất béo bão hòa, Chất đạm, Chất xơ)
- Docs/qa-v2/audit-content-i18n/04_detail_unknown.png (Ingredient detail for unknown ingredient: definition fallback only, 'Ai nên hạn chế' section completely hidden)
- Docs/qa-v2/audit-content-i18n/05_onboarding_p2.png (Onboarding page 2 displaying clean quotes without backslashes)
