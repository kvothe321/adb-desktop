package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeLogcatRepository : LogcatRepository {
    val logs = Channel<Outcome<List<LogEntry>, DeviceError>>(Channel.UNLIMITED)
    val clearedSerials = mutableListOf<String>()
    var streamsStarted = 0
        private set

    override fun observeLogs(serial: String): Flow<Outcome<List<LogEntry>, DeviceError>> {
        streamsStarted++
        return logs.receiveAsFlow()
    }

    override suspend fun clearLogs(serial: String): Outcome<Unit, DeviceError> {
        clearedSerials += serial
        return Outcome.Success(Unit)
    }
}
