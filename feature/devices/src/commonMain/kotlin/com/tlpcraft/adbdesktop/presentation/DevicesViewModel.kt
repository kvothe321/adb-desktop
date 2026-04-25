package com.tlpcraft.adbdesktop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveDevicesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DevicesViewModel(
    observeDevices: ObserveDevicesUseCase
) : ViewModel() {

    val uiState: StateFlow<DevicesUiState> = observeDevices(Unit)
        .map { outcome ->
            outcome.fold(
                onSuccess = { DevicesUiState.Content(it) },
                onFailure = { DevicesUiState.Error(it.message) }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DevicesUiState.Loading
        )
}
