package com.tlpcraft.adbdesktop.feature.screen.contract

data class ScreenUiState(
    val serial: String? = null,
    val screenshot: Screenshot? = null,
    val isCapturing: Boolean = false,
    val recording: RecordingState = RecordingState.Idle,
) {
    val canCapture: Boolean
        get() = serial != null && !isCapturing

    val canRecord: Boolean
        get() = serial != null && recording == RecordingState.Idle
}

/** A plain class on purpose: arrays compare by identity in data classes, and every capture is a new image anyway. */
class Screenshot(val png: ByteArray)

enum class RecordingState {
    Idle,
    Recording,
    Stopping,
    Saving,
}
