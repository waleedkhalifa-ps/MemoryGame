# Executed validation

Executed in the prepared cloud workspace on 2026-11-01:

```sh
./scripts/cloud-build.sh assembleDebug testDebugUnitTest lintDebug
```

- Debug APK: built successfully, package `com.localpulse`, API 26 minimum / 35 target.
- Gradle wrapper: downloaded and checksum verified; JDK 17 and SDK 35 used.
- Unit and headless Android tests: **15 passed, 0 failed, 0 skipped**.
- Android lint: **0 errors, 19 warnings** (unused resources, backup metadata guidance and display-text concatenation).
- Six Arabic screens rendered with Robolectric native graphics; home, create, AI and location output was visually inspected. This is headless rendering, not an emulator or physical-device screenshot.

| Test class | Passed |
|---|---:|
| com.localpulse.AppNavigationTest | 3 |
| com.localpulse.LocalRepositoryTest | 4 |
| com.localpulse.ScreenRenderTest | 1 |
| com.localpulse.domain.PublishPolicyTest | 3 |
| com.localpulse.domain.ReportPolicyTest | 4 |

The UI tests launch the Activity, open all eleven non-home destinations, and exercise the review gate through editor → attachments → preview → local publication. Repository tests cover persistence, draft restoration without implicit review, idempotent likes, own-comment editing/deletion and rejection of another author’s story edit. Reporter policy tests reject cross-user changes, skipped transitions and missing evidence.

## Not executed / not claimed

- Physical device or emulator installation, touch interaction, speech recognition, GPS, external PDF app and gallery picker behavior. There is no KVM device in this environment.
- Firebase project authentication, database rules/emulator tests, storage uploads, backend publication/moderation, notifications and AI providers. No Firebase project config or provider credentials were supplied; cloud data other than optional private profiles is intentionally denied.
- Full screenshot parity, all device sizes, TalkBack and complete locale/dark-mode visual coverage.

## Device acceptance checklist

Install the debug APK on Android 8.0+. Test denied location permission/manual selection, image/MP4/PDF import and removal, process restart draft recovery, own article edit/delete, landscape/large text, Arabic/Hebrew RTL, English LTR, dark mode, video dismissal and installed-provider voice dictation. Public release needs the separately documented backend phases.
