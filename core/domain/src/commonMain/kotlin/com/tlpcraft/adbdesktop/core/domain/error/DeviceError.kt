package com.tlpcraft.adbdesktop.core.domain.error

import com.tlpcraft.adbdesktop.core.common.DomainError

sealed interface DeviceError : DomainError {
    override val cause: Throwable?
        get() = null

    data class AdbNotFound(val searchedLocations: List<String>) : DeviceError {
        override val message: String = "adb executable not found, searched: ${searchedLocations.joinToString()}"
    }

    data class InvalidAdbPath(val path: String) : DeviceError {
        override val message: String = "Configured adb path is not an executable file: $path"
    }

    data class DeviceNotFound(val serial: String) : DeviceError {
        override val message: String = "Device not found: $serial"
    }

    data class InvalidEndpoint(val input: String) : DeviceError {
        override val message: String = "Not a valid host[:port] endpoint: $input"
    }

    data object InvalidPairingCode : DeviceError {
        override val message: String = "Pairing code must be 6 digits"
    }

    data class ConnectionFailed(val endpoint: String, val reason: String) : DeviceError {
        override val message: String = "Could not connect to $endpoint: $reason"
    }

    data class PairingFailed(val reason: String) : DeviceError {
        override val message: String = "Pairing failed: $reason"
    }

    data class CommandFailed(val details: String, override val cause: Throwable? = null) : DeviceError {
        override val message: String = "adb command failed: $details"
    }
}
