muse_task_id: labellens-20261028-force-light
requested_by: Muse
status: done
report: AGY_REPORT_labellens-20261028-force-light.md
finished_at: 2026-10-29T06:32:00+07:00
branch: feature/v2-proof
commit: 19a00be8dfb174d84407bc0d904a17aeb0bfa316

summary:
Forced light theme app-wide across Compose, Android platform, and iOS to resolve broken appearance on dark-mode devices. Pinned light color schemes, system bars, window backgrounds, and normalized palette colors to DESIGN.md (#F7F8F5 background, #16794C primary). Verified on emulator-5554 under night mode (captured 01_home_nightmode.png and 02_result_nightmode.png), passed spotlessCheck, assembleDebug (96,581,230 bytes), compileKotlinIosSimulatorArm64, jvmTest (27 suites, 160 tests, 0 failures), and testAndroidHostTest (23 suites, 148 tests, 0 failures).
