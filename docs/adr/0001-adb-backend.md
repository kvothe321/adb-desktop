# 1. Talk to devices by running the adb binary

Status: accepted

## Context

The app needs device tracking, wireless connect and pairing, shell commands and package management, and later
logcat streaming, file transfer and screen capture. It runs next to Android Studio, which keeps its own adb server
alive on port 5037. The adb backend must stay swappable: only `core:data:source:adb` knows about it.

## Options

1. **Run the `adb` executable** with `ProcessBuilder`, reading stdout as text or as a stream, wrapped in coroutines
   and `Flow`.
2. **dadb**, a Kotlin library that speaks the adb protocol directly to the device's `adbd`, without the adb server.
3. **An adb server client** (jadb, or Google's ddmlib) that talks to the running adb server over its socket protocol.

## Decision

Option 1: run the adb binary.

- Every adb feature is available the moment platform-tools ships it: `pair`, mDNS devices, `track-devices`,
  `install-multiple`, `logcat`, `screenrecord`. dadb does not use the adb server, so it cannot see devices the
  server owns and has no pairing or mDNS support; it would also fight Android Studio for USB devices.
- Behaviour matches the command line exactly, so users and developers can reproduce any issue by running the same
  command shown in logs.
- Users already have platform-tools; nothing extra is bundled.
- Server client libraries are either internal to Android Studio (ddmlib, large, tied to its release cadence) or
  thinly maintained (jadb), and still need a running server.

## Consequences

- One process per command. Commands that need several values are batched into a single shell round trip (for
  example the CPU snapshot), and device tracking uses one long-lived `track-devices -l` stream instead of polling.
- Output is text. Parsers are pure functions in `commonMain`, covered by tests that use real adb output as fixtures.
- adb wording can differ between platform-tools versions and some failures exit with code 0. Success is therefore
  judged from the output where adb is known to do this (`connect`, `pair`, `pm`).
- The executable is located from the user's override, then `ANDROID_HOME`, `ANDROID_SDK_ROOT` and `PATH`.
- Replacing the backend means a new implementation of the `Adb*DataSource` interfaces; domain and features are
  untouched.
