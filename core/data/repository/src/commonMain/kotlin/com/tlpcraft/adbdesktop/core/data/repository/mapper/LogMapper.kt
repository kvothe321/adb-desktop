package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel

internal fun LogEntryDto.toDomain() = LogEntry(
    timestamp = timestamp,
    processId = processId,
    threadId = threadId,
    level = level.toLogLevel(),
    tag = tag,
    message = message,
)

private fun Char.toLogLevel(): LogLevel = when (this) {
    'V' -> LogLevel.VERBOSE
    'D' -> LogLevel.DEBUG
    'I' -> LogLevel.INFO
    'W' -> LogLevel.WARNING
    'E' -> LogLevel.ERROR
    else -> LogLevel.FATAL
}
