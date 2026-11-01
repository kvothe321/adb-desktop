# ADB Desktop

A desktop GUI for `adb`, built with Compose Multiplatform for Windows, macOS and Linux.

- **Devices**: live list over USB, emulator and Wi-Fi with battery and CPU usage, connect, pair and disconnect over
  Wi-Fi, including devices discovered on the network.
- **Apps**: browse and filter packages, see details, open, force stop, clear cache or data, uninstall, install APKs
  by picking or dropping them.
- **Files**: browse the device, upload and download with progress, drop files on the window, delete and create folders.
- **Logcat**: live stream with level, text and package filters, pause, clear and export.
- **Shell**: a real terminal on the device, with colors, full-screen programs such as `top` and `vi`, CJK text and emoji in
  their full width, and input methods for typing them.
- **Screen**: live mirroring you can use with mouse and keyboard as in scrcpy, pinned beside every section if you like,
  plus screenshots, copied to the clipboard or saved, and screen recording with a running clock.
- **Settings**: choose the adb executable and the theme.

Everything on screen stays current on its own: changes made on the device, by other tools or by this app appear
without a refresh, and failures clear once their cause is gone. Screens that are hidden or minimized stop reading from
the device.

Actions apply to the device picked in the sidebar. See [docs/ROADMAP.md](docs/ROADMAP.md) for what comes next.

## Requirements

- **JDK 17 or newer** to start Gradle. The build provisions JDK 21 for the Gradle daemon, compiling and running.
- **Android platform-tools** (`adb`). It is found through, in order: the path set in the app, `ANDROID_HOME`,
  `ANDROID_SDK_ROOT`, then `PATH`.
- **An Android SDK** (`ANDROID_HOME`, or `sdk.dir` in `local.properties`). Only the build needs it: Compose modules
  carry an Android target so IDE previews render ([ADR 2](docs/adr/0002-preview-only-android-target.md)).

## Run

```bash
./gradlew :desktopApp:run                              # run the app
./gradlew :desktopApp:hotRun --auto                    # run with Compose Hot Reload, reloading on save
./gradlew :desktopApp:packageDistributionForCurrentOS  # build a native installer (dmg, msi or deb)
```

Keyboard shortcuts: `Ctrl+1` to `Ctrl+7` open the sections in sidebar order (`Cmd` on macOS).

## Check

```bash
./gradlew check            # ktlint, detekt (with compose-rules), module graph rules and all tests
./gradlew ktlintFormat     # fix formatting
./gradlew checkModuleGraph # dependency direction only
```

To also run the real app graph against a device or emulator (`check` itself never needs one):

```bash
ADB_TEST_SERIAL=emulator-5554 ./gradlew :desktopApp:test --rerun --tests '*DeviceIntegrationTest'
```

`./gradlew check` must pass before every commit. The build installs two git hooks: `pre-commit` runs
`ktlintFormat`, `commit-msg` enforces [Conventional Commits](https://www.conventionalcommits.org).

## Known Android limits

- **Clear cache** only works on debuggable apps. Android does not let adb clear another app's cache, so the app uses
  `run-as` and reports the reason when the device refuses.
- **Data and cache sizes** need a recent Android version; older ones show them as unknown.
- **Screen recording** stops after 3 minutes, which is Android's limit.

## Terminal

Ctrl+C goes to the running program, as in any terminal. Paste with Ctrl+Shift+V, and copy a selection from its
context menu. Colors beyond the 16 of the theme map to the closest one. Characters are one cell wide, so CJK text and
emoji can misalign, and input methods and dead keys are not supported.

## Continuous integration

- [`ci.yml`](.github/workflows/ci.yml) runs `./gradlew check` once on Linux for pushes to `main` and for pull
  requests. On macOS and Windows it runs only the specs tagged `platform`, which cover the code that depends on the
  host (finding and running executables, the settings file):
  `./gradlew :core:data:source:adb:jvmTest :core:data:source:preferences:jvmTest -Pkotest.tags=platform`.
- [`ci.yml`](.github/workflows/ci.yml) also runs `DeviceIntegrationTest` on an Android emulator.
- [`package.yml`](.github/workflows/package.yml) builds the deb, dmg and msi on their native runners, on demand or when
  a tag is pushed. A tag such as `v1.2.0` sets the version and publishes the installers as a GitHub release. Tags need
  a major version of at least 1, because the dmg format refuses 0.

## Modules

```mermaid
graph LR
    desktopApp --> feature_devices[feature:devices]
    desktopApp --> feature_apps[feature:apps]
    desktopApp --> feature_files[feature:files]
    desktopApp --> feature_logcat[feature:logcat]
    desktopApp --> feature_shell[feature:shell]
    desktopApp --> feature_screen[feature:screen]
    desktopApp --> feature_settings[feature:settings]
    desktopApp --> repository[core:data:repository]
    desktopApp --> logger[core:logger]

    feature_devices --> domain[core:domain]
    feature_apps --> domain
    feature_files --> domain
    feature_logcat --> domain
    feature_shell --> domain
    feature_screen --> domain
    feature_settings --> domain
    feature_devices --> uikit[adbdesktop-ui-kit]
    feature_apps --> uikit
    feature_files --> uikit
    feature_logcat --> uikit
    feature_shell --> uikit
    feature_screen --> uikit
    feature_settings --> uikit

    repository --> domain
    repository --> adb[core:data:source:adb]
    repository --> preferences[core:data:source:preferences]

    domain --> common[core:common]
    adb --> common
    adb --> logger
    preferences --> common
```

| Module | Responsibility |
|---|---|
| `desktopApp` | Entry point, window, keyboard shortcuts, navigation host, DI graph |
| `adbdesktop-ui-kit` | Design system: `AppTheme`, tokens, closed feature-agnostic components, previews |
| `feature:*` | One screen each: MVI contract, ViewModel as reducer, stateless content |
| `core:domain` | Models, repository interfaces and use cases. Pure Kotlin |
| `core:data:repository` | Implements the domain repositories and maps DTOs to domain models |
| `core:data:source:adb` | Finds and runs adb, streams and parses its output into DTOs |
| `core:data:source:preferences` | Per-user settings storage |
| `core:common` | `Outcome`, `DomainError`, dispatchers |
| `core:logger` | Logging behind `AppLogger` |
| `core:testing` | Fakes of the domain repositories for tests |

Rules the build enforces:

- Features see `core:domain` and the UI kit only. Only `desktopApp` sees the data layer, to wire DI. adb is a data
  source, never visible above `core:data:repository` ([ADR 1](docs/adr/0001-adb-backend.md)).
- Only `adbdesktop-ui-kit` may use Material. Feature modules and `desktopApp` do not have it on their classpath,
  and detekt forbids Material, colors, shapes and fonts there.
- Every user-facing string is a Compose resource.

## Project layout

- `build-logic/`: convention plugins (Kotlin Multiplatform library, Compose library, feature, lint, module graph,
  commit lint, git hooks).
- `config/detekt/`: detekt configuration. ktlint and compose-rules settings live in `.editorconfig`.
- `gradle/libs.versions.toml`: every dependency version.
- `docs/`: architecture decisions and roadmap.

## Credits

Icons are the linear style of [Iconsax](https://iconsax.io), from the `iconsax` npm package (MIT).

Mirroring runs the [scrcpy](https://github.com/Genymobile/scrcpy) 5.0.1 server (Apache 2.0) on the device, and decodes its video
with FFmpeg (LGPL) through [JavaCPP Presets](https://github.com/bytedeco/javacpp-presets).
