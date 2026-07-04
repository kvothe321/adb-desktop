# ADB Desktop

A desktop GUI for `adb`, built with Compose Multiplatform for Windows, macOS and Linux.

Today it lists attached devices live (USB, emulators and Wi-Fi), shows battery and CPU usage, connects and pairs
devices over Wi-Fi, and lets you browse and manage the installed apps of the selected device.
See [docs/ROADMAP.md](docs/ROADMAP.md) for what comes next.

## Requirements

- **JDK 17 or newer** to run Gradle. The build provisions the JDK 21 toolchain it compiles and runs with.
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

Keyboard shortcuts: `Ctrl+1` Devices, `Ctrl+2` Apps, `Ctrl+3` Files (`Cmd` on macOS).

## Check

```bash
./gradlew check            # ktlint, detekt (with compose-rules), module graph rules and all tests
./gradlew ktlintFormat     # fix formatting
./gradlew checkModuleGraph # dependency direction only
```

`./gradlew check` must pass before every commit. The build installs two git hooks: `pre-commit` runs
`ktlintFormat`, `commit-msg` enforces [Conventional Commits](https://www.conventionalcommits.org).

## Continuous integration

- [`ci.yml`](.github/workflows/ci.yml) runs `./gradlew check` once on Linux for pushes to `main` and for pull
  requests. On macOS and Windows it runs only the specs tagged `platform`, which cover the code that depends on the
  host (finding and running executables, the preferences store):
  `./gradlew :core:data:source:adb:jvmTest :core:data:source:preferences:jvmTest -Pkotest.tags=platform`.
- [`package.yml`](.github/workflows/package.yml) builds the deb, dmg and msi on their native runners when a `v*` tag
  is pushed, or on demand, and uploads them as artifacts.

## Modules

```mermaid
graph LR
    desktopApp --> feature_devices[feature:devices]
    desktopApp --> feature_apps[feature:apps]
    desktopApp --> feature_files[feature:files]
    desktopApp --> repository[core:data:repository]
    desktopApp --> logger[core:logger]

    feature_devices --> domain[core:domain]
    feature_apps --> domain
    feature_files --> domain
    feature_devices --> uikit[adbdesktop-ui-kit]
    feature_apps --> uikit
    feature_files --> uikit

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
