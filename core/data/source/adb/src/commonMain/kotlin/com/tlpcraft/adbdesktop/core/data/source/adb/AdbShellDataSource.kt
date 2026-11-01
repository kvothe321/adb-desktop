package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.TerminalSizeDto
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

interface AdbShellDataSource {
    /**
     * Runs one shell in a terminal on the device. [input] is sent as typed, the result is raw terminal output.
     * The first of [sizes] sets the size of the terminal, later ones resize it.
     */
    fun openSession(serial: String, input: Flow<String>, sizes: Flow<TerminalSizeDto>): Flow<String>
}

class AdbShellDataSourceImpl(private val client: AdbClient) : AdbShellDataSource {

    /**
     * Anything typed into the session would reach the program running in it, so resizing goes through a connection
     * of its own. That needs the name of the terminal, which the session prints first. Input waits for it, because
     * the terminal echoes whatever arrives and that would come before the name.
     */
    override fun openSession(serial: String, input: Flow<String>, sizes: Flow<TerminalSizeDto>): Flow<String> = channelFlow {
        val size = sizes.first()
        val isStarted = CompletableDeferred<Unit>()
        val header = StringBuilder()
        var resizer: Job? = null
        val delayedInput = flow {
            isStarted.await()
            emitAll(input)
        }

        client.interact(delayedInput, "-s", serial, "shell", "-tt", startScript(size)).collect { chunk ->
            if (isStarted.isCompleted) {
                send(chunk)
                return@collect
            }
            header.append(chunk)
            val lineEnd = header.indexOf("\n")
            if (lineEnd < 0) return@collect

            isStarted.complete(Unit)
            val terminal = header.substring(0, lineEnd).trim()
            if (terminal.startsWith(TERMINAL_PREFIX)) resizer = followSizes(serial, terminal, size, sizes)
            val output = if (resizer != null) header.substring(lineEnd + 1) else header.toString()
            if (output.isNotEmpty()) send(output)
        }
        if (!isStarted.isCompleted && header.isNotEmpty()) send(header.toString())
        resizer?.cancel()
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.followSizes(
        serial: String,
        terminal: String,
        size: TerminalSizeDto,
        sizes: Flow<TerminalSizeDto>,
    ): Job = launch {
        sizes.dropWhile { it == size }.debounce(RESIZE_DELAY).collect { (rows, columns) ->
            runCatching { client.shell(serial, "stty", "-F", terminal, "rows", "$rows", "cols", "$columns") }
        }
    }

    private fun startScript(size: TerminalSizeDto): String = "export TERM=$TERMINAL_TYPE; stty rows ${size.rows} cols ${size.columns}; tty; exec sh"

    private companion object {
        const val TERMINAL_TYPE = "xterm-256color"
        const val TERMINAL_PREFIX = "/dev/"
        val RESIZE_DELAY = 200.milliseconds
    }
}
