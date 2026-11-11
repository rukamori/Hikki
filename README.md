<a id="readme-top"></a>

<div align="center">

<img src="https://raw.githubusercontent.com/rukamori/Hikki/refs/heads/main/assets/app_icon.png" alt="Hikki app icon" height="150" />

# Hikki · 筆記

**A cute, rich and private Markdown notes app for Android — built with Material 3 Expressive.**

<p>
  <a href="https://github.com/rukamori/Hikki/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/rukamori/hikki?style=for-the-badge&logo=github&color=f5a6bd" /></a>
  <a href="https://github.com/rukamori/Hikki/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/rukamori/hikki/total?style=for-the-badge&logo=github&color=f5a6bd" /></a>
  <a href="LICENSE"><img alt="License: GPL-3.0" src="https://img.shields.io/github/license/rukamori/hikki?style=for-the-badge&color=f5a6bd" /></a>
</p>
<p>
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-f5a6bd?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-100%25-f5a6bd?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-f5a6bd?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
</p>

<a href="https://github.com/rukamori/Hikki/releases/latest">
  <img src="https://raw.githubusercontent.com/rukamori/Hikki/refs/heads/main/assets/badge_github.png" alt="Get it on GitHub" height="56" />
</a>

<p>
  <a href="https://github.com/rukamori/Hikki/releases/latest"><b>Download</b></a>
  ·
  <a href="https://github.com/rukamori/Hikki/issues/new"><b>Report a bug</b></a>
  ·
  <a href="https://github.com/rukamori/Hikki/issues/new"><b>Request a feature</b></a>
</p>

<img src="https://raw.githubusercontent.com/rukamori/Hikki/refs/heads/main/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot_1.jpg" alt="Hikki notes screen" width="30%" />
<img src="https://raw.githubusercontent.com/rukamori/Hikki/refs/heads/main/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot_2.jpg" alt="Hikki editor screen" width="30%" />
<img src="https://raw.githubusercontent.com/rukamori/Hikki/refs/heads/main/fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot_3.jpg" alt="Hikki preview screen" width="30%" />

</div>

---

## 🌸 About

Hikki (筆記, *"notes"*) is an offline-first notes app that keeps everything on your device. Live preview the result as you type, organize notes with folders, categories and colorful tags — all wrapped in a Hikki!.

There are no accounts, no sync servers and no ads. Your notes live in a local database on your phone.

## 📦 Download

Grab the latest signed APK from **[GitHub Releases](https://github.com/rukamori/Hikki/releases/latest)**.

| Requirement | Version |
| --- | --- |
| Android | 8.0 (API 26) or newer |
| Target SDK | 37 |

## 🛠️ Build from source

**Prerequisites**

- Android Studio with Android SDK 37
- JDK 21

**Steps**

1. Clone the repository.

   ```bash
   git clone https://github.com/rukamori/Hikki.git
   cd Hikki
   ```

2. Build and install a debug build on a connected device.

   ```bash
   ./gradlew installDebug
   ```

   Debug builds use the `moe.rukamori.hikki.debug` application ID, so they install alongside a release build.

3. Or build a release APK. It is minified, so you need your own signing configuration.

   ```bash
   ./gradlew assembleRelease
   ```

## 🤝 Contributing

Contributions, bug reports and ideas are welcome.

1. Fork the project.
2. Create a branch: `git checkout -b feature/your-feature`.
3. Commit your changes: `git commit -m "feat: add your feature"`.
4. Push the branch: `git push origin feature/your-feature`.
5. Open a pull request.

## ❤️ Support

If Hikki makes your day a little cuter, you can support development:

- [GitHub Sponsors](https://github.com/sponsors/rukamori)
- [Buy Me a Coffee](https://buymeacoffee.com/morieattonkatsu)
- [Donate](https://miuka.moe/donate)

Starring the repository helps too ⭐

## 💬 Contact

- Telegram: [@rukamori](https://t.me/rukamori)
- Email: [me@koiiverse.cloud](mailto:me@koiiverse.cloud)

## ⚖️ License

Copyright © 2026 rukamori / morrieeattonkatsu

Hikki is free software: you can redistribute it and/or modify it under the terms of the **GNU General Public License v3.0** as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but **without any warranty**; without even the implied warranty of merchantability or fitness for a particular purpose. See [`LICENSE`](LICENSE) for the full text.

<p align="right">(<a href="#readme-top">back to top</a>)</p>
