# Architecture and scope

## Navigation

Home ↔ Explore ↔ Create ↔ Notifications ↔ Profile are the five bottom destinations. Home/Explore → Article → Comments/Publisher. Home/Explore → Location. Create → AI Studio or Community Reporter; Create internally advances Editor → Attachments → Preview. Profile → Saved stories / Draft / Settings / Firebase accounts.

## UI

Material 3, teal `#006B65`, mint surfaces, rounded cards, amber accents, editorial titles and generous spacing. `activity_main.xml`, `fragment_feed.xml`, and `fragment_page.xml` provide XML view structure. Individual Java fragments compose reusable Material cards, inputs and actions through `Ui`. Vector icons and original park illustrations are bundled, so the demo never needs external image services. Illustrations are not replicas of the reference photographs. Navigation Component owns back navigation. RecyclerView recycles feed cards. Media3 players are released when their viewer closes. System insets and keyboard insets are handled at the activity root.

`LocalPulseApp → LocalRepository` retains device-only JSON data in private SharedPreferences. `ComposerViewModel` retains the active draft and persists it across process death. Models are separate from views. `PublishPolicy` and `ReportPolicy` contain testable Java decisions. The bounded demo dataset is searched locally; this is not a scalable Realtime Database full-text search implementation.

## Online boundary

`AuthRepository` is the only live Firebase data adapter in this delivery. Firebase initialization uses the official Google Services configuration. Private profile rules deny role injection and extra fields. All unimplemented cloud data is default-deny. No admin credentials, provider keys, precise coordinates or raw audio are saved in the repo.

Suggested subsequent backend schema:

| Node | Purpose / access |
|---|---|
| users/{uid} | Private account data; self access, validated fields |
| publishers/{uid} | Separate public profile, backend-controlled counts |
| news/{id}, media/{id} | Moderated publication and attachment metadata |
| userNews/{uid}, locationNews/{locationId}, categoryNews/{categoryId} | Fan-out indexes, timestamp cursor + page limit |
| comments/{newsId}/{id}, likes/{newsId}/{uid} | Own mutation, immutable author, idempotent like key |
| favorites/{uid}, following/{uid}, followedLocations/{uid} | Self-only preferences |
| notifications/{uid}, reports/{id} | Trusted dispatch and private moderation reports |
| communityReports/{id}, reportUpdates/{id}, suggestedUpdates/{id} | Reports, attributed append-only updates, suggestions |
| reportAudit/{id}, reportModeration/{id} | Backend-only immutable audit and private moderator data |
| categories, locations, adminSettings | Trusted canonical IDs and configuration |

These nodes are **planning, not deployed functionality**. Required cloud invariants: author immutable, suspended-user publishing denied, privileged claims issued only by trusted Admin SDK, atomic fan-out publication, storage cleanup, pagination and query indexes. Moderator approval must not imply factual verification. Status changes and audit writes require a callable backend transaction and cannot trust device state. Source validation, geographic coverage, localization and admin workflows need implementation before public release.

## AI boundary

Future: Android AIRepository → authenticated/App Check callable Function → server-held provider key → draft/media preview → explicit human approval → normal publication. Configure per-user quotas and spending ceilings server-side; video is disabled by default. Real photo evidence and generated illustrations must retain different provenance. Offline formatting in this release only trims whitespace and preserves the user's supplied text. It does not claim AI understanding, translation or verification.

## Learning guide

For students: `News` shows object state and a zero-argument constructor; `LocalRepository` demonstrates ArrayList/JSON persistence and keyed idempotent likes; `ComposerViewModel` shows lifecycle state; `AuthRepository.Result` demonstrates asynchronous interfaces/callbacks; `ReportPolicy` models a state machine. Trace a button click through its fragment, repository mutation and redraw. An on-device ownership check is useful UX, but only server rules can protect shared data. Avoid storing credentials in an APK, treating a hidden button as security, or claiming a passed test that was never run.
