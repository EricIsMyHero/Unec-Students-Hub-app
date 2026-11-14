# UNEC Students Hub (Android)

Native Android application for **UNEC Students Hub**, built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, **DataStore**, and **Navigation Compose**.

## Core Features
- **Course Materials & Subject Catalog**: Browse PDFs, study resources, and extra materials across Courses 1–4 and UNEC majors, filter by format/type/major, favorite items, rate materials, add study notes, open/download PDFs, and report issues.
- **Automated Test System**: Extract and merge multiple-choice questions directly from UNEC e-test PDF banks or imported local PDF files via `OpenDocument`, run timed randomized quizzes with shuffled options, and review results.
- **GPA Calculator & Predictor**: UNEC earned-credit (`score >= 51`) GPA calculation with animated ring indicator, snapshot history comparison, target GPA prediction, and sharing.
- **Curriculum (Major Plan)**: 8-semester study plans across 15 UNEC majors with credit completion tracking, subject codes toggle, absence limits, and direct links to matching materials.
- **Student Compass**: Bilingual (AZ/EN) guidance articles for students and applicants with category filters, search, bookmarks, collapsible table of contents, tables, action checklists, and official source links.
- **Pomodoro Timer & Exam Countdowns**: Timestamp-accurate Focus/Break Pomodoro intervals with optional completion notifications, plus exam date countdowns.
- **Dashboard & Material Requests**: Local XP rank progression (`Freshman` to `Professor`), daily streak tracker, 14 unlockable badges, quiz history, and material request board with reply threads.
- **Themes & Localization**: Dynamic Azerbaijani / English switching and 11 color themes (`UNEC Blue`, `Arctic`, `Candy`, `Slate`, `Rainbow`, `Sakura`, `Neon`, `Forest`, `Midnight`, `Solar`, `Rose`).
- **Live Updates & Support**: Live Telegram news feed integration, Birbank and m10 support links with QR code previews.

## Build
Built with Gradle Kotlin DSL (`compileSdk = 36`, `minSdk = 26`, Java 21, AGP 9.1.1).
