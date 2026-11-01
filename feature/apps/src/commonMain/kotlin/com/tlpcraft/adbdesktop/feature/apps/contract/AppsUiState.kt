package com.tlpcraft.adbdesktop.feature.apps.contract

import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppIcon

data class AppsUiState(
    val serial: String? = null,
    val filter: AppFilter = AppFilter.USER,
    val query: String = "",
    val list: AppListState = AppListState.NoDevice,
    val selectedPackage: String? = null,
    val details: AppDetailsState = AppDetailsState.Hidden,
    val runningAction: AppAction? = null,
    val pendingConfirmation: AppAction? = null,
    val isInstalling: Boolean = false,
    /** App icons by package. Apps missing here show the plain app glyph. */
    val icons: Map<String, AppIcon> = emptyMap(),
) {
    val visiblePackages: List<String>
        get() = (list as? AppListState.Loaded)?.packages.orEmpty().filter { it.contains(query.trim(), ignoreCase = true) }
}

sealed interface AppListState {
    data object NoDevice : AppListState

    data object Loading : AppListState

    data class Loaded(val packages: List<String>) : AppListState

    data class Failed(val details: String) : AppListState
}

sealed interface AppDetailsState {
    data object Hidden : AppDetailsState

    data object Loading : AppDetailsState

    data class Loaded(val details: AppDetails) : AppDetailsState

    data class Failed(val details: String) : AppDetailsState
}

/** Actions that destroy user data and therefore ask for confirmation first. */
val AppAction.isDestructive: Boolean
    get() = this == AppAction.CLEAR_DATA || this == AppAction.UNINSTALL
