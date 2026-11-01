package com.tlpcraft.adbdesktop.feature.devices.contract

sealed interface DevicesIntent {
    data class VisibilityChanged(val isVisible: Boolean) : DevicesIntent

    data class DeviceClicked(val serial: String) : DevicesIntent

    data class DisconnectClicked(val serial: String) : DevicesIntent

    data class AdbPathChanged(val value: String) : DevicesIntent

    data object AdbPathSubmitted : DevicesIntent

    data object ConnectFormOpened : DevicesIntent

    data object ConnectFormDismissed : DevicesIntent

    data class ConnectModeSelected(val mode: ConnectMode) : DevicesIntent

    data class AddressChanged(val value: String) : DevicesIntent

    data class PairingCodeChanged(val value: String) : DevicesIntent

    data object ConnectSubmitted : DevicesIntent

    data class QuickActionClicked(val action: QuickAction) : DevicesIntent
}
