package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import kotlinx.coroutines.CompletableDeferred

class FakeScreenRepository : ScreenRepository {
    var screenshotResult: Outcome<ByteArray, DeviceError> = Outcome.Success(byteArrayOf(1, 2))
    val savedRecordings = mutableListOf<String>()
    private var recording = CompletableDeferred<Unit>()

    override suspend fun captureScreenshot(serial: String): Outcome<ByteArray, DeviceError> = screenshotResult

    override suspend fun recordScreen(serial: String): Outcome<Unit, DeviceError> {
        recording = CompletableDeferred()
        recording.await()
        return Outcome.Success(Unit)
    }

    override suspend fun stopRecording(serial: String): Outcome<Unit, DeviceError> {
        recording.complete(Unit)
        return Outcome.Success(Unit)
    }

    override suspend fun saveRecording(serial: String, localPath: String): Outcome<Unit, DeviceError> {
        savedRecordings += localPath
        return Outcome.Success(Unit)
    }
}
