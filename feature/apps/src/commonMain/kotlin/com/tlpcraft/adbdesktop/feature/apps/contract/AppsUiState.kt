package com.tlpcraft.adbdesktop.feature.apps.contract

import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter

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
    /** App names and icons by package. Apps missing here show a name made from the package and the plain app glyph. */
    val appearances: Map<String, AppAppearance> = emptyMap(),
) {
    /** The query matches the package or the app's own name. */
    val visiblePackages: List<String>
        get() {
            val text = query.trim()
            return (list as? AppListState.Loaded)?.packages.orEmpty().filter { packageName ->
                packageName.contains(text, ignoreCase = true) || appearances[packageName]?.label?.contains(text, ignoreCase = true) == true
            }
        }
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
