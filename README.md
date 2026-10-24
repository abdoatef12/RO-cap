# ROSTO Editor Android (prototype)

A native Android starter project using Kotlin and AndroidX Media3. It currently supports selecting and previewing a video and exporting a 9:16 cropped MP4. It is a prototype, not a CapCut replacement yet.

## Build an APK with GitHub Actions
1. Upload the contents of this project folder into your `RO-cap` repository (not just this ZIP).
2. Open the repository's **Actions** tab.
3. Select **Build Android APK** and press **Run workflow** (or wait for the workflow to run after a push).
4. Open the completed run and download the `ROSTO-Editor-debug-apk` artifact.
5. Extract the downloaded artifact ZIP; inside is `app-debug.apk`.
6. Transfer the APK to Android and install it. Android may ask you to allow installs from the browser/file manager you used.

## Current limits
- This prototype is not a full timeline editor.
- No trim handles, multi-clip timeline, subtitles, music mixing, transitions, or AI scene analysis yet.
- Debug APK is unsigned for Play Store distribution, but Android can install it after user approval.
- Build success must be confirmed in GitHub Actions before treating an APK as available.
