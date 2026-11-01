package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceInfoRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeviceInfoRepositoryImpl(
    private val propertiesDataSource: AdbDevicePropertiesDataSource,
    private val pollInterval: Duration = 10.seconds,
) : DeviceInfoRepository {

    override fun observeDeviceInfo(serial: String): Flow<Outcome<DeviceInfo, DeviceError>> = flow {
        val properties = propertiesDataSource.readProperties(serial)
        while (true) {
            emit(
                DeviceInfo(
                    manufacturer = properties.manufacturer,
                    model = properties.model,
                    androidVersion = properties.androidVersion,
                    batteryLevel = propertiesDataSource.readBatteryLevel(serial),
                ),
            )
            delay(pollInterval)
        }
    }.asOutcomeFlow(Throwable::toDeviceError)
}
