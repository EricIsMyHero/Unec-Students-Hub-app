# UNEC Students Hub (Android)

Native Android application for **UNEC Students Hub**, built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, **DataStore**, and **Navigation Compose**.

## Core Features
- **Course Materials & Subject Catalog**: Browse PDFs, study resources, and extra materials across Courses 1–4 and UNEC majors, filter by format/type/major, favorite items, rate materials, add study notes, open/download PDFs, and report issues.
- **Practice**: Opens the website's practice screen in the browser, where sign-in, results, written practice and daily limits are managed by the website. The native prototype's local quiz implementation is retained in the codebase but is not exposed as the main practice flow.
- **GPA Calculator & Predictor**: UNEC earned-credit (`score >= 51`) GPA calculation with animated ring indicator, snapshot history comparison, target GPA prediction, and sharing.
- **Curriculum (Major Plan)**: 8-semester study plans across 15 UNEC majors with credit completion tracking, subject codes toggle, absence limits, and direct links to matching materials.
- **Student Compass**: Bilingual (AZ/EN) guidance articles for students and applicants with category filters, search, bookmarks, collapsible table of contents, tables, action checklists, and official source links.
- **Pomodoro Timer & Exam Countdowns**: Timestamp-accurate Focus/Break Pomodoro intervals with optional completion notifications, plus exam date countdowns.
- **Dashboard & Material Requests**: Local XP rank progression, profile and a device-only request notebook. Use the link in Material Requests to post to the shared website board.
- **Themes & Localization**: Dynamic Azerbaijani / English switching and 11 color themes (`UNEC Blue`, `Arctic`, `Candy`, `Slate`, `Rainbow`, `Sakura`, `Neon`, `Forest`, `Midnight`, `Solar`, `Rose`).
- **Live Updates & Support**: Live Telegram news feed integration, Birbank and m10 support links with QR code previews.

## Data and account limitations

The app includes an offline snapshot of the public material catalog, curricula and Student Compass. On launch it checks `https://unecstudentshub.com/data/mobile-catalog.json` in the background and applies a valid downloaded snapshot on the next cold start. If the download fails, the last valid snapshot or the bundled data remains available.

The native app does **not** currently sign in to Firebase or synchronize profiles, favorites, ratings, requests, quiz history, or progress with the website. These native records stay on the device. Website practice and shared requests open in the browser and use the website account and its existing rules. Do not present this app as having native cloud synchronization or publish it as a production release without completing and testing those integrations.

## Build and test

Install JDK 21, Android SDK Platform 36 and Build Tools 36, then run:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

On Windows, run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` instead. The debug APK is at `app/build/outputs/apk/debug/app-debug.apk`. It is signed with the machine's local debug key and is for testing, not distribution.

Release builds are unsigned by default. To sign a release with your privately managed upload key, set `UNEC_RELEASE_KEYSTORE`, `UNEC_RELEASE_STORE_PASSWORD`, `UNEC_RELEASE_KEY_ALIAS` and `UNEC_RELEASE_KEY_PASSWORD` in the build environment. Keep the keystore and passwords outside the repository and back them up securely. Register the intended Android package and signing certificate in Firebase before implementing native Google sign-in.
