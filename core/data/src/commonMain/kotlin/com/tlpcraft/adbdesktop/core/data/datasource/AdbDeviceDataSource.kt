package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AdbDevice

interface AdbDeviceDataSource {
    suspend fun getDevices(): List<AdbDevice>
}
