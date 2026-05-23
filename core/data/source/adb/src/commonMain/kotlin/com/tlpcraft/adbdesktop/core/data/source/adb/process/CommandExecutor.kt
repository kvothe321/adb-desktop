package com.tlpcraft.adbdesktop.core.data.source.adb.process

import kotlinx.coroutines.flow.Flow

data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String)

interface CommandExecutor {
    suspend fun execute(command: List<String>): CommandResult

    /**
     * Streams stdout in arbitrary chunks until the process exits. The process is killed when the
     * collector is cancelled. A non-zero exit fails the flow with the collected stderr.
     */
    fun stream(command: List<String>): Flow<String>
}
