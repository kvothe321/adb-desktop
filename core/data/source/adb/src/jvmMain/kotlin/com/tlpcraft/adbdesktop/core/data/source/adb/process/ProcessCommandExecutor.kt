package com.tlpcraft.adbdesktop.core.data.source.adb.process

import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import java.io.InputStream
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

class ProcessCommandExecutor(private val dispatchers: DispatcherProvider) : CommandExecutor {

    override suspend fun execute(command: List<String>): CommandResult = withContext(dispatchers.io) {
        val process = ProcessBuilder(command).start()
        try {
            coroutineScope {
                // Both pipes are drained concurrently: a full stderr buffer would otherwise block the process.
                val stdout = async { process.inputStream.readAll() }
                val stderr = async { process.errorStream.readAll() }
                val exitCode = runInterruptible { process.waitFor() }
                CommandResult(exitCode, stdout.await(), stderr.await())
            }
        } finally {
            process.destroy()
        }
    }

    override fun stream(command: List<String>): Flow<String> = flow {
        val process = ProcessBuilder(command).start()
        try {
            coroutineScope {
                val stderr = async { process.errorStream.readAll() }
                val reader = process.inputStream.bufferedReader()
                val buffer = CharArray(BUFFER_SIZE)
                while (true) {
                    val read = runInterruptible { reader.read(buffer) }
                    if (read < 0) break
                    emit(String(buffer, 0, read))
                }
                val exitCode = runInterruptible { process.waitFor() }
                if (exitCode != 0) throw AdbException.CommandFailed(command.drop(1), exitCode, stderr.await().trim())
            }
        } finally {
            process.destroy()
        }
    }.flowOn(dispatchers.io)

    private suspend fun InputStream.readAll(): String = runInterruptible { bufferedReader().use { it.readText() } }

    private companion object {
        const val BUFFER_SIZE = 4_096
    }
}
