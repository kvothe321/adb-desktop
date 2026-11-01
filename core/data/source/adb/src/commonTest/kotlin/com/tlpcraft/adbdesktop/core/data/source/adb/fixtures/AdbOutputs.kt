package com.tlpcraft.adbdesktop.core.data.source.adb.fixtures

/** Real adb output. Serials and addresses are anonymized, layout and wording are unchanged. */
object AdbOutputs {
    const val DEVICES_EMPTY = "List of devices attached\n\n"

    val DEVICES_LONG = """
        List of devices attached
        emulator-5554          device product:sdk_gphone64_x86_64 model:sdk_gphone64_x86_64 device:emu64xa transport_id:1
        R5CT10ABCDE            device usb:1-1 product:a52qnsxx model:SM_A525F device:a52q transport_id:3
        192.168.1.42:5555      offline product:a52qnsxx model:SM_A525F device:a52q transport_id:4
        adb-R5CT10ABCDE-xYz12._adb-tls-connect._tcp device product:a52qnsxx model:SM_A525F device:a52q transport_id:5
        0123456789ABCDEF       unauthorized usb:1-2 transport_id:6
        FA7AB1A02345           no permissions (missing udev rules? user is in the plugdev group); see [http://developer.android.com/tools/device.html] usb:1-3 transport_id:7

    """.trimIndent()

    const val DEVICES_WITH_DAEMON_START = "* daemon not running; starting now at tcp:5037\n* daemon started successfully\nList of devices attached\n" +
        "emulator-5554\tdevice\n"

    const val TRACK_DEVICES_EMPTY_FRAME = "0000"

    const val CONNECT_SUCCESS = "connected to 192.168.1.42:5555\n"
    const val CONNECT_ALREADY = "already connected to 192.168.1.42:5555\n"
    const val CONNECT_REFUSED = "failed to connect to '127.0.0.1:5599': Connection refused\n"
    const val CONNECT_NO_ROUTE = "cannot connect to 192.168.1.99:5555: No route to host (113)\n"

    const val DISCONNECT_SUCCESS = "disconnected 192.168.1.42:5555\n"
    const val DISCONNECT_UNKNOWN = "error: no such device '127.0.0.1:5599'\n"

    const val PAIR_SUCCESS = "Successfully paired to 192.168.1.42:37123 [guid=adb-R5CT10ABCDE-xYz12]\n"
    const val PAIR_WRONG_CODE = "Failed: Wrong password or connection was dropped.\n"
    const val PAIR_PROTOCOL_FAULT = "error: protocol fault (couldn't read status message): Success\n"

    const val DEVICE_NOT_FOUND = "adb: device 'nonexistent' not found\n"

    val GETPROP = """
        [ro.build.version.release]: [14]
        [ro.build.version.sdk]: [34]
        [ro.product.manufacturer]: [samsung]
        [ro.product.model]: [SM-A525F]
        [persist.sys.timezone]: []
    """.trimIndent()

    val DUMPSYS_BATTERY = """
        Current Battery Service state:
          AC powered: false
          USB powered: true
          Wireless powered: false
          status: 2
          health: 2
          present: true
          level: 87
          scale: 100
          voltage: 4211
          temperature: 285
          technology: Li-ion
    """.trimIndent()

    val CPU_SNAPSHOT = """
        cpu  14725 1022 9480 120480 512 0 211 0 0 0
        ---
        8
        ---
        1803000
        1803000
        2400000
        2400000
    """.trimIndent()

    val CPU_SNAPSHOT_NO_FREQUENCY = """
        cpu  100 0 100 800 0 0 0 0 0 0
        ---
        4
        ---
    """.trimIndent()

    val PM_LIST_PACKAGES = """
        package:com.android.settings
        package:com.google.android.gms
        package:com.example.notes
    """.trimIndent()

    val PM_PATH_SPLIT = """
        package:/data/app/~~a1b2==/com.example.notes-c3d4==/base.apk
        package:/data/app/~~a1b2==/com.example.notes-c3d4==/split_config.arm64_v8a.apk
    """.trimIndent()

    val DUMPSYS_PACKAGE = """
        Packages:
          Package [com.example.notes] (5d3c1f0):
            userId=10234
            pkg=Package{8a1b2c3 com.example.notes}
            codePath=/data/app/~~a1b2==/com.example.notes-c3d4==
            versionCode=241 minSdk=26 targetSdk=34
            versionName=2.4.1
            flags=[ HAS_CODE ALLOW_CLEAR_USER_DATA ALLOW_BACKUP ]
            timeStamp=2024-11-20 14:30:21
            firstInstallTime=2023-03-12 09:15:00
            lastUpdateTime=2024-11-20 14:30:22
    """.trimIndent()

    val STAT_SIZES = """
        41943040
        1048576
    """.trimIndent()

    val APP_SIZES = """
        code: 8192 bytes (8.00 Kb)
        data: 12582912 bytes (12.00 Mb)
        cache: 2097152 bytes (2.00 Mb)
        apk: 0 bytes
        lib: 0 bytes
        dm: 0 bytes
        dexopt artifacts: 0 bytes
        current profile : 0 bytes
        reference profile: 0 bytes
        external cache: 0 bytes
    """.trimIndent()

    const val APP_SIZES_UNSUPPORTED = "Unknown command: get-package-storage-stats\n"

    val LOGCAT = """
        --------- beginning of main
        10-31 23:05:50.598  1537  4112 I SpamListSync: Call spam module disabled. Skipping cleaning cache
        10-31 23:05:50.599  1254  5024 W NetworkScheduler.Stats: (REDACTED) Task %s/%s finished executing. cause:%s
        10-31 23:05:50.605  1254  3510 E AndroidRuntime:
        10-31 23:05:50.606   712   712 D Spaced Tag  : message: with colons
    """.trimIndent()

    val LS_ROOT = """
        total 84
        drwxr-xr-x  28 root   root       683 2009-01-01 02:00 .
        drwxr-xr-x  28 root   root       683 2009-01-01 02:00 ..
        lrw-r--r--   1 root   root        30 2009-01-01 02:00 adb_keys -> /product/etc/security/adb_keys
        drwxr-xr-x 101 root   root      2040 2026-10-31 23:03 apex
        d?????????   ? ?      ?            ?                ? data_mirror
        -rw-r--r--   1 root   root      1234 2009-01-01 02:00 init.rc
    """.trimIndent()

    const val LS_DEVICE_NODE = "crw-rw-rw- 1 root root 1,   3 2026-10-31 23:03 null\n"
    const val LS_SPACES = "total 4\n-rw-rw---- 1 u0_a215 media_rw 0 2026-10-31 23:05 it's a file.txt\n"
    const val LS_MISSING = "ls: /nonexistent: No such file or directory\n"

    val ADB_VERSION = """
        Android Debug Bridge version 1.0.41
        Version 37.0.0p1-android-tools
        Installed as /usr/bin/adb
        Running on Linux 7.2.11-zen1-1-zen (x86_64)
    """.trimIndent()

    const val MDNS_SERVICES = "List of discovered mdns services\n" +
        "adb-EMULATOR36X2X12X0\t_adb._tcp\t10.0.2.16:5555\n" +
        "adb-EMULATOR36X2X12X0\t_adb._tcp\t10.0.2.16:5555\n" +
        "adb-R5CT10ABCDE-xYz12\t_adb-tls-pairing._tcp\t192.168.1.42:41567\n"

    val INSTALL_EXCEPTION = """
        Performing Streamed Install
        adb: failed to install /tmp/real.apk:
        Exception occurred while executing 'install':
        android.os.ParcelableException: java.io.IOException: Requested internal only, but not enough space
        	at android.util.ExceptionUtils.wrap(ExceptionUtils.java:34)
        	at com.android.server.pm.PackageInstallerService.createSession(PackageInstallerService.java:671)
        Caused by: java.io.IOException: Requested internal only, but not enough space
        	at com.android.internal.content.InstallLocationUtils.resolveInstallVolume(InstallLocationUtils.java:242)
        	... 13 more
    """.trimIndent()

    const val PM_FAILURE = "Failure [DELETE_FAILED_INTERNAL_ERROR]\n"
}
