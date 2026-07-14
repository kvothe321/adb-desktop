package com.tlpcraft.adbdesktop.feature.logcat.contract

sealed interface LogcatSideEffect {
    data object ClearFailed : LogcatSideEffect
}
