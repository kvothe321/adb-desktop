package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogcatRepository {
    /** Emits batches of entries, recent history first. A failure completes the flow. */
    fun observeLogs(serial: String): Flow<Outcome<List<LogEntry>, DeviceError>>

    suspend fun clearLogs(serial: String): Outcome<Unit, DeviceError>
}
