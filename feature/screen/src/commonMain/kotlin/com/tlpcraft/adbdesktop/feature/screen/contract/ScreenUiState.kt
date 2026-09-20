package com.tlpcraft.adbdesktop.feature.screen.contract

import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame

data class ScreenUiState(
    val serial: String? = null,
    val screenshot: Screenshot? = null,
    val isCapturing: Boolean = false,
    val recording: RecordingState = RecordingState.Idle,
    val recordingSeconds: Int = 0,
    val isMirrorOn: Boolean = false,
    val mirror: MirrorState = MirrorState.Off,
) {
    val canCapture: Boolean
        get() = serial != null && !isCapturing

    val canRecord: Boolean
        get() = serial != null && recording == RecordingState.Idle
}

/** A plain class on purpose: arrays compare by identity in data classes, and every capture is a new image anyway. */
class Screenshot(val png: ByteArray)

sealed interface MirrorState {
    data object Off : MirrorState

    data object Starting : MirrorState

    /** A plain class on purpose: every frame is a new image, so comparing the pixels would only cost time. */
    class Live(val frame: MirrorFrame) : MirrorState
}

enum class RecordingState {
    Idle,
    Recording,
    Stopping,
    Saving,
}
