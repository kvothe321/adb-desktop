package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DevicePropertiesDto

interface AdbDevicePropertiesDataSource {
    suspend fun readProperties(serial: String): DevicePropertiesDto

    suspend fun readBatteryLevel(serial: String): Int?

    suspend fun readCpuSnapshot(serial: String): CpuSnapshotDto
}
