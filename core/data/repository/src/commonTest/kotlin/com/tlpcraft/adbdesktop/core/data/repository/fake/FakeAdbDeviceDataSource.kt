package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAdbDeviceDataSource : AdbDeviceDataSource {
    var devices: List<DeviceDto> = emptyList()
    var trackingError: Throwable? = null
    var connectError: Throwable? = null
    var trackingStarts = 0
        private set

    override fun trackDevices(): Flow<List<DeviceDto>> = flow {
        trackingStarts++
        trackingError?.let { throw it }
        emit(devices)
    }

    override suspend fun connect(address: String) {
        connectError?.let { throw it }
    }

    override suspend fun disconnect(serial: String) = Unit

    override suspend fun pair(address: String, pairingCode: String) = Unit
}
