package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository

class FakeFilesRepository(private val listing: Map<String, List<String>> = emptyMap()) : FilesRepository {
    var failure: DeviceError? = null
    val uploads = mutableListOf<Pair<String, String>>()
    val downloads = mutableListOf<Pair<String, String>>()
    val deleted = mutableListOf<String>()
    val createdDirectories = mutableListOf<String>()

    override suspend fun list(serial: String, directory: String): Outcome<List<RemoteFile>, DeviceError> = result {
        listing[directory].orEmpty().map { name ->
            val isDirectory = name.endsWith("/")
            val cleanName = name.trimEnd('/')
            RemoteFile(
                name = cleanName,
                path = RemotePath.child(directory, cleanName),
                type = if (isDirectory) RemoteFileType.DIRECTORY else RemoteFileType.FILE,
                sizeBytes = 1,
                modified = "2026-10-31 23:05",
                permissions = if (isDirectory) "drwxrwx---" else "-rw-rw----",
            )
        }
    }

    override suspend fun upload(serial: String, localPaths: List<String>, remoteDirectory: String): Outcome<Unit, DeviceError> =
        result { localPaths.forEach { uploads += it to remoteDirectory } }

    override suspend fun download(serial: String, remotePath: String, localDirectory: String): Outcome<Unit, DeviceError> =
        result { downloads += remotePath to localDirectory }

    override suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError> = result { deleted += remotePath }

    override suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError> = result { createdDirectories += remotePath }

    private fun <T> result(block: () -> T): Outcome<T, DeviceError> = failure?.let { Outcome.Failure(it) } ?: Outcome.Success(block())
}
