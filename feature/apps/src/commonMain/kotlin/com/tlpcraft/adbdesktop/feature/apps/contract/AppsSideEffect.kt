package com.tlpcraft.adbdesktop.feature.apps.contract

import com.tlpcraft.adbdesktop.core.domain.model.AppAction

sealed interface AppsSideEffect {
    data class ActionSucceeded(val action: AppAction, val packageName: String) : AppsSideEffect

    /** [reason] is the device's message, shown as it is. */
    data class ActionFailed(val action: AppAction, val packageName: String, val reason: String) : AppsSideEffect

    data class Installed(val fileCount: Int) : AppsSideEffect

    /** [reason] is the package manager's message, shown as it is. */
    data class InstallFailed(val reason: String) : AppsSideEffect
}
