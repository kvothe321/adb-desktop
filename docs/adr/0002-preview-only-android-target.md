# 2. Give Compose modules an Android target for previews only

Status: accepted

## Context

The app ships for desktop JVM only. IDE rendering of `@Preview` for Compose Multiplatform still runs on the Android
tooling, so a module without an Android target gets no design-time previews. Every public component and screen is
required to have previews.

## Decision

The `compose.multiplatform.library` convention applies AGP's Android-KMP library plugin to the design system and
feature modules, with `ui-tooling` on the Android runtime classpath. Nothing is ever packaged for Android. Pure
Kotlin modules (common, domain, data) stay JVM-only.

## Consequences

- Building the Compose modules needs an Android SDK (`ANDROID_HOME` or `sdk.dir` in `local.properties`).
- Previews use `@UiKitPreview` with `PreviewContext`, which renders light and dark variants.
- If JetBrains ships desktop preview rendering for common code, the Android target can be removed from the
  convention plugin without touching any module.
