package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toControlMessages
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbMirrorDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbScreenDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class ScreenRepositoryImpl(
    private val screenDataSource: AdbScreenDataSource,
    private val mirrorDataSource: AdbMirrorDataSource,
) : ScreenRepository {

    override suspend fun captureScreenshot(serial: String): Outcome<ByteArray, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.captureScreenshot(serial) }

    override suspend fun recordScreen(serial: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.recordScreen(serial, RECORDING_PATH) }

    override suspend fun stopRecording(serial: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.stopRecording(serial) }

    override fun observeMirror(serial: String, input: Flow<MirrorInput>): Flow<Outcome<MirrorFrame, DeviceError>> = mirrorDataSource
        .streamFrames(serial, input.transform { it.toControlMessages().forEach { message -> emit(message) } })
        .map { MirrorFrame(it.width, it.height, it.pixels) }
        .asOutcomeFlow(Throwable::toDeviceError)

    override suspend fun saveRecording(serial: String, localPath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { screenDataSource.moveToHost(serial, RECORDING_PATH, localPath) }

    private companion object {
        const val RECORDING_PATH = "/sdcard/adb-desktop-recording.mp4"
    }
}
