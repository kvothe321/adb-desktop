package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class FakeScreenRepository : ScreenRepository {
    var screenshotResult: Outcome<ByteArray, DeviceError> = Outcome.Success(byteArrayOf(1, 2))
    val savedRecordings = mutableListOf<String>()
    val mirror = MutableSharedFlow<Outcome<MirrorFrame, DeviceError>>(extraBufferCapacity = 8)
    val mirroredSerials = mutableListOf<String>()
    val mirrorInput = mutableListOf<MirrorInput>()
    var activeMirrors = 0
        private set
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

    override fun observeMirror(serial: String, input: Flow<MirrorInput>): Flow<Outcome<MirrorFrame, DeviceError>> = channelFlow {
        mirroredSerials += serial
        activeMirrors++
        try {
            launch { input.collect { mirrorInput += it } }
            mirror.collect { send(it) }
        } finally {
            activeMirrors--
        }
    }

    override suspend fun saveRecording(serial: String, localPath: String): Outcome<Unit, DeviceError> {
        savedRecordings += localPath
        return Outcome.Success(Unit)
    }
}
