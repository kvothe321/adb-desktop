package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

interface DeviceInfoRepository {
    /**
     * Returns a continuous stream of [DeviceInfo] snapshots for the given [deviceSerial],
     * polling the device at a fixed interval via ADB shell.
     */
    fun observeDeviceInfo(deviceSerial: String): Flow<Outcome<DeviceInfo, DeviceError>>
}
