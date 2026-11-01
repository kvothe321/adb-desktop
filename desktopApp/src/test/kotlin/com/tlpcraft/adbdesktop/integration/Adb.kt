package com.tlpcraft.adbdesktop.integration

/** Runs adb directly, to check the device from outside the app. */
internal fun adb(serial: String, vararg args: String): String {
    val process = ProcessBuilder(listOf("adb", "-s", serial) + args).redirectErrorStream(true).start()
    return process.inputStream.bufferedReader().readText().also { process.waitFor() }
}
