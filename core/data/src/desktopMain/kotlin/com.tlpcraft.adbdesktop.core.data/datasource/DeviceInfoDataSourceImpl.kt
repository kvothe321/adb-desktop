package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.withContext

class DeviceInfoDataSourceImpl(
    private val dispatcherProvider: DispatcherProvider,
) : DeviceInfoDataSource {

    override suspend fun getDeviceInfo(deviceSerial: String): DeviceInfo = withContext(dispatcherProvider.io) {
        val manufacturer = runAdbShell(deviceSerial, "getprop ro.product.manufacturer")
        val model = runAdbShell(deviceSerial, "getprop ro.product.model")
        val versionRelease = runAdbShell(deviceSerial, "getprop ro.build.version.release")
        val batteryOutput = runAdbShell(deviceSerial, "dumpsys battery")
        val batteryLevel = batteryOutput
            .lines()
            .firstOrNull { it.trim().startsWith("level:") }
            ?.substringAfter("level:")
            ?.trim()
            ?.toIntOrNull()

        DeviceInfo(
            name = "$manufacturer $model".trim(),
            androidVersion = "Android $versionRelease",
            batteryLevel = batteryLevel,
        )
    }

    private fun runAdbShell(deviceSerial: String, command: String): String {
        val process = ProcessBuilder("adb", "-s", deviceSerial, "shell", command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.trim()
    }
}
