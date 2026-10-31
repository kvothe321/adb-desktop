package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Reads the platform-tools version from the `Version 37.0.0-...` line of `adb version`. */
internal object AdbVersionParser {
    private const val VERSION_PREFIX = "Version "

    fun parse(output: String): String? = output.lineSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith(VERSION_PREFIX) }
        ?.removePrefix(VERSION_PREFIX)
}
