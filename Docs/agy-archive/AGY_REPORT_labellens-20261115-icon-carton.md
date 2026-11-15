# LabelLens Engineering Report: Carton Launcher Icon Replacement

**Task ID**: `labellens-20261115-icon-carton` (Requested by Muse)  
**Date**: 2026-11-15T08:02:00+07:00  
**Branch**: `feature/icon-carton`  
**Status**: DONE  
**Base Commit**: `3fa296280d9f9e21871c2a4159836d0ad285ceb6`  
**Commit**: `8c34185b29a7db26ee1273af8aa4fdbf128ea43f`  
**Commit Message**: `feat(icon): carton scan-frame launcher icon`  

---

## 1. Summary of Changes

The approved orange carton character launcher icon artwork (scan frame baked in) has been integrated into both Android and iOS launcher asset sets:

### Android (`MobileApp/androidApp/src/main/res/`):
- Copied `ic_launcher_<density>.png`, `ic_launcher_round_<density>.png`, and `ic_launcher_foreground_<density>.png` across all densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) into their respective `mipmap-<density>/` directories.
- Deleted all obsolete legacy `.webp` icon files (`ic_launcher.webp`, `ic_launcher_round.webp`, `ic_launcher_foreground.webp`) across all 5 density folders.
- Set `ic_launcher_background` color resource in `values/colors.xml` to `#05532F`.
- Removed `<monochrome>` element from both `mipmap-anydpi-v26/ic_launcher.xml` and `mipmap-anydpi-v26/ic_launcher_round.xml`.
- Deleted `drawable/ic_launcher_monochrome.xml`.

### iOS (`MobileApp/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`):
- Replaced 19 PNG icon assets across all required dimensions (20, 29, 40, 50, 57, 58, 60, 72, 76, 80, 87, 100, 114, 120, 144, 152, 167, 180, 1024 px).
- Kept `Contents.json` unmodified.

---

## 2. Git Diff Stat (`main..HEAD`)

```
 .../main/res/drawable/ic_launcher_monochrome.xml   |  14 --------------
 .../src/main/res/mipmap-anydpi-v26/ic_launcher.xml |   1 -
 .../res/mipmap-anydpi-v26/ic_launcher_round.xml    |   1 -
 .../src/main/res/mipmap-hdpi/ic_launcher.png       | Bin 0 -> 7398 bytes
 .../src/main/res/mipmap-hdpi/ic_launcher.webp      | Bin 1538 -> 0 bytes
 .../res/mipmap-hdpi/ic_launcher_foreground.png     | Bin 0 -> 22739 bytes
 .../res/mipmap-hdpi/ic_launcher_foreground.webp    | Bin 2292 -> 0 bytes
 .../src/main/res/mipmap-hdpi/ic_launcher_round.png | Bin 0 -> 8305 bytes
 .../main/res/mipmap-hdpi/ic_launcher_round.webp    | Bin 3172 -> 0 bytes
 .../src/main/res/mipmap-mdpi/ic_launcher.png       | Bin 0 -> 4067 bytes
 .../src/main/res/mipmap-mdpi/ic_launcher.webp      | Bin 1050 -> 0 bytes
 .../res/mipmap-mdpi/ic_launcher_foreground.png     | Bin 0 -> 12037 bytes
 .../res/mipmap-mdpi/ic_launcher_foreground.webp    | Bin 1214 -> 0 bytes
 .../src/main/res/mipmap-mdpi/ic_launcher_round.png | Bin 0 -> 4548 bytes
 .../main/res/mipmap-mdpi/ic_launcher_round.webp    | Bin 1914 -> 0 bytes
 .../src/main/res/mipmap-xhdpi/ic_launcher.png      | Bin 0 -> 11324 bytes
 .../src/main/res/mipmap-xhdpi/ic_launcher.webp     | Bin 1936 -> 0 bytes
 .../res/mipmap-xhdpi/ic_launcher_foreground.png    | Bin 0 -> 37518 bytes
 .../res/mipmap-xhdpi/ic_launcher_foreground.webp   | Bin 3504 -> 0 bytes
 .../main/res/mipmap-xhdpi/ic_launcher_round.png    | Bin 0 -> 12747 bytes
 .../main/res/mipmap-xhdpi/ic_launcher_round.webp   | Bin 4216 -> 0 bytes
 .../src/main/res/mipmap-xxhdpi/ic_launcher.png     | Bin 0 -> 21807 bytes
 .../src/main/res/mipmap-xxhdpi/ic_launcher.webp    | Bin 3032 -> 0 bytes
 .../res/mipmap-xxhdpi/ic_launcher_foreground.png   | Bin 0 -> 80566 bytes
 .../res/mipmap-xxhdpi/ic_launcher_foreground.webp  | Bin 7692 -> 0 bytes
 .../main/res/mipmap-xxhdpi/ic_launcher_round.png   | Bin 0 -> 24579 bytes
 .../main/res/mipmap-xxhdpi/ic_launcher_round.webp  | Bin 6816 -> 0 bytes
 .../src/main/res/mipmap-xxxhdpi/ic_launcher.png    | Bin 0 -> 35767 bytes
 .../src/main/res/mipmap-xxxhdpi/ic_launcher.webp   | Bin 4284 -> 0 bytes
 .../res/mipmap-xxxhdpi/ic_launcher_foreground.png  | Bin 0 -> 142283 bytes
 .../res/mipmap-xxxhdpi/ic_launcher_foreground.webp | Bin 13376 -> 0 bytes
 .../main/res/mipmap-xxxhdpi/ic_launcher_round.png  | Bin 0 -> 40469 bytes
 .../main/res/mipmap-xxxhdpi/ic_launcher_round.webp | Bin 9436 -> 0 bytes
 .../androidApp/src/main/res/values/colors.xml      |   2 +-
 .../Assets.xcassets/AppIcon.appiconset/100.png     | Bin 8600 -> 12108 bytes
 .../Assets.xcassets/AppIcon.appiconset/1024.png    | Bin 393308 -> 773895 bytes
 .../Assets.xcassets/AppIcon.appiconset/114.png     | Bin 11350 -> 14857 bytes
 .../Assets.xcassets/AppIcon.appiconset/120.png     | Bin 12682 -> 16114 bytes
 .../Assets.xcassets/AppIcon.appiconset/144.png     | Bin 18649 -> 21807 bytes
 .../Assets.xcassets/AppIcon.appiconset/152.png     | Bin 20511 -> 23774 bytes
 .../Assets.xcassets/AppIcon.appiconset/167.png     | Bin 24500 -> 27927 bytes
 .../Assets.xcassets/AppIcon.appiconset/180.png     | Bin 28151 -> 31698 bytes
 .../Assets.xcassets/AppIcon.appiconset/20.png      | Bin 741 -> 1097 bytes
 .../Assets.xcassets/AppIcon.appiconset/29.png      | Bin 1115 -> 1917 bytes
 .../Assets.xcassets/AppIcon.appiconset/40.png      | Bin 1735 -> 3061 bytes
 .../Assets.xcassets/AppIcon.appiconset/50.png      | Bin 2469 -> 4326 bytes
 .../Assets.xcassets/AppIcon.appiconset/57.png      | Bin 3062 -> 5216 bytes
 .../Assets.xcassets/AppIcon.appiconset/58.png      | Bin 3141 -> 5325 bytes
 .../Assets.xcassets/AppIcon.appiconset/60.png      | Bin 3354 -> 5706 bytes
 .../Assets.xcassets/AppIcon.appiconset/72.png      | Bin 4607 -> 7398 bytes
 .../Assets.xcassets/AppIcon.appiconset/76.png      | Bin 5040 -> 8009 bytes
 .../Assets.xcassets/AppIcon.appiconset/80.png      | Bin 5605 -> 8627 bytes
 .../Assets.xcassets/AppIcon.appiconset/87.png      | Bin 6465 -> 9839 bytes
 53 files changed, 1 insertion(+), 17 deletions(-)
```

---

## 3. Build & Test Verification

### Android Debug Build
- Command: `cd MobileApp && ./gradlew :androidApp:assembleDebug --console=plain`
- Result: `BUILD SUCCESSFUL in 5s (160 actionable tasks: 14 executed, 146 up-to-date)`
- APK Path: `/Volumes/SSDMacMini/DevMacMini/Mod/LabelLens/MobileApp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`
- APK Size: `98482532` bytes
- APK SHA256: `ea74a4488ffcf7fbeafdc8ecea99774bb92ba1d8fb0af58934830f6a43c9ba5a`

### Spotless Check
- Command: `cd MobileApp && ./gradlew spotlessCheck --console=plain`
- Result: `BUILD SUCCESSFUL in 4s (63 actionable tasks: 63 up-to-date)`

### Unit & Robolectric Test Counts (from XML)
- Command: `cd MobileApp && ./gradlew :shared:jvmTest :shared:testAndroidHostTest --console=plain`
- Result: `BUILD SUCCESSFUL`
- `jvmTest`: 40 suites, 271 tests, 0 failures, 0 errors, 0 skipped
- `testAndroidHostTest`: 35 suites, 258 tests, 0 failures, 0 errors, 0 skipped
- Total: 75 suites, 529 tests, 0 failures, 0 errors, 0 skipped
