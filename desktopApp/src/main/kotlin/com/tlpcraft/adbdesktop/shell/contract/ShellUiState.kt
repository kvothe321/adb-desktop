package com.tlpcraft.adbdesktop.shell.contract

data class ShellUiState(
    val devices: List<DeviceOption> = emptyList(),
    val selectedSerial: String? = null,
) {
    val selectedDevice: DeviceOption?
        get() = devices.firstOrNull { it.serial == selectedSerial }
}

data class DeviceOption(val serial: String, val name: String)
