package com.tlpcraft.adbdesktop.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveCpuInfoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Handles CPU-stats polling for the Devices screen.
 *
 * Intentionally knows nothing about device-list observation or selection — both
 * are owned by the app-level SharedDeviceViewModel and passed into [DevicesScreen]
 * as plain parameters. This ViewModel's only job is to keep per-device CPU flows
 * alive for as long as [DevicesScreen] is visible, and to expose the latest
 * [CpuInfo] keyed by device serial.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModel(
    private val observeCpuInfo: ObserveCpuInfoUseCase
) : ViewModel() {
    /** The set of online devices whose CPU stats should be polled. */
    private val onlineDevices = MutableStateFlow<List<AdbDevice>>(emptyList())

    /**
     * Latest [CpuInfo] snapshot per device serial.
     * A missing key means the first poll has not yet completed for that device.
     */
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
                    }
                ) { pairs -> pairs.toMap() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    /**
     * Updates the set of devices for which CPU stats are polled.
     * Should be called from the screen whenever the online-device list changes.
     * Only devices with `status == "device"` are polled.
     */
    fun updateOnlineDevices(devices: List<AdbDevice>) {
        onlineDevices.value = devices.filter { it.status == "device" }
    }
}
