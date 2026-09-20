package com.tlpcraft.adbdesktop.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.getOrNull
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.shell.contract.AppShellIntent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppShellViewModel(
    private val observeDevices: ObserveDevicesUseCase,
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val selectDevice: SelectDeviceUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppShellUiState())
    val uiState: StateFlow<AppShellUiState> = _uiState.asStateFlow()

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
        viewModelScope.launch {
            observeThemeMode(Unit).collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
    }

    fun reduceIntent(intent: AppShellIntent) {
        when (intent) {
            is AppShellIntent.DeviceSelected -> viewModelScope.launch { selectDevice(intent.serial) }
            is AppShellIntent.ThemeToggled -> viewModelScope.launch { setThemeMode(if (intent.isDarkShowing) ThemeMode.LIGHT else ThemeMode.DARK) }
        }
    }
}
