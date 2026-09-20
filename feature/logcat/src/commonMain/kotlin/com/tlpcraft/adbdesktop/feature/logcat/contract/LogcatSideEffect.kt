package com.tlpcraft.adbdesktop.feature.logcat.contract

sealed interface LogcatSideEffect {
    data object ClearFailed : LogcatSideEffect

    data class Exported(val entryCount: Int, val path: String) : LogcatSideEffect
}
