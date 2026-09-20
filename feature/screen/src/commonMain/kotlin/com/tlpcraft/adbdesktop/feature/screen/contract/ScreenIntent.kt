package com.tlpcraft.adbdesktop.feature.screen.contract

sealed interface ScreenIntent {
    data object CaptureClicked : ScreenIntent

    data object SaveScreenshotClicked : ScreenIntent

    data object CopyScreenshotClicked : ScreenIntent

    data object RecordClicked : ScreenIntent

    data object StopClicked : ScreenIntent

    data object MirrorClicked : ScreenIntent

    data class VisibilityChanged(val isVisible: Boolean) : ScreenIntent
}
