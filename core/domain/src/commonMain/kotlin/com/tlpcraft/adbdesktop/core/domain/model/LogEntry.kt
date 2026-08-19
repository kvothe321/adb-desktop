package com.tlpcraft.adbdesktop.core.domain.model

data class LogEntry(
    val timestamp: String,
    val processId: Int,
    val threadId: Int,
    val level: LogLevel,
    val tag: String,
    val message: String,
)

/** The entry in the format of `logcat -v threadtime`. */
fun LogEntry.toThreadTimeLine(): String = "$timestamp $processId $threadId ${level.name.first()} $tag: $message"

/** Ordered from least to most severe. */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL,
}

/** [processIds] limits entries to those processes; `null` means every process. */
data class LogFilter(
    val minimumLevel: LogLevel = LogLevel.VERBOSE,
    val query: String = "",
    val processIds: Set<Int>? = null,
) {
    fun matches(entry: LogEntry): Boolean {
        val text = query.trim()
        return entry.level >= minimumLevel &&
            (processIds == null || entry.processId in processIds) &&
            (text.isEmpty() || entry.tag.contains(text, ignoreCase = true) || entry.message.contains(text, ignoreCase = true))
    }
}
