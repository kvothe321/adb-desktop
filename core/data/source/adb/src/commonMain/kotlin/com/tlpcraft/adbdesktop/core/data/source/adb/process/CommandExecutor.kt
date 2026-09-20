package com.tlpcraft.adbdesktop.core.data.source.adb.process

import kotlinx.coroutines.flow.Flow

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

class BinaryCommandResult(val exitCode: Int, val stdout: ByteArray, val stderr: String)

/** Cancelling any call kills the process it started. */
interface CommandExecutor {
    suspend fun execute(command: List<String>): CommandResult

    suspend fun executeBinary(command: List<String>): BinaryCommandResult

    /** Streams stdout in arbitrary chunks until the process exits. A non-zero exit fails the flow with the collected stderr. */
    fun stream(command: List<String>): Flow<String>

    /** Writes each [input] value to stdin and streams stdout merged with stderr until the process exits. */
    fun interact(command: List<String>, input: Flow<String>): Flow<String>
}
