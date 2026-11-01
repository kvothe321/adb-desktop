package com.tlpcraft.adbdesktop.feature.devices.component

import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.device_connection_emulator
import com.tlpcraft.adbdesktop.feature.devices.resources.device_connection_usb
import com.tlpcraft.adbdesktop.feature.devices.resources.device_connection_wireless
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_authorizing
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_bootloader
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_connecting
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_no_permissions
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_offline
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_online
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_recovery
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_sideload
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_unauthorized
import com.tlpcraft.adbdesktop.feature.devices.resources.device_state_unknown
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import org.jetbrains.compose.resources.StringResource

internal val DeviceState.label: StringResource
    get() = when (this) {
        DeviceState.ONLINE -> Res.string.device_state_online
        DeviceState.OFFLINE -> Res.string.device_state_offline
        DeviceState.UNAUTHORIZED -> Res.string.device_state_unauthorized
        DeviceState.AUTHORIZING -> Res.string.device_state_authorizing
        DeviceState.CONNECTING -> Res.string.device_state_connecting
        DeviceState.NO_PERMISSIONS -> Res.string.device_state_no_permissions
        DeviceState.BOOTLOADER -> Res.string.device_state_bootloader
        DeviceState.RECOVERY -> Res.string.device_state_recovery
        DeviceState.SIDELOAD -> Res.string.device_state_sideload
        DeviceState.UNKNOWN -> Res.string.device_state_unknown
    }

internal val DeviceState.tone: Tone
    get() = when (this) {
        DeviceState.ONLINE -> Tone.Success
        DeviceState.UNAUTHORIZED, DeviceState.NO_PERMISSIONS -> Tone.Warning
        DeviceState.OFFLINE -> Tone.Error
        DeviceState.AUTHORIZING, DeviceState.CONNECTING -> Tone.Info
        DeviceState.BOOTLOADER, DeviceState.RECOVERY, DeviceState.SIDELOAD, DeviceState.UNKNOWN -> Tone.Neutral
    }

internal val ConnectionType.icon: IconName
    get() = when (this) {
        ConnectionType.USB -> IconName.Devices
        ConnectionType.WIRELESS -> IconName.Wifi
        ConnectionType.EMULATOR -> IconName.Mirror
    }

internal val ConnectionType.label: StringResource
    get() = when (this) {
        ConnectionType.USB -> Res.string.device_connection_usb
        ConnectionType.WIRELESS -> Res.string.device_connection_wireless
        ConnectionType.EMULATOR -> Res.string.device_connection_emulator
    }

private const val HIGH_USAGE_PERCENT = 80
private const val ELEVATED_USAGE_PERCENT = 60
private const val LOW_BATTERY_PERCENT = 20
private const val MEDIUM_BATTERY_PERCENT = 50

internal fun batteryTone(level: Int): Tone = when {
    level <= LOW_BATTERY_PERCENT -> Tone.Error
    level <= MEDIUM_BATTERY_PERCENT -> Tone.Warning
    else -> Tone.Success
}

internal fun cpuTone(usagePercent: Float): Tone = when {
    usagePercent >= HIGH_USAGE_PERCENT -> Tone.Error
    usagePercent >= ELEVATED_USAGE_PERCENT -> Tone.Warning
    else -> Tone.Success
}
