# android-apps

Personal Android apps for my Galaxy S25 FE.

## Hub (starter app)

- **Phone:** model, Android version, battery, Wi-Fi / mobile data / Tailscale status, and both SIMs
  (name, carrier, network type, signal, which one handles data and calls).
- **Server:** checks Home Assistant, Music Assistant, Uptime Kuma and the Jarvis PC over Tailscale.
- **Lab:** wallpaper-colour toggle and a Live Update demo that shows a server sweep in the Now Bar.

## Install on the phone

1. Download the APK: **My Drive → Android Apps → Hub-x.y.z.apk**, or the **Build** workflow's `hub-apk` artifact on GitHub.
2. Open it and select **Install**. Auto Blocker must be off (**Settings → Security and privacy → Auto Blocker**).
3. Keep Tailscale connected for the Server tab.

## Build

```bash
./gradlew assembleRelease testDebugUnitTest
```
