package com.tlpcraft.adbdesktop.presentation

import com.tlpcraft.adbdesktop.domain.model.AppDetails

sealed interface AppDetailsState {
    data object Empty : AppDetailsState

    data object Loading : AppDetailsState

    data class Ready(val details: AppDetails) : AppDetailsState

    data class Error(val message: String) : AppDetailsState
}
