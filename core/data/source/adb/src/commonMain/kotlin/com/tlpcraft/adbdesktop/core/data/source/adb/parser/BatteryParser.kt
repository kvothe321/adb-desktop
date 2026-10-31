package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Reads the charge level from `dumpsys battery`. */
internal object BatteryParser {
    private const val LEVEL_KEY = "level:"

    fun parseLevel(output: String): Int? = output.lineSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith(LEVEL_KEY) }
        ?.removePrefix(LEVEL_KEY)
        ?.trim()
        ?.toIntOrNull()
}
