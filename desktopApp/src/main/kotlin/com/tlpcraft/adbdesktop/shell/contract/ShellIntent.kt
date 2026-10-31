package com.tlpcraft.adbdesktop.shell.contract

sealed interface ShellIntent {
    data class DeviceSelected(val serial: String) : ShellIntent
}
