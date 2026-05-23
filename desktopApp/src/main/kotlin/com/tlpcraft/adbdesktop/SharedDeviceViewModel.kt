package com.tlpcraft.adbdesktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SharedDeviceViewModel(
    observeDevices: ObserveDevicesUseCase,
    observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val selectDevice: SelectDeviceUseCase,
) : ViewModel() {

    val devicesState: StateFlow<Outcome<List<Device>, DeviceError>?> = observeDevices(Unit)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    val selectedDevice: StateFlow<Device?> = observeSelectedDevice(Unit)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    fun select(serial: String) {
        viewModelScope.launch { selectDevice(serial) }
    }

    fun deselect() {
        viewModelScope.launch { selectDevice(null) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
