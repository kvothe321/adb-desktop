package com.tlpcraft.adbdesktop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveDeviceInfoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModel(
    private val observeCpuInfo: ObserveCpuInfoUseCase,
    private val observeDeviceInfo: ObserveDeviceInfoUseCase,
) : ViewModel() {

    private val onlineDevices = MutableStateFlow<List<AdbDevice>>(emptyList())

    val cpuBySerial: StateFlow<Map<String, CpuInfo?>> = onlineDevices
        .flatMapLatest { devices ->
            if (devices.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    devices.map { device ->
                        observeCpuInfo(device.serial).map { outcome ->
                            device.serial to outcome.fold(onSuccess = { it }, onFailure = { null })
                        }
                    },
                ) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val deviceInfoBySerial: StateFlow<Map<String, DeviceInfo?>> = onlineDevices
        .flatMapLatest { devices ->
            if (devices.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    devices.map { device ->
                        observeDeviceInfo(device.serial).map { outcome ->
                            device.serial to outcome.fold(onSuccess = { it }, onFailure = { null })
                        }
                    },
                ) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    fun updateOnlineDevices(devices: List<AdbDevice>) {
        onlineDevices.value = devices.filter { it.status == "device" }
    }
}
