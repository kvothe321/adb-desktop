package com.tlpcraft.adbdesktop.feature.screen.contract

sealed interface ScreenIntent {
    data object CaptureClicked : ScreenIntent

    data object SaveScreenshotClicked : ScreenIntent

    data object CopyScreenshotClicked : ScreenIntent

    data object RecordClicked : ScreenIntent

    data object StopClicked : ScreenIntent
}
