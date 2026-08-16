package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import kotlinx.coroutines.flow.Flow

interface FilesRepository {
    suspend fun list(serial: String, directory: String): Outcome<List<RemoteFile>, DeviceError>

    /**
     * Copies one file or folder to the device, emitting progress until it is done. A failure ends the flow.
     * Cancelling collection stops the transfer and removes a partly copied file.
     */
    fun upload(serial: String, localPath: String, remoteDirectory: String): Flow<Outcome<TransferProgress, DeviceError>>

    /** Counterpart of [upload]. [file] decides whether a total size is known. */
    fun download(serial: String, file: RemoteFile, localDirectory: String): Flow<Outcome<TransferProgress, DeviceError>>

    suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError>

    suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError>
}
