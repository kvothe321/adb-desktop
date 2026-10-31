package com.tlpcraft.adbdesktop.feature.apps.contract

import com.tlpcraft.adbdesktop.core.domain.model.AppAction

sealed interface AppsSideEffect {
    data class ActionSucceeded(val action: AppAction, val packageName: String) : AppsSideEffect

    data class ActionFailed(val action: AppAction, val packageName: String) : AppsSideEffect
}
