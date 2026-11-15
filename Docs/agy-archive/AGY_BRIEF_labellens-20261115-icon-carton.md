MUSE_TASK_ID: labellens-20261115-icon-carton
REQUESTED_BY: Muse

# Task: replace the LabelLens launcher icon with the approved carton artwork

The user changed the icon choice: use the orange carton character artwork (scan frame already baked into the art) INSTEAD of the raccoon. All assets are produced and pixel-final. Your job is ONLY to wire them into the project from `main`, build, and report. Do not redesign or touch app code. (The earlier branch `feature/icon-raccoon` is superseded — ignore it completely; do not base work on it and do not merge it.)

## Assets
Unzip `/Volumes/SSDMacMini/muse-orca-bridge/icon-assets-carton.zip` into a scratch folder. It contains:
- `icon-assets-carton/android/ic_launcher_<density>.png` and `ic_launcher_round_<density>.png` for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
- `icon-assets-carton/android/ic_launcher_foreground_<density>.png` for the same densities
- `icon-assets-carton/ios/<size>.png` for 20, 29, 40, 50, 57, 58, 60, 72, 76, 80, 87, 100, 114, 120, 144, 152, 167, 180, 1024

## Steps
1. Create branch `feature/icon-carton` from `main` (HEAD 3fa2962) and work on it.
2. Android, under `MobileApp/androidApp/src/main/res/`:
   - Copy each `ic_launcher_<density>.png` to `mipmap-<density>/ic_launcher.png` and DELETE `mipmap-<density>/ic_launcher.webp`.
   - Copy each `ic_launcher_round_<density>.png` to `mipmap-<density>/ic_launcher_round.png` and DELETE the matching `.webp`.
   - Copy each `ic_launcher_foreground_<density>.png` to `mipmap-<density>/ic_launcher_foreground.png` and DELETE the matching `.webp`.
   - Set the color resource `ic_launcher_background` to `#05532F`.
   - In BOTH `mipmap-anydpi-v26/ic_launcher.xml` and `mipmap-anydpi-v26/ic_launcher_round.xml`, remove the `<monochrome ... />` element. Delete `drawable/ic_launcher_monochrome.xml` if nothing else references it.
3. iOS: copy every `icon-assets-carton/ios/<size>.png` over the same filename in `MobileApp/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`. Do NOT modify `Contents.json`.
4. Build: `cd MobileApp && ./gradlew :androidApp:assembleDebug --console=plain`. Must succeed; fix only icon-resource wiring if a resource error appears.
5. Also run `./gradlew jvmTest testAndroidHostTest --console=plain` and report exact test counts from the JUnit XML files.
6. Commit on `feature/icon-carton` with message `feat(icon): carton scan-frame launcher icon`. Do NOT merge, do NOT push.
7. Write the DONE file (mandatory in every outcome) to `Docs/agy-archive/AGY_DONE_labellens-20261115-icon-carton.md` with: branch, commit hash, `git diff --stat main..HEAD` summary, build result, exact test counts, debug APK absolute path + size bytes + sha256.

## Hard limits
- Icon resources only. No Kotlin/Gradle/manifest/backend changes.
- Never read/print/modify: google-services.json, GoogleService-Info.plist, firebase_options.dart, .firebaserc, firebase.json, local.properties.
- Do NOT install the APK anywhere.
- If ambiguous, stop, write DONE marked BLOCKED with the exact question, and end.
