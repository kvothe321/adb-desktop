package com.tlpcraft.adbdesktop.feature.devices.contract

sealed interface DevicesSideEffect {
    data class Connected(val address: String) : DevicesSideEffect

    data class Paired(val address: String) : DevicesSideEffect

    data class Disconnected(val serial: String) : DevicesSideEffect

    data class DisconnectFailed(val serial: String) : DevicesSideEffect

    data object ScreenshotCopied : DevicesSideEffect

    data class Installed(val count: Int) : DevicesSideEffect

    /** [details] is the message from the device, shown as it is. */
    data class ActionFailed(val details: String) : DevicesSideEffect

    /** The user asked for another section of the app, which the window opens. */
    data class SectionRequested(val action: QuickAction) : DevicesSideEffect
}
