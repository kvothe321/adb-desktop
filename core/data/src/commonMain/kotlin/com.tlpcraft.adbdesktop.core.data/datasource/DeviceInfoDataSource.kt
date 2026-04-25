package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.DeviceInfo

interface DeviceInfoDataSource {
    /**
     * Fetches a [DeviceInfo] snapshot for the given [deviceSerial] via ADB shell.
     * Implementations must perform all blocking I/O on an appropriate dispatcher.
     */
    suspend fun getDeviceInfo(deviceSerial: String): DeviceInfo
}
