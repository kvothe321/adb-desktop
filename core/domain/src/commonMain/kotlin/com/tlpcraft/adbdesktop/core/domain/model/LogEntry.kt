package com.tlpcraft.adbdesktop.core.domain.model

data class LogEntry(
    val timestamp: String,
    val processId: Int,
    val threadId: Int,
    val level: LogLevel,
    val tag: String,
    val message: String,
)

/** Ordered from least to most severe. */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL,
}

data class LogFilter(val minimumLevel: LogLevel = LogLevel.VERBOSE, val query: String = "") {
    fun matches(entry: LogEntry): Boolean {
        val text = query.trim()
        return entry.level >= minimumLevel &&
            (text.isEmpty() || entry.tag.contains(text, ignoreCase = true) || entry.message.contains(text, ignoreCase = true))
    }
}
