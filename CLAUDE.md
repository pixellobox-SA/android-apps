# android-apps

Personal Android apps for Jacques's Galaxy S25 FE (SM-S731B/DS, Android 17 / One UI 9, two Telkom SIMs).
Apps are sideloaded, not published to Play.

## Build

- JDK 21, Android SDK at `/opt/android-sdk` (cloud environment setup script installs it), Gradle wrapper 9.9.1, AGP 9.5.1.
- `./gradlew assembleDebug testDebugUnitTest`. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.
- Maven Central sometimes answers 429 (rate limit): wait and retry.
- No emulator here (no KVM). Verify with unit tests; the person checks on the phone.

## Stack

Kotlin, Jetpack Compose, Material 3 **1.5** (Expressive APIs: `MaterialExpressiveTheme`, `MotionScheme.expressive()`,
`LoadingIndicator`), dynamic colour with a Pixello pink/violet fallback. `minSdk 31`, `targetSdk 37`.
Versions live in `gradle/libs.versions.toml`.

## Rules

- Never commit secrets, tokens or personal identifiers (phone numbers, IMEIs, serials).
  Tokens are entered by the person in the app's own settings.
- `keystore/debug.keystore` is a shared **debug** key so local and CI builds install over each other. It is not a release key.
- Home-network services are reached only over Tailscale using full MagicDNS names under `taild9be91.ts.net`.
  Never expose anything publicly (no Tailscale Funnel).
- Machines: `homeserver` (Home Assistant :8123, Music Assistant :8095, Uptime Kuma :3001),
  `jarvis` (desktop PC; Jarvis V2 removed 26 Oct 2026). Source: the Obsidian vault note "Jarvis - Complete Breakdown".
- Locks, alarms, sirens, cameras and gates in Home Assistant always need the person's explicit yes in the app.

## Apps

- `app/` — **Hub** (`za.pixello.hub`): phone and SIM info, home-server status over Tailscale, Live Update (Now Bar) demo.
