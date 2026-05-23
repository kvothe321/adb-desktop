package com.tlpcraft.adbdesktop.core.data.source.adb.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/** Answers commands by their arguments (the executable path excluded). Unknown commands succeed with no output. */
class FakeCommandExecutor : CommandExecutor {
    private val results = mutableMapOf<List<String>, CommandResult>()
    private val streams = mutableMapOf<List<String>, List<String>>()
    val executed = mutableListOf<List<String>>()

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

    override suspend fun execute(command: List<String>): CommandResult {
        val args = command.drop(1)
        executed += args
        return results[args] ?: CommandResult(exitCode = 0, stdout = "", stderr = "")
    }

    override fun stream(command: List<String>): Flow<String> = streams[command.drop(1)].orEmpty().asFlow()
}
