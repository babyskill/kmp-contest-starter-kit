MUSE_TASK_ID: labellens-20261115-icon-round
REQUESTED_BY: Muse

# Task: replace the LabelLens ROUND launcher icon with the approved native circular design

The user approved a purpose-designed circular icon (orange carton on a green disc, scan arcs curved for the circle). Only the round icon files change. Assets are produced and pixel-final.

## Assets
Unzip `/Volumes/SSDMacMini/muse-orca-bridge/icon-assets-round.zip` into a scratch folder. It contains `icon-assets-round/ic_launcher_round_<density>.png` for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi.

## Steps
1. Create branch `feature/icon-carton-round` from `main` (HEAD 15f0f62) and work on it.
2. Copy each `ic_launcher_round_<density>.png` over `MobileApp/androidApp/src/main/res/mipmap-<density>/ic_launcher_round.png`. Change NOTHING else: not ic_launcher.png, not the foregrounds, not colors.xml, not the anydpi XMLs, no iOS files.
3. Build: `cd MobileApp && ./gradlew :androidApp:assembleDebug --console=plain`. Must succeed.
4. Run `./gradlew jvmTest testAndroidHostTest --console=plain` and report exact test counts from the JUnit XML files.
5. Commit on `feature/icon-carton-round` with message `feat(icon): native circular round icon design`. Do NOT merge, do NOT push.
6. Write the DONE file (mandatory in every outcome) to `Docs/agy-archive/AGY_DONE_labellens-20261115-icon-round.md` with: branch, commit hash, `git diff --stat main..HEAD` summary, build result, exact test counts, debug APK absolute path + size bytes + sha256.

## Hard limits
- Round icon resources only (5 PNG files). No code, manifest, Gradle or other resource changes.
- Never read/print/modify: google-services.json, GoogleService-Info.plist, firebase_options.dart, .firebaserc, firebase.json, local.properties.
- Do NOT install the APK anywhere.
- If ambiguous, stop, write DONE marked BLOCKED with the exact question, and end.
