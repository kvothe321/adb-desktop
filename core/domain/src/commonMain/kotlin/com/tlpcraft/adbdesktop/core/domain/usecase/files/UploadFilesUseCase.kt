package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class UploadFilesUseCase(
    private val repository: FilesRepository,
) : UseCase<UploadFilesUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val localPaths: List<String>, val remoteDirectory: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.upload(param.serial, param.localPaths, param.remoteDirectory)
}
