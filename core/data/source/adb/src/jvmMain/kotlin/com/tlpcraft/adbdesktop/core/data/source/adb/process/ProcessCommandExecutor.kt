package com.tlpcraft.adbdesktop.core.data.source.adb.process

import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessCommandExecutor(private val dispatchers: DispatcherProvider) : CommandExecutor {

    override suspend fun execute(command: List<String>): CommandResult = withContext(dispatchers.io) {
        runToCompletion(command) { process ->
            val stderr = async { process.errorStream.readText() }
            val stdout = process.inputStream.readText()
            CommandResult(process.waitFor(), stdout, stderr.await())
        }
    }

    override suspend fun executeBinary(command: List<String>): BinaryCommandResult = withContext(dispatchers.io) {
        runToCompletion(command) { process ->
            val stderr = async { process.errorStream.readText() }
            val stdout = process.inputStream.use(InputStream::readBytes)
            BinaryCommandResult(process.waitFor(), stdout, stderr.await())
        }
    }

    override fun stream(command: List<String>): Flow<String> = channelFlow {
        val process = ProcessBuilder(command).start()
        val reaper = killOnCancellation(process)
        val stderr = async { process.errorStream.readText() }

        sendOutputOf(process)

        val exitCode = process.waitFor()
        if (exitCode != 0) throw AdbException.CommandFailed(command.drop(1), exitCode, stderr.await().trim())
        reaper.cancel()
    }.flowOn(dispatchers.io)

    override fun interact(command: List<String>, input: Flow<String>): Flow<String> = channelFlow {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val reaper = killOnCancellation(process)
        val writer = launch {
            input.collect { text ->
                val written = runCatching {
                    process.outputStream.write(text.toByteArray())
                    process.outputStream.flush()
                }
                if (written.isFailure) cancel()
            }
        }

        sendOutputOf(process)

        writer.cancel()
        reaper.cancel()
    }.flowOn(dispatchers.io)

    private suspend fun <T> runToCompletion(command: List<String>, block: suspend CoroutineScope.(Process) -> T): T = coroutineScope {
        val process = ProcessBuilder(command).start()
        val reaper = killOnCancellation(process)
        try {
            block(process).also { reaper.cancel() }
        } catch (failure: IOException) {
            ensureActive()
            throw failure
        }
    }

    /**
     * Reads on a blocked process stream ignore coroutine cancellation; killing the process is what unblocks them.
     * That can fail the read with an [IOException], which readers must take for the cancellation it really is.
     */
    private fun CoroutineScope.killOnCancellation(process: Process): Job = launch(start = CoroutineStart.UNDISPATCHED) {
        try {
            awaitCancellation()
        } finally {
            process.destroyForcibly()
        }
    }

    private suspend fun ProducerScope<String>.sendOutputOf(process: Process) {
        val reader = process.inputStream.bufferedReader()
        val buffer = CharArray(BUFFER_SIZE)
        try {
            generateSequence { reader.read(buffer) }
                .takeWhile { it >= 0 }
                .forEach { read -> send(String(buffer, 0, read)) }
        } catch (failure: IOException) {
            ensureActive()
            throw failure
        }
    }

    private fun InputStream.readText(): String = bufferedReader().use { it.readText() }

    private companion object {
        const val BUFFER_SIZE = 4_096
    }
}
