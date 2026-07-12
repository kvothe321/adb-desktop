package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.shellQuoted

interface AdbScreenDataSource {
    /** The current screen as PNG bytes. */
    suspend fun captureScreenshot(serial: String): ByteArray

    /** Records into [remotePath] on the device and returns when recording ends. */
    suspend fun recordScreen(serial: String, remotePath: String)

    suspend fun stopRecording(serial: String)

    suspend fun moveToHost(serial: String, remotePath: String, localPath: String)
}

class AdbScreenDataSourceImpl(private val client: AdbClient) : AdbScreenDataSource {

    override suspend fun captureScreenshot(serial: String): ByteArray = client.binary("-s", serial, "exec-out", "screencap", "-p")

    override suspend fun recordScreen(serial: String, remotePath: String) {
        client.shell(serial, listOf("screenrecord", remotePath.shellQuoted()))
    }

    /** screenrecord only finalizes the mp4 on SIGINT; killing it leaves an unplayable file. */
    override suspend fun stopRecording(serial: String) {
        client.shell(serial, "pkill", "-INT", "screenrecord")
    }

    override suspend fun moveToHost(serial: String, remotePath: String, localPath: String) {
        client.run(listOf("-s", serial, "pull", remotePath, localPath))
        client.shell(serial, listOf("rm", "-f", remotePath.shellQuoted()))
    }
}
