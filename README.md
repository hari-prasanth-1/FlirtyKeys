# FlirtyKeys – Android MVP

A local-first floating pickup-line companion for Android. Designed to work alongside Gboard without modifying it.

## Features
- Draggable floating heart bubble
- Category-based pickup lines
- Individual copy and multi-select
- Select all and copy selected
- Clipboard workflow: review, paste and send manually

## Build on GitHub
The GitHub Actions workflow builds a debug APK automatically on pushes to `main` and via **Actions → Build FlirtyKeys APK → Run workflow**.

## Install on Redmi K20 Pro
1. Download `app-debug.apk` from the latest successful workflow run's **Artifacts** section.
2. Open the APK on your phone and allow installation from that source if Android asks.
3. Open FlirtyKeys and grant **Display over other apps**.
4. Tap **Start FlirtyKeys floating button**.
5. Open a chat, tap the heart, select lines, copy, then paste with Gboard.

## Current MVP limitations
- Uses a lightweight overlay service; MIUI may stop it under battery pressure.
- Favorites, custom sentences, persistent history and search are not implemented yet.
- No message reading, keylogging, Accessibility Service, or automatic sending.
- This starter should be tested on-device before wider release.
