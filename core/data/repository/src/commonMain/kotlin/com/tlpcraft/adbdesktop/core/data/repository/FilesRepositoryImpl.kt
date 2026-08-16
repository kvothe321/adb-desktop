package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbFileDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilesRepositoryImpl(
    private val fileDataSource: AdbFileDataSource,
    private val progressInterval: Duration = 200.milliseconds,
) : FilesRepository {

    override suspend fun list(serial: String, directory: String): Outcome<List<RemoteFile>, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.list(serial, directory).map { it.toDomain(directory) } }

    override fun upload(serial: String, localPath: String, remoteDirectory: String): Flow<Outcome<TransferProgress, DeviceError>> {
        val name = localPath.substringAfterLast('/').substringAfterLast('\\')
        val remotePath = RemotePath.child(remoteDirectory, name)
        return transfer(
            name = name,
            totalBytes = fileDataSource.localFileSize(localPath),
            readTransferred = { fileDataSource.remoteFileSize(serial, remotePath) },
            removePartialFile = { fileDataSource.delete(serial, remotePath) },
            copy = { fileDataSource.push(serial, localPath, remoteDirectory) },
        )
    }

    override fun download(serial: String, file: RemoteFile, localDirectory: String): Flow<Outcome<TransferProgress, DeviceError>> {
        val localPath = fileDataSource.localPath(localDirectory, file.name)
        return transfer(
            name = file.name,
            totalBytes = file.sizeBytes.takeIf { file.type == RemoteFileType.FILE },
            readTransferred = { fileDataSource.localFileSize(localPath) },
            removePartialFile = { fileDataSource.deleteLocalFile(localPath) },
            copy = { fileDataSource.pull(serial, file.path, localDirectory) },
        )
    }

    override suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.delete(serial, remotePath) }

    override suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.createDirectory(serial, remotePath) }

    /**
     * adb prints no progress when it is not attached to a terminal, so progress comes from watching the target grow.
     * Without a known total the transfer is a folder, which is never removed: it may be an existing one merged into.
     */
    private fun transfer(
        name: String,
        totalBytes: Long?,
        readTransferred: suspend () -> Long?,
        removePartialFile: suspend () -> Unit,
        copy: suspend () -> Unit,
    ): Flow<Outcome<TransferProgress, DeviceError>> = channelFlow {
        val reporter = launch {
            while (true) {
                send(TransferProgress(name, if (totalBytes != null) readTransferred() ?: 0 else 0, totalBytes))
                delay(progressInterval)
            }
        }
        try {
            copy()
        } catch (cancelled: CancellationException) {
            if (totalBytes != null) withContext(NonCancellable) { removePartialFile() }
            throw cancelled
        }
        reporter.cancel()
        send(TransferProgress(name, totalBytes ?: 0, totalBytes))
    }.asOutcomeFlow(Throwable::toDeviceError)
}
