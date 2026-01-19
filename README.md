<!-- Shields from shields.io -->
[![][shield-release]][latest-release]
[![GitHub Downloads](https://img.shields.io/github/downloads/brittytino/t_launcher/total?style=flat&label=Downloads)][latest-release]
[![Android CI](https://github.com/brittytino/t_launcher/actions/workflows/android.yml/badge.svg)](https://github.com/brittytino/t_launcher/actions/workflows/android.yml)
[![][shield-license]][license]
[![GitHub Stars](https://img.shields.io/github/stars/brittytino/t_launcher?style=flat)][repo]

# T_Launcher

**Minimal. Efficient. Unapologetic.**

T_Launcher is a sophisticated Android home screen designed to eliminate distractions and enforce productivity. It is strict by design, prioritizing user focus over endless customization.

> **T Launcher is a continuation and significant rework of the unmaintained project originally created by Finn Glas. While inspired by the original architecture, the current codebase introduces substantial changes in design, enforcement logic, and user experience.**

<p align=center>
  <img src=docs/screenshots/1.jpeg alt=Home Screen width=23%>
  <img src=docs/screenshots/3.jpeg alt=App Drawer width=23%>
  <img src=docs/screenshots/2.jpeg alt=Settings width=23%>
  <img src=docs/screenshots/4.jpeg alt=Focus Mode width=23%>
</p>
<p align=center>
  <img src=docs/screenshots/5.jpeg alt=Dev Panel width=23%>
  <img src=docs/screenshots/6.jpeg alt=Launch Delay width=23%>
</p>

## Philosophy

T_Launcher is not built to satisfy every user request. It is a tool with a specific opinion on how a smartphone should be used: **as a utility, not a slot machine.**

*   **Strict by Design:** Features that encourage mindless scrolling or clutter are intentionally omitted.
*   **Focus First:** Every design decision is measured against its ability to keep the user focused.
*   **Utility over Vanity:** Aesthetics serve the function of readability and calm, not decoration.

**For a deeper dive into the "why" behind T_Launcher, read the official developer's perspective: [T_Launcher Blog Post](https://www.tinobritty.me/blog/t-launcher)**

## Project Status: Phases

We follow an Evolutionary Agile approach.

*    **Phase 1: Core Stability** (App launch pipeline, Focus Mode enforcement, State persistence)
*    **Phase 2: UX Hardening** (We are here - Navigation correctness, Lock states, Error flows)
*    **Phase 3: Opinion Freeze** (Locking down the feature set)
*    **Phase 4: Contributor-Friendly** (Opening up for broader community maintenance)

## Key Features

*   **Focus Enforcement:**
    *   **Focus Mode:** Strict 15-minute cooldowns, blocked apps, and intentional friction.
    *   **Launch Delays:** Customizable pauses (3-15s) before opening problematic apps.
*   **Dynamic Aesthetics:**
    *   **Motivational Engine:** Daily high-contrast wallpapers with productivity-focused quotes.
    *   **Adaptive Theme:** Seamless Light/Dark mode integration across the entire UI.
*   **Developer Tools:**
    *   **LeetCode Integration:** Built-in dashboard to track daily coding consistency.
    *   **Heatmap & Stats:** Visual progress tracking directly on your home screen.
*   **Minimalist Interface:**
    *   **Timebomber Clock:** High-visibility time awareness.
    *   **Text-Only Home:** No icons, no widgets, just the time and your agenda.
    *   **Universal Search:** Instant app access via gestures.
*   **Privacy:** 100% Open Source, Zero Trackers, Offline-first.

## Download

### Direct Download (Recommended)

<a href=https://github.com/brittytino/t_launcher/releases/latest>
  <img src=https://img.shields.io/badge/Download%20APK-Latest%20Release-success?style=for-the-badge&logo=android alt=Download APK height=60>
</a>

**Requirements:** Android 6.0 (API 23) or higher.

## Documentation

Comprehensive documentation can be found in the [docs/](docs/) directory.

*   [Installation Guide](INSTALL.md)
*   [Build Instructions](docs/build.md)
*   [Changes from Original](docs/changes-fork.md)

## Contributing

We welcome contributions that align with the project's philosophy. Please read the [Contributing Guidelines](CONTRIBUTING.md) **before** opening an issue or PR to understand our strict feature policy.

## License

Distributed under the MIT License. See LICENSE for more information.

---

  [repo]: https://github.com/brittytino/t_launcher
  [toolate]: https://toolate.othing.xyz/projects/brittytino-launcher/
  [latest-release]: https://github.com/brittytino/t_launcher/releases/latest
  [shield-release]: https://img.shields.io/github/v/release/brittytino/t_launcher?style=flat
  [shield-license]: https://img.shields.io/badge/license-MIT-007ec6?style=flat
  [license]: https://github.com/brittytino/t_launcher/blob/main/LICENSE

