<p align="center">
  <img src="docs/favicon.svg" width="80" alt="dotBox logo">
</p>

<h1 align="center">dotBox</h1>

<p align="center">
  <strong>55 tools. One app. Zero clutter.</strong><br>
  A minimalist Android toolkit built with Jetpack Compose.
</p>

<p align="center">
  <a href="https://paksulab.github.io/dotbox/">Website</a> &bull;
  <a href="https://paksulab.github.io/dotbox/privacy.html">Privacy Policy</a>
</p>

---

## About

dotBox is a utility toolkit app that puts 55+ tools in your pocket — from calculators and converters to timers, fitness tools, and scanners. Everything runs locally on your device with no accounts, no tracking, and no ads.

Designed with a Nothing-inspired dark aesthetic using JetBrains Mono typography and a monochrome + red accent palette.

## Categories

| Category | Tools | Examples |
|----------|-------|---------|
| **Utility** | 12 | Flashlight, Stopwatch, Countdown, Pomodoro, Counter, Clipboard |
| **Calculators** | 6 | Scientific Calculator, Tip Splitter, Percentage, Loan, Aspect Ratio, Date Math |
| **Measurement** | 7 | Compass, Level, Ruler, Sound Meter, Speedometer, Magnifier, Light Meter |
| **Converters** | 7 | Unit Converter, Currency, Cooking, Time Zones, Number Base, Clothing, Morse Code |
| **Generators** | 6 | QR Generator, Color Picker, Text Tools, Tone Generator, WiFi QR, Camera Color |
| **Scanners** | 2 | QR Scanner, Document Scanner |
| **Fitness** | 11 | BMI, BMR/TDEE, Heart Rate Zones, Body Fat %, Water Intake, Workout Timer, 1RM |
| **Medical** | 4 | BAC Calculator, Due Date, Dose Calculator, IV Drip Rate |

## Features

- **Dark by default** — Nothing-inspired monochrome design
- **Drag-to-reorder favorites** — pin and arrange your most-used tools
- **Home screen widget** — quick tool launch from your home screen
- **Tablet-ready** — adaptive two-pane layout for tablets and foldables
- **Works offline** — everything except Currency Converter runs without internet
- **Privacy first** — no accounts, no analytics, no data collection

## Tech Stack

- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Repository pattern
- **Database:** Room (favorites persistence)
- **Preferences:** SharedPreferences (centralized via AppPreferences)
- **Camera:** CameraX + ML Kit (barcode scanning, document scanner)
- **QR Generation:** ZXing
- **Location:** Google Play Services
- **Widget:** Glance 1.1.0
- **Min SDK:** 28 (Android 9)
- **Target SDK:** 35

## Building

```bash
# Clone the repo
git clone https://github.com/Paksulab/dotbox.git
cd dotbox

# Build debug APK
./gradlew assembleDebug

# Build release AAB (requires signing config)
./gradlew bundleRelease
```

## Project Structure

```
app/src/main/java/com/dotbox/app/
├── DotBoxApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/          # Room database
│   ├── model/          # Data models (Tool, Category)
│   ├── preferences/    # Centralized SharedPreferences
│   └── repository/     # ToolsRepository
├── ui/
│   ├── components/     # Reusable composables (ToolCard, DotPattern)
│   ├── navigation/     # NavGraph, Screen definitions
│   ├── screens/
│   │   ├── home/       # HomeScreen, HomeViewModel, TwoPaneHomeScreen
│   │   ├── onboarding/ # First-launch onboarding
│   │   ├── settings/   # App settings
│   │   └── tools/      # 55 individual tool screens
│   ├── theme/          # Colors, Typography
│   └── utils/
└── widget/             # Home screen widget (Glance)
```

## License

All rights reserved.
