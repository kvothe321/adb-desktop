package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbFileDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import kotlinx.coroutines.CompletableDeferred

class FakeAdbFileDataSource : AdbFileDataSource {
    var listing: List<RemoteFileDto> = emptyList()
    val remoteSizes = mutableMapOf<String, Long>()
    val localSizes = mutableMapOf<String, Long>()
    val deletedRemote = mutableListOf<String>()
    val deletedLocal = mutableListOf<String>()
    var copyGate = CompletableDeferred(Unit)

    override suspend fun list(serial: String, directory: String): List<RemoteFileDto> = listing

    override suspend fun push(serial: String, localPath: String, remoteDirectory: String) {
        copyGate.await()
    }

    override suspend fun pull(serial: String, remotePath: String, localDirectory: String) {
        copyGate.await()
    }

    override suspend fun delete(serial: String, remotePath: String) {
        deletedRemote += remotePath
    }

    override suspend fun createDirectory(serial: String, remotePath: String) = Unit

    override suspend fun remoteFileSize(serial: String, remotePath: String): Long? = remoteSizes[remotePath]

    override fun localFileSize(path: String): Long? = localSizes[path]

    override fun deleteLocalFile(path: String) {
        deletedLocal += path
    }

    override fun localPath(directory: String, name: String): String = "$directory/$name"
}
