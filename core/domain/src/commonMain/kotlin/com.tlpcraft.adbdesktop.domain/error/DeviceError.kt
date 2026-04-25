package com.tlpcraft.adbdesktop.domain.error

import com.tlpcraft.adbdesktop.domain.DomainError

sealed class DeviceError : DomainError {

    data class CommandFailed(override val cause: Throwable) : DeviceError() {
        override val message: String = "Failed to retrieve devices: ${cause.message}"
        override val isFatal: Boolean = false
    }
}
