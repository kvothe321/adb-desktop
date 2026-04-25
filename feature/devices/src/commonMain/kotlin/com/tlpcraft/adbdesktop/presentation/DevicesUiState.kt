package com.tlpcraft.adbdesktop.presentation

import com.tlpcraft.adbdesktop.domain.model.AdbDevice

sealed interface DevicesUiState {
    data object Loading : DevicesUiState

    data class Content(val devices: List<AdbDevice>) : DevicesUiState

    data class Error(val message: String) : DevicesUiState
}
