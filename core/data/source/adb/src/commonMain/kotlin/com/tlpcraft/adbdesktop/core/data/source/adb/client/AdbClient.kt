package com.tlpcraft.adbdesktop.core.data.source.adb.client

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbBinaryResolver
import com.tlpcraft.adbdesktop.core.data.source.adb.process.BinaryCommandResult
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.process.CommandResult
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Runs adb commands against the resolved executable and turns failures into [AdbException]s. */
class AdbClient(
    private val binaryResolver: AdbBinaryResolver,
    private val executor: CommandExecutor,
    private val logger: AppLogger,
) {
    /** Runs a command and returns its result without judging the exit code. */
    suspend fun execute(vararg args: String): CommandResult = execute(args.asList())

    suspend fun execute(args: List<String>): CommandResult = executor.execute(command(args))

    /** Runs a command and returns stdout, failing on a non-zero exit code. */
    suspend fun run(args: List<String>): String {
        val command = command(args)
        val result = executor.execute(command)
        if (result.exitCode != 0) throw failure(command, result).also { logger.warn(TAG, it.message.orEmpty()) }
        return result.stdout
    }

    suspend fun shell(serial: String, vararg args: String): String = shell(serial, args.asList())

    suspend fun shell(serial: String, args: List<String>): String = run(listOf("-s", serial, "shell") + args)

    /** Like [shell], but keeps stdout of commands that fail for only some of their arguments. */
    suspend fun shellResult(serial: String, args: List<String>): CommandResult = executor.execute(command(listOf("-s", serial, "shell") + args))

    suspend fun binary(vararg args: String): ByteArray {
        val command = command(args.asList())
        val result: BinaryCommandResult = executor.executeBinary(command)
        if (result.exitCode != 0) throw failure(command, CommandResult(result.exitCode, stdout = "", stderr = result.stderr))
        return result.stdout
    }

    fun stream(vararg args: String): Flow<String> = stream(args.asList())

    fun stream(args: List<String>): Flow<String> = flow {
        executor.stream(command(args)).collect { emit(it) }
    }

    fun interact(input: Flow<String>, vararg args: String): Flow<String> = flow {
        executor.interact(command(args.asList()), input).collect { emit(it) }
    }

    suspend fun executablePath(): String = binaryResolver.resolve()

    private suspend fun command(args: List<String>): List<String> = listOf(binaryResolver.resolve()) + args

    private fun failure(command: List<String>, result: CommandResult): AdbException {
        val output = result.stderr.ifBlank { result.stdout }.trim()
        val missingSerial = DEVICE_NOT_FOUND.find(output)?.groupValues?.get(1)
        return when {
            missingSerial != null -> AdbException.DeviceNotFound(missingSerial)
            NO_DEVICES in output -> AdbException.DeviceNotFound(serial = null)
            else -> AdbException.CommandFailed(command.drop(1), result.exitCode, output)
        }
    }

    private companion object {
        const val TAG = "AdbClient"
        const val NO_DEVICES = "no devices/emulators found"
        val DEVICE_NOT_FOUND = Regex("""device '([^']+)' not found""")
    }
}
