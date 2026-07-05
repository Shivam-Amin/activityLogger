# Activity Logger

Android app that logs app usage events, home screen visits, app session durations, and outgoing calls — all stored locally.

## Tech stack

- Kotlin
- Jetpack Compose
- Room (SQLite)
- Hilt
- Foreground Service
- UsageStatsManager + Telephony APIs

## Setup in Android Studio

1. Open Android Studio.
2. Choose **File → Open**.
3. Select this folder: `/Users/shivam/Projects/ActivityLogger`
4. Wait for Gradle sync to finish (Android Studio downloads Gradle automatically).
5. Connect a physical Android 12+ device (recommended). Emulators work for app events, but call logging needs a real phone/SIM.
6. Click **Run**.

If Gradle wrapper scripts are missing, Android Studio will offer to create them during sync. You can also run:

```bash
cd /Users/shivam/Projects/ActivityLogger
gradle wrapper
```

## Permissions (first launch)

When you open the app:

1. Tap **Grant runtime permissions** for:
   - Phone state
   - Call log
   - Contacts
   - Notifications (Android 13+)
2. Tap **Open usage access settings** and enable **Activity Logger**.
3. Return to the app. Monitoring starts automatically once everything is granted.

### Manual permission you must allow

| Permission | How |
|---|---|
| Usage access | Settings → Apps → Special app access → Usage access → Activity Logger → Allow |
| Phone / Call log / Contacts | Runtime dialogs in the app |
| Notifications | Runtime dialog (Android 13+) |

On some phones (Samsung, Xiaomi, Oppo), also disable battery restrictions for the app so the foreground service is not killed.

## Background behavior

The app runs all day using a **foreground service** with a persistent notification ("Activity logging active"). It restarts after reboot if permissions were already granted.

## What gets logged

| Event | Details |
|---|---|
| App opened | App name + timestamp |
| App closed | App name + session duration |
| Home screen | When launcher/home is opened |
| Outgoing call | Contact name/number + call duration |

## Log management

- Filter logs by date
- Select multiple logs and delete
- Delete all logs for a selected day

## Project structure

```
app/src/main/java/com/activitylogger/
  data/          Room database + repository
  domain/        Models + mappers
  monitor/       Foreground service + usage/call monitors
  ui/            Compose screens
  di/            Hilt modules
  util/          Date/time helpers
```

## Build from terminal

```bash
cd /Users/shivam/Projects/ActivityLogger
./gradlew assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## Notes

- This app is designed for personal sideload use.
- Call logging requires a physical device with telephony support.
- Session duration is calculated when an app moves to background.
