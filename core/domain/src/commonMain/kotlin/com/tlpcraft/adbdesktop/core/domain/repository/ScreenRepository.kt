package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import kotlinx.coroutines.flow.Flow

interface ScreenRepository {
    /** The current screen as PNG bytes. */
    suspend fun captureScreenshot(serial: String): Outcome<ByteArray, DeviceError>

    /** Suspends while the device records, until [stopRecording] or the system limit ends it. */
    suspend fun recordScreen(serial: String): Outcome<Unit, DeviceError>

    suspend fun stopRecording(serial: String): Outcome<Unit, DeviceError>

    /** The live screen while collected. Slow collectors skip frames rather than fall behind. */
    fun observeMirror(serial: String): Flow<Outcome<MirrorFrame, DeviceError>>

    /** Moves the last recording off the device. */
    suspend fun saveRecording(serial: String, localPath: String): Outcome<Unit, DeviceError>
}
