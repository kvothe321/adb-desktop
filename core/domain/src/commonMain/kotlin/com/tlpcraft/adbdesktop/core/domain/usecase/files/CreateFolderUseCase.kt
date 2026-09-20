package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Creates [Params.name] inside [Params.directory]. Names cannot contain a path separator. */
class CreateFolderUseCase(
    private val repository: FilesRepository,
) : UseCase<CreateFolderUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val directory: String, val name: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> {
        val name = param.name.trim()
        if (name.isEmpty() || name.contains('/')) return Outcome.Failure(DeviceError.InvalidFileName(param.name))
        return repository.createDirectory(param.serial, RemotePath.child(param.directory, name))
    }
}
