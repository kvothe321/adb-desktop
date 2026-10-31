package com.tlpcraft.adbdesktop.feature.devices.contract

import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState

data class DevicesUiState(
    val list: DeviceListState = DeviceListState.Loading,
    val stats: Map<String, DeviceStats> = emptyMap(),
    val selectedSerial: String? = null,
    val adbPathInput: String = "",
    val connectForm: ConnectFormState? = null,
)

sealed interface DeviceListState {
    data object Loading : DeviceListState

    data class Loaded(val devices: List<DeviceItem>) : DeviceListState

    data class Failed(val failure: DevicesFailure) : DeviceListState
}

data class DeviceItem(
    val serial: String,
    val name: String?,
    val state: DeviceState,
    val connectionType: ConnectionType,
) {
    val isOnline: Boolean
        get() = state == DeviceState.ONLINE

    val canDisconnect: Boolean
        get() = connectionType == ConnectionType.WIRELESS
}

data class DeviceStats(
    val androidVersion: String? = null,
    val batteryLevel: Int? = null,
    val cpuUsagePercent: Float? = null,
)

sealed interface DevicesFailure {
    data class AdbNotFound(val searchedLocations: List<String>) : DevicesFailure

    data class InvalidAdbPath(val path: String) : DevicesFailure

    data class Unavailable(val details: String) : DevicesFailure
}

enum class ConnectMode {
    Connect,
    Pair,
}

data class ConnectFormState(
    val mode: ConnectMode = ConnectMode.Connect,
    val address: String = "",
    val pairingCode: String = "",
    val inProgress: Boolean = false,
    val error: ConnectFormError? = null,
) {
    val canSubmit: Boolean
        get() = !inProgress && address.isNotBlank() && (mode == ConnectMode.Connect || pairingCode.isNotBlank())
}

sealed interface ConnectFormError {
    data object InvalidAddress : ConnectFormError

    data object InvalidPairingCode : ConnectFormError

    /** [reason] is adb's own message, shown verbatim. */
    data class Failed(val reason: String) : ConnectFormError
}
