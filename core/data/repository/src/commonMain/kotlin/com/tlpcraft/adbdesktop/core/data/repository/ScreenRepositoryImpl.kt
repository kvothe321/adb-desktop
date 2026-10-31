package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbScreenDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository

class ScreenRepositoryImpl(private val screenDataSource: AdbScreenDataSource) : ScreenRepository {

    override suspend fun captureScreenshot(serial: String): Outcome<ByteArray, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.captureScreenshot(serial) }

    override suspend fun recordScreen(serial: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.recordScreen(serial, RECORDING_PATH) }

    override suspend fun stopRecording(serial: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.stopRecording(serial) }

    override suspend fun saveRecording(serial: String, localPath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.moveToHost(serial, RECORDING_PATH, localPath) }

    private companion object {
        const val RECORDING_PATH = "/sdcard/adb-desktop-recording.mp4"
    }
}
