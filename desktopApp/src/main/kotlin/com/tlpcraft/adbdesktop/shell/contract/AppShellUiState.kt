package com.tlpcraft.adbdesktop.shell.contract

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode

data class AppShellUiState(
    val devices: List<DeviceOption> = emptyList(),
    val selectedSerial: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isPhonePanelShown: Boolean = true,
) {
    val selectedDevice: DeviceOption?
        get() = devices.firstOrNull { it.serial == selectedSerial }

    fun isDarkTheme(isSystemDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

data class DeviceOption(val serial: String, val name: String)
