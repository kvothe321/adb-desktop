package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState

private const val EMULATOR_PREFIX = "emulator-"
private const val MDNS_SERVICE_MARKER = "._adb-tls-connect._tcp"

internal fun DeviceDto.toDomain() = Device(
    serial = serial,
    state = state.toDeviceState(),
    connectionType = connectionType(),
    model = model?.replace('_', ' '),
    product = product,
)

private fun String.toDeviceState(): DeviceState = when (this) {
    "device" -> DeviceState.ONLINE
    "offline" -> DeviceState.OFFLINE
    "unauthorized" -> DeviceState.UNAUTHORIZED
    "authorizing" -> DeviceState.AUTHORIZING
    "connecting" -> DeviceState.CONNECTING
    "no permissions" -> DeviceState.NO_PERMISSIONS
    "bootloader" -> DeviceState.BOOTLOADER
    "recovery", "rescue" -> DeviceState.RECOVERY
    "sideload" -> DeviceState.SIDELOAD
    else -> DeviceState.UNKNOWN
}

/** adb serials encode the transport: `emulator-5554`, `host:port` or an mDNS service name for wireless. */
private fun DeviceDto.connectionType(): ConnectionType = when {
    serial.startsWith(EMULATOR_PREFIX) -> ConnectionType.EMULATOR
    serial.contains(':') || serial.contains(MDNS_SERVICE_MARKER) -> ConnectionType.WIRELESS
    else -> ConnectionType.USB
}
