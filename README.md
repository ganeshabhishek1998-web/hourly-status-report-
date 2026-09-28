# Hourly Status Report — Android

Native Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room**, and **Retrofit + Kotlinx Serialization** for tracking a 12-hour daily operational work schedule (**9:00 AM – 9:00 PM**).

## Key Features

- **12-Hour Operational Schedule (9:00 AM – 9:00 PM)**:
  - Tracks 13 structured hourly and half-hour slots (`slot_1` through `slot_13`), including a pre-scheduled `1:00 – 1:30 PM` Lunch break and an `8:30 – 9:00 PM` shift wrap-up slot.
  - Automatically calculates total logged hours (`0.5` to `12.0` hours) with a real-time completion progress bar.
- **Live Clock & Active Slot Highlighting**:
  - Displays a live second-by-second clock and highlights the active time slot with a `NOW` badge when viewing today's report.
- **Date Navigation & Local Persistence (Room)**:
  - Navigate across days (`Previous Day`, `Date Picker`, `Next Day`, `Today`) with automatic persistence in a local Room SQLite database.
- **Status Cycling & Quick Templates**:
  - Tap any slot's status pill to cycle between `Completed`, `In Progress`, and `Blocked` (entering an activity automatically transitions `Pending` slots to `Completed`).
  - Apply a full 12-hour sample engineering log or reset the current date's report.
- **Copy, Share, Export CSV & Multi-Format Import**:
  - **Copy Report / Share**: Generates a formatted ASCII table summary for daily standups.
  - **Export CSV**: Saves `hourly_status_report_<YYYY-MM-DD>.csv` via the Android system document picker.
  - **Upload / Import Report**: Supports importing `.csv`, `.json`, or plain-text report files from device storage or pasted text.
- **AI Chat: "Write It Down" Assistant**:
  - Natural-language assistant powered by the Gemini REST API (`gemini-3.5-flash`) with an offline/no-key deterministic fallback.
  - Dictate or tap quick chips (e.g., `"I'm just saying, write it down."`, `"It's a machine"`, `"Morning standup"`) to log activities directly into the active or specified time slot.

## Secrets Configuration

To enable live Gemini API responses in the **AI Chat: Write It Down** sheet, configure `GEMINI_API_KEY` in the **Secrets panel in AI Studio** (wired via `.env` / `.env.example` and the Secrets Gradle Plugin into `BuildConfig.GEMINI_API_KEY`).
