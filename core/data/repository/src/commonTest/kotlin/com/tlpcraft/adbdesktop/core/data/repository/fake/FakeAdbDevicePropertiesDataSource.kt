package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DevicePropertiesDto

class FakeAdbDevicePropertiesDataSource(private val cpuSnapshots: List<CpuSnapshotDto>) : AdbDevicePropertiesDataSource {
    private var nextSnapshot = 0

    override suspend fun readProperties(serial: String) = DevicePropertiesDto(manufacturer = "Google", model = "Pixel 8", androidVersion = "15")

    override suspend fun readBatteryLevel(serial: String): Int = 80

    override suspend fun readCpuSnapshot(serial: String): CpuSnapshotDto = cpuSnapshots[nextSnapshot++.coerceAtMost(cpuSnapshots.lastIndex)]
}
