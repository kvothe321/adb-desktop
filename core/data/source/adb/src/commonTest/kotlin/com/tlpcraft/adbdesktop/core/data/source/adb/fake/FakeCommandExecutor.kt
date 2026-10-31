package com.tlpcraft.adbdesktop.core.data.source.adb.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.process.BinaryCommandResult
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandResult
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/** Answers commands by their arguments (the executable path excluded). Unknown commands succeed with no output. */
class FakeCommandExecutor : CommandExecutor {
    private val results = mutableMapOf<List<String>, CommandResult>()
    private val streams = mutableMapOf<List<String>, List<String>>()
    private val binaryResults = mutableMapOf<List<String>, ByteArray>()
    val executed = mutableListOf<List<String>>()

    /** When set, [execute] never returns, like a device command that hangs. */
    var hangs = false

    fun respond(
        vararg args: String,
        stdout: String = "",
        stderr: String = "",
        exitCode: Int = 0,
    ) {
        results[args.asList()] = CommandResult(exitCode, stdout, stderr)
    }

    fun respondStream(vararg args: String, chunks: List<String>) {
        streams[args.asList()] = chunks
    }

    fun respondBinary(vararg args: String, stdout: ByteArray) {
        binaryResults[args.asList()] = stdout
    }

    override suspend fun execute(command: List<String>): CommandResult {
        val args = command.drop(1)
        executed += args
        if (hangs) awaitCancellation()
        return results[args] ?: CommandResult(exitCode = 0, stdout = "", stderr = "")
    }

    override suspend fun executeBinary(command: List<String>): BinaryCommandResult {
        val args = command.drop(1)
        executed += args
        return BinaryCommandResult(exitCode = 0, stdout = binaryResults[args] ?: ByteArray(0), stderr = "")
    }

    override fun stream(command: List<String>): Flow<String> = streams[command.drop(1)].orEmpty().asFlow()

    override fun interact(command: List<String>, input: Flow<String>): Flow<String> = stream(command)
}
