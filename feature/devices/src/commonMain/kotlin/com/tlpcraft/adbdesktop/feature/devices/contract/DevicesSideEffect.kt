package com.tlpcraft.adbdesktop.feature.devices.contract

sealed interface DevicesSideEffect {
    data class Connected(val address: String) : DevicesSideEffect

    data class Paired(val address: String) : DevicesSideEffect

    data class Disconnected(val serial: String) : DevicesSideEffect

    data class DisconnectFailed(val serial: String) : DevicesSideEffect
}
