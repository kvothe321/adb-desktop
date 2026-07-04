package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DevicePropertiesDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.BatteryParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.CpuSnapshotParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.PropertiesParser

class AdbDevicePropertiesDataSourceImpl(private val client: AdbClient) : AdbDevicePropertiesDataSource {

    override suspend fun readProperties(serial: String): DevicePropertiesDto {
        val properties = PropertiesParser.parse(client.shell(serial, "getprop"))
        return DevicePropertiesDto(
            manufacturer = properties["ro.product.manufacturer"].orEmpty(),
            model = properties["ro.product.model"].orEmpty(),
            androidVersion = properties["ro.build.version.release"].orEmpty(),
        )
    }

    override suspend fun readBatteryLevel(serial: String): Int? = BatteryParser.parseLevel(client.shell(serial, "dumpsys", "battery"))

    override suspend fun readCpuSnapshot(serial: String): CpuSnapshotDto = CpuSnapshotParser.parse(client.shell(serial, CpuSnapshotParser.SCRIPT))
}
