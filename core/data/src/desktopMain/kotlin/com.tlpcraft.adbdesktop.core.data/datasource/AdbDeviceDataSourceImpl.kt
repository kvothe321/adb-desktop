package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AdbDevice

class AdbDeviceDataSourceImpl : AdbDeviceDataSource {

    override suspend fun getDevices(): List<AdbDevice> {
        val process = ProcessBuilder("adb", "devices")
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        return parseAdbDevicesOutput(output)
    }

    private fun parseAdbDevicesOutput(output: String): List<AdbDevice> = output
        .lines()
        .drop(1) // skip "List of devices attached" header
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size >= 2) {
                AdbDevice(serial = parts[0], status = parts[1])
            } else {
                null
            }
        }
}
