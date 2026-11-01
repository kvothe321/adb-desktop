package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeFilesRepository(private val listing: Map<String, List<String>> = emptyMap()) : FilesRepository {
    var failure: DeviceError? = null

    /** When set, transfers stay in progress until it completes. */
    var transferGate: CompletableDeferred<Unit>? = null
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

    override fun upload(serial: String, localPath: String, remoteDirectory: String): Flow<Outcome<TransferProgress, DeviceError>> = flow {
        emit(result { uploads += localPath to remoteDirectory }.map { TransferProgress(localPath, transferredBytes = 1, totalBytes = 2) })
        transferGate?.await()
    }

    override fun download(serial: String, file: RemoteFile, localDirectory: String): Flow<Outcome<TransferProgress, DeviceError>> = flow {
        emit(result { downloads += file.path to localDirectory }.map { TransferProgress(file.name, transferredBytes = 1, totalBytes = 2) })
        transferGate?.await()
    }

    override suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError> = result { deleted += remotePath }

    override suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError> = result { createdDirectories += remotePath }

    private fun <T> result(block: () -> T): Outcome<T, DeviceError> = failure?.let { Outcome.Failure(it) } ?: Outcome.Success(block())
}
