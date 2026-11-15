MUSE_TASK_ID: labellens-20261115-icon-raccoon
REQUESTED_BY: Muse

# Task: replace the LabelLens launcher icon with the approved raccoon artwork

The user approved a temporary new app icon (raccoon mascot holding a product packet, with a camera scan frame drawn around the whole icon). All icon assets are already produced and pixel-final. Your job is ONLY to wire them into the project, build, and report. Do not redesign, redraw, resize differently, or touch any app code.

## Assets
Unzip `/Volumes/SSDMacMini/muse-orca-bridge/icon-assets.zip` into a scratch folder. It contains:
- `icon-assets/android/ic_launcher_<density>.png` and `ic_launcher_round_<density>.png` for densities mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
- `icon-assets/android/ic_launcher_foreground_<density>.png` for the same densities (adaptive-icon foreground, artwork inset on transparency)
- `icon-assets/ios/<size>.png` for every size present in the iOS AppIcon set (20, 29, 40, 50, 57, 58, 60, 72, 76, 80, 87, 100, 114, 120, 144, 152, 167, 180, 1024)

## Steps
1. Create branch `feature/icon-raccoon` from `main` (HEAD 3fa2962) and work on it.
2. Android, under `MobileApp/androidApp/src/main/res/`:
   - Copy each `ic_launcher_<density>.png` to `mipmap-<density>/ic_launcher.png` and DELETE `mipmap-<density>/ic_launcher.webp`.
   - Copy each `ic_launcher_round_<density>.png` to `mipmap-<density>/ic_launcher_round.png` and DELETE the matching `.webp`.
   - Copy each `ic_launcher_foreground_<density>.png` to `mipmap-<density>/ic_launcher_foreground.png` and DELETE the matching `.webp`.
   - Find the color resource `ic_launcher_background` and set its value to `#02646A`.
   - In BOTH `mipmap-anydpi-v26/ic_launcher.xml` and `mipmap-anydpi-v26/ic_launcher_round.xml`, remove the `<monochrome ... />` element (the old monochrome vector is the retired starter arrow and must not ship). Delete `drawable/ic_launcher_monochrome.xml` if nothing else references it.
3. iOS: copy every `icon-assets/ios/<size>.png` over the same filename in `MobileApp/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`. Do NOT modify `Contents.json`.
4. Build: `cd MobileApp && ./gradlew :androidApp:assembleDebug --console=plain`. The build MUST succeed; if a duplicate-resource or missing-resource error appears, fix only icon-resource wiring (names/references), nothing else.
5. Commit everything on `feature/icon-raccoon` with message `feat(icon): raccoon scan-frame launcher icon (temporary)`. Do NOT merge, do NOT push.
6. Write the DONE file (mandatory, in every outcome — success, partial, or blocked) to `Docs/agy-archive/AGY_DONE_labellens-20261115-icon-raccoon.md` containing: branch name, commit hash, `git diff --stat main..HEAD` summary, build result line, and the debug APK absolute path + size in bytes + sha256.

## Hard limits
- Icon resources only: mipmap/drawable/values icon files, the two anydpi XMLs, and the iOS AppIcon.appiconset PNGs. No Kotlin, Gradle, manifest (other than nothing), or backend changes.
- Never read, print, or modify: google-services.json, GoogleService-Info.plist, firebase_options.dart, .firebaserc, firebase.json, local.properties.
- Do NOT install the APK on any device or emulator.
- If anything is ambiguous, stop, write the DONE file marked BLOCKED with the exact question, and end.
