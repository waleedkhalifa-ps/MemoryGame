# LocalPulse · نبض البلد

## Downloads

- [Android APK](https://github.com/waleedkhalifa-ps/MemoryGame/blob/main/downloads/LocalPulse-debug.apk?raw=true)
- [Android Studio project ZIP](https://github.com/waleedkhalifa-ps/MemoryGame/blob/main/downloads/LocalPulse-Android-Studio.zip?raw=true)
- [Setup instructions](downloads/README.md)

The APK is a debug-signed offline demonstration build. See the scope below before using it.

Native Android **Java + XML**, designed from the supplied Arabic LocalPulse screens. This first runnable delivery is an **offline interactive app**, not a production news service. All seeded stories, names and events are fictional; original vector illustrations are labeled. Nothing is publicly published. No API credentials are embedded.

## Build and run

Open this directory in Android Studio. Use JDK 17 or 21, Android SDK platform 35 and Build Tools 35.0.0. Set Android Studio's SDK location or create an ignored `local.properties` containing `sdk.dir=/your/android/sdk`.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.localpulse/.MainActivity
```

In the prepared cloud workspace, run `./scripts/cloud-build.sh assembleDebug testDebugUnitTest lintDebug` to use the installed toolchain and configured proxy/CA trust.

Gradle 8.11.1 is pinned in the wrapper. No Firebase project is required for the demo. Android 8.0/API 26 or later is required. Arabic is the default; change to English or Hebrew in Profile → Settings. Light, dark and system themes are available. Sample stories intentionally remain in Arabic; switching UI language does not silently translate source content.

## Working flows

- Home: RecyclerView news feed, Latest/Nearby/Following filters, locality selection, likes and saved stories.
- Explore: bounded on-device text search across title, text and publisher, category filters. Publisher profiles and followed-author feed.
- Read: full article, image viewer, local MP4 playback, external PDF viewing, Android share sheet, own-story editing/deletion, content-report acknowledgement.
- Comments: add, edit and delete the local account's comments. Data persists across restarts.
- Create: title/details/category/locality → images, MP4, PDF → preview → explicit factual review → local publication. Drafts persist. File import occurs off the UI thread, limits six files/20 MB each, uses MIME allowlists and app-private storage. Attachment removal detaches the file; garbage collection of unused files is not yet implemented.
- AI Studio: editable facts → **offline formatting helper**, preview → explicit acceptance into composer. Voice uses an installed Android speech recognition service and requires consent through that service. It may send audio to its provider. AI image/video screens explain that a provider is not connected; they never display simulated job progress or fake generated media.
- Community reporter: local report, follow toggle, timestamped progress, evidence-required author transitions, resolved/reopened timeline. This is a local demonstration, not a trusted backend audit log or fact-verification system.
- Profile, saved articles, local activity notifications, display-name editing, RTL/LTR, dark mode.

## Optional Firebase account connection

1. Register Android application `com.localpulse` in your own Firebase project.
2. Put its downloaded configuration at `app/google-services.json` (ignored by Git). The Gradle Google Services plugin activates only when this file exists.
3. Enable Email/Password Authentication and create a Realtime Database. Do not choose open test rules.
4. Review and deploy the restrictive starter rules in `firebase/` using your own authenticated Firebase CLI/project selection. No deployment was performed by this task.
5. Profile → Sign in supports real Firebase registration, login, password reset and logout when configured. Registration writes only the authenticated user's private display name and creation timestamp.

**Signing in does not switch the demo feed to cloud data.** All publishing, likes, comments, following and reports remain device-only and labeled. Cloud data paths other than the private account profile are denied by the supplied rules. Authentication profile-write failure is reported separately from account creation.

## Production work remaining

See [architecture and delivery scope](docs/ARCHITECTURE.md). The master specification describes a larger, phased product: shared Firebase news/media, secure moderation/admin roles, App Check, trusted status/audit operations, cloud notifications, and quota-controlled AI generation remain future phases. They require a real Firebase project, provider selection and deployment. No simulated backend is presented as live. There is no claim of a complete production implementation.

## Validation

Unit tests cover publication review, input bounds and reporter transitions. Robolectric tests launch the real Activity and navigate all primary fragments with Android resources. Android lint and the debug APK build are included in the build command. Headless tests do not establish visual fidelity or physical-device behavior. See `docs/VALIDATION.md` for actual executed outcomes and remaining device checks.
