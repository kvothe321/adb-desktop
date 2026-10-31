package com.tlpcraft.adbdesktop.feature.screen.contract

sealed interface ScreenSideEffect {
    data class ScreenshotSaved(val path: String) : ScreenSideEffect

    data class RecordingSaved(val path: String) : ScreenSideEffect

    data object RecordingDiscarded : ScreenSideEffect

    /** [details] is the message from the device, shown as it is. */
    data class Failed(val details: String) : ScreenSideEffect
}
