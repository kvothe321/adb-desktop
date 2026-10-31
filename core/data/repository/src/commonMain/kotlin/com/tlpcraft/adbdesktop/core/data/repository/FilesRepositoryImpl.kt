package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbFileDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository

class FilesRepositoryImpl(private val fileDataSource: AdbFileDataSource) : FilesRepository {

    override suspend fun list(serial: String, directory: String): Outcome<List<RemoteFile>, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.list(serial, directory).map { it.toDomain(directory) } }

    override suspend fun upload(serial: String, localPaths: List<String>, remoteDirectory: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { localPaths.forEach { fileDataSource.push(serial, it, remoteDirectory) } }

    override suspend fun download(serial: String, remotePath: String, localDirectory: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.pull(serial, remotePath, localDirectory) }

    override suspend fun delete(serial: String, remotePath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.delete(serial, remotePath) }

    override suspend fun createDirectory(serial: String, remotePath: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { fileDataSource.createDirectory(serial, remotePath) }
}
