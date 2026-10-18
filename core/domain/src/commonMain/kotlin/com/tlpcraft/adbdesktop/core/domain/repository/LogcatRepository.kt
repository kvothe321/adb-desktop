package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogcatRepository {
    /**
     * Emits batches of entries, recent history first, until cancelled. When the stream breaks it emits the failure,
     * reconnects after a pause and carries on after the last entry it emitted, so no entry comes twice.
     */
    fun observeLogs(serial: String): Flow<Outcome<List<LogEntry>, DeviceError>>

    suspend fun clearLogs(serial: String): Outcome<Unit, DeviceError>

    /** Emits the processes running for [packageName] whenever they change. */
    fun observeProcessIds(serial: String, packageName: String): Flow<Set<Int>>
}
