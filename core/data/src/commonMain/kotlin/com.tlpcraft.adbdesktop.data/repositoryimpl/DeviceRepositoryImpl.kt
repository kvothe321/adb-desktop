package com.tlpcraft.adbdesktop.data.repositoryimpl

import com.tlpcraft.adbdesktop.data.repository.DeviceRepository
import com.tlpcraft.adbdesktop.domain.model.AdbDevice

class DeviceRepositoryImpl : DeviceRepository {
    override suspend fun getConnectedDevices(): Result<List<AdbDevice>> {
        val process = ProcessBuilder("adb", "devices")
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        val x = output.lines()
            .drop(1) // skip "List of devices attached"
            .mapNotNull { line ->
                val parts = line.trim().split("\t")
                if (parts.size == 2) AdbDevice(parts[0], parts[1]) else null
            }

        return Result.success(x)
    }
}
