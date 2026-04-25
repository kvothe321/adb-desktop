package com.tlpcraft.adbdesktop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModel(
    observeDevices: ObserveDevicesUseCase,
    private val observeCpuInfo: ObserveCpuInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<DevicesUiState> = observeDevices(Unit)
        .flatMapLatest { outcome ->
            outcome.fold(
                onFailure = { flowOf(DevicesUiState.Error(it.message)) },
                onSuccess = { devices -> devicesWithCpuInfo(devices) }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DevicesUiState.Loading
        )

    /**
     * For each connected device, subscribes to its CPU info stream.
     * Devices that are not in `device` state show `cpuInfo = null` immediately
     * without starting a CPU poll.
     */
    private fun devicesWithCpuInfo(devices: List<AdbDevice>): Flow<DevicesUiState.Content> {
        if (devices.isEmpty()) return flowOf(DevicesUiState.Content(emptyList()))

        val onlineDevices = devices.filter { it.status == "device" }

        if (onlineDevices.isEmpty()) {
            return flowOf(
                DevicesUiState.Content(
                    devices.map { DeviceUiModel(serial = it.serial, status = it.status, cpuInfo = null) }
                )
            )
        }

        val cpuFlows = onlineDevices.map { device ->
            observeCpuInfo(device.serial).map { cpuOutcome ->
                device.serial to cpuOutcome.fold(onSuccess = { it }, onFailure = { null })
            }
        }

        return combine(cpuFlows) { results ->
            val cpuBySerial = results.toMap()
            DevicesUiState.Content(
                devices.map { device ->
                    DeviceUiModel(
                        serial = device.serial,
                        status = device.status,
                        cpuInfo = cpuBySerial[device.serial]
                    )
                }
            )
        }
    }
}
