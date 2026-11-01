package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbMirrorDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorFrameDto
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Mirrors through scrcpy's server: it is pushed to the device, encodes the screen as H.264 and serves it on an abstract
 * socket reached with `adb forward`. A second connection to the same socket carries input. The server deletes its own jar
 * when the stream ends.
 */
class ScrcpyMirrorDataSource(private val client: AdbClient, private val logger: AppLogger) : AdbMirrorDataSource {
    private val serverFile: Path by lazy(::extractServer)

    override fun streamFrames(serial: String, control: Flow<MirrorControlDto>): Flow<MirrorFrameDto> = channelFlow {
        val sessionId = Random.nextInt(1, Int.MAX_VALUE)
        client.run(listOf("-s", serial, "push", serverFile.toString(), ScrcpyServerCommand.remotePath(sessionId)))
        val port = client.run(listOf("-s", serial, "forward", "tcp:0", "localabstract:${ScrcpyServerCommand.socketName(sessionId)}")).trim()
        try {
            val server = launch { client.stream(listOf("-s", serial, "shell") + ScrcpyServerCommand.shellArguments(sessionId)).collect() }
            connect(port.toInt()).use { video ->
                closeWhenCancelled(video)
                Socket(InetAddress.getLoopbackAddress(), port.toInt()).use { controlSocket ->
                    closeWhenCancelled(controlSocket)
                    launch { sendControl(controlSocket, control) }
                    sendFrames(video)
                }
            }
            server.cancel()
        } finally {
            withContext(NonCancellable) { removeForward(serial, port) }
        }
    }.conflate().flowOn(Dispatchers.IO)

    /** adb accepts on the forwarded port before the server listens, so only the server's first byte proves a live stream. */
    private suspend fun connect(port: Int): Socket = withTimeoutOrNull(START_TIMEOUT) {
        var socket: Socket? = null
        while (socket == null) {
            socket = tryConnect(port)
            if (socket == null) delay(CONNECT_RETRY_DELAY)
        }
        socket
    } ?: throw AdbException.CommandFailed(listOf("scrcpy"), exitCode = 1, output = "The device did not start streaming its screen")

    private fun tryConnect(port: Int): Socket? = try {
        val socket = Socket(InetAddress.getLoopbackAddress(), port)
        socket.takeIf { it.getInputStream().read() != END_OF_STREAM } ?: null.also { socket.close() }
    } catch (_: IOException) {
        null
    }

    /** A blocked socket read ignores cancellation, closing the socket is what ends it. */
    private fun CoroutineScope.closeWhenCancelled(socket: Socket) {
        launch {
            try {
                awaitCancellation()
            } finally {
                socket.close()
            }
        }
    }

    private suspend fun ProducerScope<MirrorFrameDto>.sendFrames(socket: Socket) {
        val packets = ScrcpyPacketReader(socket.getInputStream())
        H264Decoder().use { decoder ->
            while (true) {
                val accessUnit = try {
                    packets.nextAccessUnit() ?: break
                } catch (error: IOException) {
                    ensureActive()
                    throw error
                }
                decoder.decode(accessUnit).forEach { send(it) }
            }
        }
        channel.close()
    }

    private suspend fun removeForward(serial: String, port: String) {
        val result = client.execute("-s", serial, "forward", "--remove", "tcp:$port")
        if (result.exitCode != 0) logger.warn(TAG, "Could not remove the mirror's forward of port $port: ${result.stderr.ifBlank { result.stdout }.trim()}")
    }

    private suspend fun sendControl(socket: Socket, control: Flow<MirrorControlDto>) {
        val output = socket.getOutputStream()
        control.collect { message ->
            output.write(ScrcpyControlEncoder.encode(message))
            output.flush()
        }
    }

    private fun extractServer(): Path {
        val file = Files.createTempFile("adb-desktop-scrcpy-server", ".jar").also { it.toFile().deleteOnExit() }
        checkNotNull(javaClass.getResourceAsStream(ScrcpyServerCommand.RESOURCE)) { "The scrcpy server is missing from this build" }.use { resource ->
            Files.copy(resource, file, StandardCopyOption.REPLACE_EXISTING)
        }
        return file
    }

    private companion object {
        const val TAG = "Mirror"
        const val END_OF_STREAM = -1
        val START_TIMEOUT = 10.seconds
        val CONNECT_RETRY_DELAY = 100.milliseconds
    }
}
