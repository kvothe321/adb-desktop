# Roadmap

The main features are in place. What comes next, highest priority first:

1. **Files**: transfer progress, and drag and drop onto the window.
2. **Apps**: install by dropping APK files on the window.
3. **Logcat**: filter by package, and export to a file.
4. **Screen**: copy a screenshot to the clipboard, show the elapsed recording time.
5. **Shell**: colored output and a real pseudo-terminal, so full-screen programs work.

Engineering follow-ups:

- Publish the packaged installers as GitHub release assets and derive `packageVersion` from the tag.
- Run `DeviceIntegrationTest` on an emulator in CI.
