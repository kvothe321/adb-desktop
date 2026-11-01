package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** One `logcat -v threadtime` line. [level] is the single-letter priority: `V`, `D`, `I`, `W`, `E`, `F` or `S`. */
data class LogEntryDto(
    val timestamp: String,
    val processId: Int,
    val threadId: Int,
    val level: Char,
    val tag: String,
    val message: String,
)
