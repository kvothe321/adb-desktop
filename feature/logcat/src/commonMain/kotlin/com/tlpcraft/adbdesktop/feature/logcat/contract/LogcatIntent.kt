package com.tlpcraft.adbdesktop.feature.logcat.contract

import com.tlpcraft.adbdesktop.core.domain.model.LogLevel

sealed interface LogcatIntent {
    data object ScreenOpened : LogcatIntent

    data object ScreenClosed : LogcatIntent

    data class LevelSelected(val level: LogLevel) : LogcatIntent

    data class QueryChanged(val value: String) : LogcatIntent

    data object PauseToggled : LogcatIntent

    data object ClearClicked : LogcatIntent

    data object RetryClicked : LogcatIntent
}
