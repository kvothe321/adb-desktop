package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.FileListParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.shellQuoted

interface AdbFileDataSource {
    suspend fun list(serial: String, directory: String): List<RemoteFileDto>

    suspend fun push(serial: String, localPath: String, remoteDirectory: String)

    suspend fun pull(serial: String, remotePath: String, localDirectory: String)

    suspend fun delete(serial: String, remotePath: String)

    suspend fun createDirectory(serial: String, remotePath: String)
}

class AdbFileDataSourceImpl(private val client: AdbClient) : AdbFileDataSource {

    /** `ls` exits non-zero when only some entries are unreadable, so a listing with rows still counts as success. */
    override suspend fun list(serial: String, directory: String): List<RemoteFileDto> {
        val args = listOf("ls", "-la", "$directory/".replace("//", "/").shellQuoted())
        val result = client.shellResult(serial, args)
        val entries = FileListParser.parse(result.stdout)
        if (result.exitCode != 0 && entries.isEmpty()) {
            throw AdbException.CommandFailed(args, result.exitCode, result.stderr.ifBlank { result.stdout }.trim())
        }
        return entries
    }

    override suspend fun push(serial: String, localPath: String, remoteDirectory: String) {
        client.run(listOf("-s", serial, "push", localPath, "$remoteDirectory/".replace("//", "/")))
    }

    override suspend fun pull(serial: String, remotePath: String, localDirectory: String) {
        client.run(listOf("-s", serial, "pull", remotePath, localDirectory))
    }

    override suspend fun delete(serial: String, remotePath: String) {
        client.shell(serial, listOf("rm", "-rf", remotePath.shellQuoted()))
    }

    override suspend fun createDirectory(serial: String, remotePath: String) {
        client.shell(serial, listOf("mkdir", "-p", remotePath.shellQuoted()))
    }
}
