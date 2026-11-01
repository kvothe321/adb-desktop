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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class FakeFilesRepository(listing: Map<String, List<String>> = emptyMap()) : FilesRepository {
    val listing = MutableStateFlow(listing)
    private val failures = MutableStateFlow<DeviceError?>(null)
    var failure: DeviceError?
        get() = failures.value
        set(value) {
            failures.value = value
        }

    /** When set, transfers stay in progress until it completes. */
    var transferGate: CompletableDeferred<Unit>? = null
    val uploads = mutableListOf<Pair<String, String>>()
    val downloads = mutableListOf<Pair<String, String>>()
    val deleted = mutableListOf<String>()
    val createdDirectories = mutableListOf<String>()

    override fun observeFiles(serial: String, directory: String): Flow<Outcome<List<RemoteFile>, DeviceError>> = combine(listing, failures) { entries, _ ->
        result { entries[directory].orEmpty().map { name -> remoteFile(directory, name) } }
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

    private fun remoteFile(directory: String, name: String): RemoteFile {
        val isDirectory = name.endsWith("/")
        val cleanName = name.trimEnd('/')
        return RemoteFile(
            name = cleanName,
            path = RemotePath.child(directory, cleanName),
            type = if (isDirectory) RemoteFileType.DIRECTORY else RemoteFileType.FILE,
            sizeBytes = 1,
            modified = "2026-10-31 23:05",
            permissions = if (isDirectory) "drwxrwx---" else "-rw-rw----",
        )
    }

    private fun <T> result(block: () -> T): Outcome<T, DeviceError> = failure?.let { Outcome.Failure(it) } ?: Outcome.Success(block())
}
