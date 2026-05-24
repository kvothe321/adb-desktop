package com.tlpcraft.adbdesktop.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.getOrNull
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import com.tlpcraft.adbdesktop.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.shell.contract.ShellUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShellViewModel(
    private val observeDevices: ObserveDevicesUseCase,
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val selectDevice: SelectDeviceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShellUiState())
    val uiState: StateFlow<ShellUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeDevices(Unit).collect { outcome ->
                val options = outcome.getOrNull().orEmpty().filter { it.isOnline }.map { DeviceOption(it.serial, it.model ?: it.serial) }
                _uiState.update { it.copy(devices = options) }
            }
        }
        viewModelScope.launch {
            observeSelectedDevice(Unit).collect { device -> _uiState.update { it.copy(selectedSerial = device?.serial) } }
        }
    }

    fun reduceIntent(intent: ShellIntent) {
        when (intent) {
            is ShellIntent.DeviceSelected -> viewModelScope.launch { selectDevice(intent.serial) }
        }
    }
}
