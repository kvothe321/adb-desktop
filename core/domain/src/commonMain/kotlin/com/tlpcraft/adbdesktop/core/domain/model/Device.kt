package com.tlpcraft.adbdesktop.core.domain.model

data class Device(
    val serial: String,
    val state: DeviceState,
    val connectionType: ConnectionType,
    val model: String? = null,
    val product: String? = null,
) {
    val isOnline: Boolean
        get() = state == DeviceState.ONLINE
}
