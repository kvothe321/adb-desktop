package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import kotlinx.coroutines.flow.Flow

interface AdbDeviceDataSource {
    /** Emits the attached devices whenever the adb server reports a change. */
    fun trackDevices(): Flow<List<DeviceDto>>

    suspend fun connect(address: String)

    suspend fun disconnect(serial: String)

    suspend fun pair(address: String, pairingCode: String)
}
