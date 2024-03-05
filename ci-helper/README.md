# ci-helper

Build scripts used in CI/CD workflows to help uploading artifacts to GitHub releases.

It registers Gradle tasks:

- `uploadAndroidApk`
- `uploadAndroidApkQR`

In CI workflows, `./gradlew :ci-helper:uploadAndroidApk` is executed to upload the Android APK to
the release draft.