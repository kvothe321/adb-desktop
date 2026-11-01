package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbLogcatDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class LogcatRepositoryImpl(
    private val logcatDataSource: AdbLogcatDataSource,
    private val processPollInterval: Duration = 2.seconds,
) : LogcatRepository {

    override fun observeLogs(serial: String): Flow<Outcome<List<LogEntry>, DeviceError>> = logcatDataSource.streamLogs(serial)
        .map { entries -> entries.map { it.toDomain() } }
        .asOutcomeFlow(Throwable::toDeviceError)

    override suspend fun clearLogs(serial: String): Outcome<Unit, DeviceError> = safeCall(Throwable::toDeviceError) { logcatDataSource.clearLogs(serial) }

    override fun observeProcessIds(serial: String, packageName: String): Flow<Set<Int>> = flow {
        while (true) {
            emit(logcatDataSource.readProcessIds(serial, packageName))
            delay(processPollInterval)
        }
    }.distinctUntilChanged()
}
