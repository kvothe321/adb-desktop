package com.tlpcraft.adbdesktop.presentation

import com.tlpcraft.adbdesktop.domain.model.CpuInfo

/**
 * Presentation model for a single ADB device row, bundling identity fields with
 * an optionally loaded [CpuInfo] snapshot.
 *
 * [cpuInfo] is `null` while the first CPU poll has not yet completed, or when the
 * device is not in the `device` state (e.g. `offline`, `unauthorized`).
 */
data class DeviceUiModel(
    val serial: String,
    val status: String,
    val cpuInfo: CpuInfo?
)

sealed interface DevicesUiState {
    data object Loading : DevicesUiState

    data class Content(val devices: List<DeviceUiModel>) : DevicesUiState

    data class Error(val message: String) : DevicesUiState
}
