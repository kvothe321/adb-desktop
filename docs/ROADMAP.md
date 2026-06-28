# Roadmap

Next features, highest priority first. Each one lands as its own feature module that depends on `core:domain` and
`adbdesktop-ui-kit` only.

1. **Settings** (`feature:settings`): adb path override (today it can only be set from the "adb not found" state),
   persisted theme choice, polling intervals.
2. **Logcat** (`feature:logcat`): streaming `logcat -v threadtime` as a `Flow`, filters by level, tag, package and
   text, pause and clear, export to file.
3. **Shell** (`feature:shell`): interactive `adb shell` session with history and copyable monospace output.
4. **Files** (`feature:files`): browse the device file system, push and pull with progress, drag and drop.
   The sidebar entry exists and currently shows a placeholder that links here.
5. **Apps: install** (`feature:apps`): install and update APKs and split APKs by picking or dropping files.
6. **Screen** (`feature:screen`): screenshot to clipboard or file, screen recording with `screenrecord`.
7. **Wireless discovery**: list `adb mdns services` so pairing does not need typing the address.

Engineering follow-ups:

- Compose UI tests for the screen contents.
- Publish the packaged installers as GitHub release assets and derive `packageVersion` from the tag.
