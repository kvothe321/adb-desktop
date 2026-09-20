package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError

internal fun Throwable.toDeviceError(): DeviceError = when (this) {
    is AdbException.ExecutableNotFound -> DeviceError.AdbNotFound(searchedLocations)
    is AdbException.InvalidExecutable -> DeviceError.InvalidAdbPath(path)
    is AdbException.DeviceNotFound -> DeviceError.DeviceNotFound(serial.orEmpty())
    is AdbException.ConnectFailed -> DeviceError.ConnectionFailed(address, reason)
    is AdbException.PairFailed -> DeviceError.PairingFailed(reason)
    is AdbException.CommandFailed -> DeviceError.CommandFailed(output.ifBlank { message.orEmpty() }, cause = this)
    else -> DeviceError.CommandFailed(message ?: this::class.simpleName.orEmpty(), cause = this)
}
