# Repository Guidelines

## Project Structure & Module Organization
This repository contains a single Android application module under `HookVIP/`. The main hook entry points live in `HookVIP/app/src/main/java/com/hook/vip`, with target-specific implementations in `.../app/` and shared loader utilities in `.../util/`. Android resources are under `HookVIP/app/src/main/res`, Xposed metadata is in `HookVIP/app/src/main/assets/xposed_init`, and local Xposed APIs are stored in `HookVIP/app/libs/`. Unit tests live in `HookVIP/app/src/test`, and instrumented tests live in `HookVIP/app/src/androidTest`.

## Build, Test, and Development Commands
Run commands from `HookVIP/`.

- `./gradlew assembleDebug`: build a debug APK for local verification.
- `./gradlew assembleRelease`: produce the shrinked release APK using ProGuard and resource shrinking.
- `./gradlew testDebugUnitTest`: run JVM unit tests in `src/test`.
- `./gradlew connectedDebugAndroidTest`: run instrumented tests on a connected device or emulator.
- `./gradlew lint`: check Android lint issues before opening a PR.

## Coding Style & Naming Conventions
The codebase uses Java 11 with standard Android Gradle Plugin conventions. Follow 4-space indentation, keep braces on the same line, and prefer short, direct method bodies. Name Xposed entry and hook classes by responsibility, for example `HookEntry`, `WeightAppHooker`, and `UniversalRealClassLoaderUtil`. Keep package names lowercase and resource names snake_case such as `activity_main.xml`. Reuse the existing `Log.d` and `Log.e` pattern for hook diagnostics.

## Testing Guidelines
JUnit 4 is configured for local unit tests, and AndroidX JUnit plus Espresso are available for device tests. Add unit tests for pure Java helpers and instrumentation tests for behavior that depends on Android or Xposed integration points. Name test classes after the target class, for example `HookEntryTest` or `WeightAppHookerInstrumentedTest`. If a hook change cannot be fully automated, document the tested app version in the PR.

## Commit & Pull Request Guidelines
Recent history uses short Chinese commit messages such as `更新README` and `更新代码`. Keep commit subjects brief, imperative, and scoped to one change. Pull requests should describe the target app or hook flow affected, list tested package versions, and include screenshots or Logcat snippets when UI or runtime behavior changes. Link related issues when available.

## Security & Configuration Tips
This project targets Xposed-based runtime modification. Do not commit third-party APKs, secrets, or private test data. Treat supported package names and version notes in `README.md` as user-facing compatibility data and update them when adding or changing hook behavior.
