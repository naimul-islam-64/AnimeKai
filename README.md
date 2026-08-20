# 🌌 AnimeKai — Streamlined Anime Experience for Android

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="AnimeKai Logo" width="110" height="110" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>A blazing-fast, privacy-first native Android streaming client for AnimeKai with built-in AdShield, Anti-Redirect Engine, Offline Page Vault, and Fullscreen Video Immersion.</b>
</p>

<p align="center">
  <a href="https://github.com/actions"><img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square&logo=github-actions" alt="Build Status" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?style=flat-square&logo=kotlin" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?style=flat-square&logo=android" alt="Jetpack Compose" /></a>
  <a href="https://developer.android.com/about/versions/14"><img src="https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0+)-00C853.svg?style=flat-square" alt="Min SDK" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License" /></a>
</p>

---

## ✨ Key Features

### 🛡️ Ironclad AdShield & Anti-Redirect Engine
- **Zero-Redirect Navigation Boundary**: Intelligently traps and terminates unauthorized third-party redirects, casino portals, betting sites, and malware URLs before page navigation is triggered.
- **Capture-Phase Click Trap Destroyer**: Intercepts DOM click and touch events in the capture phase to eliminate transparent full-screen overlay traps placed over video player play buttons.
- **Anti-Adblock Bypass**: Emulates browser variables (`canRunAds`, `FuckAdBlock`) to prevent streaming video players from freezing or showing anti-adblock alerts.
- **Real-Time Ad Network Filter**: Automatically drops requests to known popup networks, video interstitial injectors, and rogue intent schemes (`market://`, `intent://`, `tg://`).

### 🎬 Immersive Fullscreen Video Player
- **Native Fullscreen Hook**: Seamless transition to true landscape fullscreen when tapping player fullscreen triggers.
- **Orientation Lock & System Bar Auto-Hide**: Keeps the anime stream distraction-free with edge-to-edge controls.

### 💾 10-Page Offline Vault & Custom Fallback
- **Automatic Multi-Page Snapshot Engine**: Automatically saves complete HTML + CSS snapshots of the last 10 visited anime pages.
- **No Internet, No Problem**: Instantly renders the stored offline file version when offline or displays the elegant **AnimeKai Offline Hub** to browse cached series.

### 🎨 Material You 3 Dynamic Interface
- **Midnight Purple Dark Canvas**: Handcrafted OLED-friendly dark palette tailored for nighttime anime watching.
- **Dynamic Theming Support**: Switch seamlessly between Dark, Light, and System Default themes.
- **Quick Action Bottom Sheet**: One-tap access to Whitelist controls, Offline Vault, AdBlock toggle, Cache cleaner, and Quick Search.

---

## 📱 Screenshots & Previews

| Anime Home & Search | AdShield Controller | Offline Page Vault |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="220" alt="Home" /> | <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="220" alt="Shield" /> | <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="220" alt="Offline" /> |

---

## 📦 Download & Release APKs

Pre-built standalone APKs are automatically generated for every release:

| Architecture | APK Variant | Target Devices |
| :--- | :--- | :--- |
| **Universal** | `AnimeKai-v1.0-universal-release.apk` | Works on all Android 7.0+ devices |
| **ARM 64-bit** | `AnimeKai-v1.0-arm64-v8a-release.apk` | Modern smartphones & tablets (Recommended) |
| **ARM 32-bit** | `AnimeKai-v1.0-armeabi-v7a-release.apk` | Older Android phones & TV sticks |
| **x86_64** | `AnimeKai-v1.0-x86_64-release.apk` | Chromebooks, Android Emulators, x86 tablets |

👉 Check the **[Latest GitHub Releases](../../releases/latest)** to download the latest APK for your device.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Engine**: Custom Android `WebView` with customized `WebViewClient` & `WebChromeClient`
- **AdBlock Core**: Kotlin Domain Engine + Injected ES6 Capture-Phase DOM Mutation Shield
- **Architecture**: MVVM (Model-View-ViewModel) + Kotlin StateFlow + Coroutines
- **Testing**: Robolectric Local JVM Tests + Roborazzi Visual Testing

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug | 2024.2+ or higher
- JDK 17 or JDK 21
- Android SDK Platform 36 (minSdk 24)

### Clone & Build
```bash
# 1. Clone the repository
git clone https://github.com/naimulislam0037/AnimeKai.git
cd AnimeKai

# 2. Build debug APK
gradle :app:assembleDebug

# 3. Run all unit tests
gradle :app:testDebugUnitTest

# 4. Build release APKs (Universal & ABI splits)
gradle :app:assembleRelease
```

Generated APKs will be located in:
`app/build/outputs/apk/release/`

---

## ⚙️ Configuration & Signing (CI/CD)

The automated GitHub Action workflow (`release.yml`) builds multi-arch split APKs and releases them automatically when you push a version tag (e.g. `v1.0.0`).

To enable signed releases, add the following GitHub Secrets to your repository:
- `KEYSTORE_BASE64`: Base64 string of your upload keystore file (`.jks` / `.keystore`)
- `STORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password

---

## 🤝 Contributing

Contributions, bug reports, and feature requests are welcome!
1. Fork the repository
2. Create your branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ for Anime Lovers worldwide 🎌
</p>
