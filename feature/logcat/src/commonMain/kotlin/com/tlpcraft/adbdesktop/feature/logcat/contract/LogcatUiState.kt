package com.tlpcraft.adbdesktop.feature.logcat.contract

import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogFilter

data class LogcatUiState(
    val serial: String? = null,
    val rows: List<LogRow> = emptyList(),
    val filter: LogFilter = LogFilter(),
    val packageName: String = "",
    val isPackageRunning: Boolean = false,
    val isPaused: Boolean = false,
    val failure: String? = null,
) {
    val visibleRows: List<LogRow>
        get() = rows.filter { filter.matches(it.entry) }

    val showsPackageNotRunning: Boolean
        get() = packageName.isNotBlank() && !isPackageRunning
}

/** [id] is unique within a stream, because identical lines are common in logs. */
data class LogRow(val id: Long, val entry: LogEntry)
