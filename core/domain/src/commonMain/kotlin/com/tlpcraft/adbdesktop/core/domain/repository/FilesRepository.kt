package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile

interface FilesRepository {
    suspend fun list(serial: String, directory: String): Outcome<List<RemoteFile>, DeviceError>

    suspend fun upload(serial: String, localPaths: List<String>, remoteDirectory: String): Outcome<Unit, DeviceError>

    suspend fun download(serial: String, remotePath: String, localDirectory: String): Outcome<Unit, DeviceError>

    suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError>

    suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError>
}
