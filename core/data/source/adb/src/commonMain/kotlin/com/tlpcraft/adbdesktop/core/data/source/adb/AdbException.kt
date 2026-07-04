package com.tlpcraft.adbdesktop.core.data.source.adb

sealed class AdbException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ExecutableNotFound(val searchedLocations: List<String>) : AdbException("adb not found in: ${searchedLocations.joinToString()}")

    class InvalidExecutable(val path: String) : AdbException("Not an executable file: $path")

    class DeviceNotFound(val serial: String?) : AdbException("Device not found: ${serial ?: "any"}")

    class CommandFailed(val command: List<String>, val exitCode: Int, val output: String) :
        AdbException("'${command.joinToString(" ")}' exited with $exitCode: $output")

    class ConnectFailed(val address: String, val reason: String) : AdbException("Could not connect to $address: $reason")

    class PairFailed(val reason: String) : AdbException("Pairing failed: $reason")
}
