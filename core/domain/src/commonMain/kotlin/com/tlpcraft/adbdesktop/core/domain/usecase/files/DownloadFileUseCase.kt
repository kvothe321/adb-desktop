package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class DownloadFileUseCase(
    private val repository: FilesRepository,
) : UseCase<DownloadFileUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val remotePath: String, val localDirectory: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.download(param.serial, param.remotePath, param.localDirectory)
}
