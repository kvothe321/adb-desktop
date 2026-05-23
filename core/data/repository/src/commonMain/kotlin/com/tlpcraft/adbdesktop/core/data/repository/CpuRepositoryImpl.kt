package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toCpuInfo
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.repository.CpuRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Usage needs two samples, so the first value arrives after a short [firstSampleWindow]. */
class CpuRepositoryImpl(
    private val propertiesDataSource: AdbDevicePropertiesDataSource,
    private val firstSampleWindow: Duration = 500.milliseconds,
    private val pollInterval: Duration = 2.seconds,
) : CpuRepository {

    override fun observeCpuInfo(serial: String): Flow<Outcome<CpuInfo, DeviceError>> = flow {
        var previous = propertiesDataSource.readCpuSnapshot(serial)
        delay(firstSampleWindow)
        while (true) {
            val current = propertiesDataSource.readCpuSnapshot(serial)
            emit(current.toCpuInfo(previous))
            previous = current
            delay(pollInterval)
        }
    }.asOutcomeFlow(Throwable::toDeviceError)
}
