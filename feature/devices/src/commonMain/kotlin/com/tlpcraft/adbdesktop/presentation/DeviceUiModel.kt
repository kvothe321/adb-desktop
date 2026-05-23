package com.tlpcraft.adbdesktop.presentation

import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState

/**
 * Presentation model for a single ADB device row, bundling identity fields with
 * an optionally loaded [CpuInfo] snapshot.
 *
 * [cpuInfo] is `null` while the first CPU poll has not yet completed, or when the
 * device is not in the `device` state (e.g. `offline`, `unauthorized`).
 */
data class DeviceUiModel(
    val serial: String,
    val state: DeviceState,
    val cpuInfo: CpuInfo?,
    val modelName: String? = null,
    val androidVersion: String? = null,
    val batteryLevel: Int? = null,
) {
    val isOnline: Boolean
        get() = state == DeviceState.ONLINE
}
