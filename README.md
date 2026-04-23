# SMSBridge

An Android SMS manager app that works as your **default SMS app** and forwards incoming messages to configurable webhooks — with per-webhook filter rules, custom HTTP headers, and encrypted auth storage.

## Features

### SMS App (Play Store Compliant)
- Full inbox with conversation threads
- Send & receive SMS
- Contact name resolution
- Unread badge indicators
- Persistent foreground service — never killed by Android OS
- Survives reboot via `BootReceiver`

### Webhook Forwarding
- Add multiple webhooks, each independently enabled/disabled
- Per-webhook filter rules — only matching SMS are forwarded
- Configurable retry count and delay with linear back-off
- Delivery log with status, attempt count, HTTP response code, and error detail

### Filter Rules
Each webhook has its own set of filter rules with:

| Field | Match Types |
|---|---|
| `SENDER` | `CONTAINS`, `EXACT`, `STARTS_WITH`, `ENDS_WITH`, `REGEX` |
| `BODY` | `CONTAINS`, `EXACT`, `STARTS_WITH`, `ENDS_WITH`, `REGEX` |

- **AND** mode — SMS must satisfy **all** rules
- **OR** mode — SMS must satisfy **any** rule
- **Negate** toggle — invert any rule (NOT condition)
- No rules = forward all SMS (catch-all)

### HTTP Headers & Auth
- Add any custom header per webhook (e.g. `Authorization`, `X-Api-Key`, `X-Custom-Header`)
- Supports all auth patterns:
  - Bearer Token: `Authorization: Bearer <token>`
  - API Key: `X-Api-Key: <key>`
  - Basic Auth: `Authorization: Basic <base64>`
  - Any custom header
- Secret flag — masks value in UI with a reveal button
- All values encrypted with **AES-256-GCM via Android Keystore** before Room storage

### Webhook Payload (JSON)
```json
{
  "sender": "+919876543210",
  "body": "Your OTP is 123456",
  "timestamp": 1745123456789,
  "device_id": "abc123def456"
}
```

## Architecture

```
SMS arrives
    ↓
SmsReceiver (SMS_DELIVER — fires even if app killed)
    ↓
SmsForegroundService (foreground priority, START_REDELIVER_INTENT)
    ↓
WebhookDispatcher
    ├── FilterMatcher (evaluate per-webhook rules)
    ├── Room DB (save to local inbox + delivery log)
    └── OkHttp POST (with retry + encrypted headers)
```

### Why It Won't Be Killed
| Mechanism | Effect |
|---|---|
| Default SMS app | `SMS_DELIVER` wakes app even if process is killed — Android OS guarantee |
| `startForeground()` | Same kill priority as visible UI |
| `START_REDELIVER_INTENT` | OS re-delivers SMS data if killed mid-flight |
| `BootReceiver` | Restarts after phone reboot |
| `LOCKED_BOOT_COMPLETED` | Starts even before screen unlock |

## Tech Stack

| Component | Library |
|---|---|
| Language | Kotlin |
| UI | Material3 + ViewBinding + Navigation Component |
| Database | Room 2.6 |
| HTTP | OkHttp 4.12 |
| Async | Kotlin Coroutines + Flow |
| Security | Android Keystore + EncryptedSharedPreferences |
| Architecture | MVVM + Repository |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## Project Structure

```
app/src/main/java/com/smsbridge/
├── SMSBridgeApp.kt                    # Application class
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── dao/                       # WebhookDao, FilterRuleDao, MessageDao, etc.
│   │   └── entity/                    # WebhookEntity, FilterRuleEntity, etc.
│   └── repository/
│       ├── WebhookRepository.kt
│       └── SmsRepository.kt
├── receiver/
│   ├── SmsReceiver.kt                 # SMS_DELIVER broadcast
│   ├── MmsReceiver.kt                 # Required stub for default SMS app
│   └── BootReceiver.kt
├── service/
│   ├── SmsForegroundService.kt        # Foreground service — processes + forwards SMS
│   ├── WebhookDispatcher.kt           # Core forwarding engine with retry logic
│   └── HeadlessSmsSendService.kt      # Required stub for default SMS app
├── ui/
│   ├── MainActivity.kt                # Permission + default SMS app request
│   ├── inbox/                         # Inbox thread list
│   ├── conversation/                  # Message thread view + send
│   └── webhook/                       # Webhook CRUD, filter builder, header manager, log
└── util/
    ├── FilterMatcher.kt               # Evaluates filter rules against SMS
    ├── SecureStorage.kt               # AES-256-GCM encryption via Android Keystore
    ├── NotificationHelper.kt
    └── SmsUtils.kt
```

## Getting Started

1. Clone the repo
2. Open in **Android Studio Hedgehog** or later
3. Add an `ic_launcher` icon in `app/src/main/res/mipmap-*/`
4. Build and install on a device (API 26+)
5. Grant SMS permissions and set SMSBridge as the **default SMS app**
6. Go to **Webhooks** tab → tap **+** → add your endpoint URL
7. Optionally add filter rules and headers
8. Send yourself a test SMS — check the Delivery Log tab

## Play Store Compliance

This app is structured for Play Store submission:
- Declared as a legitimate default SMS handler with full inbox UI
- `Data Safety` form should declare: SMS content collected, sent to user-configured third-party endpoints
- A **Privacy Policy URL** is required before submission — it must disclose that SMS content may be forwarded to user-specified webhooks
- Targets API 35, uses `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (requires justification in store listing)
