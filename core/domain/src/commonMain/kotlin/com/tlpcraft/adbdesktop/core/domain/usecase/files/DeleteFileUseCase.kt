package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class DeleteFileUseCase(
    private val repository: FilesRepository,
) : UseCase<DeleteFileUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val remotePath: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.delete(param.serial, param.remotePath)
}
