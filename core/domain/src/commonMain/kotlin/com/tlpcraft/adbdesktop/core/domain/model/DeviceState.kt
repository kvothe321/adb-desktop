package com.tlpcraft.adbdesktop.core.domain.model

enum class DeviceState {
    ONLINE,
    OFFLINE,
    UNAUTHORIZED,
    AUTHORIZING,
    CONNECTING,
    NO_PERMISSIONS,
    BOOTLOADER,
    RECOVERY,
    SIDELOAD,
    UNKNOWN,
}
