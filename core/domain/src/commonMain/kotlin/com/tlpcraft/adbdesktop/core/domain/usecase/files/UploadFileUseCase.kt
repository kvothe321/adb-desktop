package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class UploadFileUseCase(
    private val repository: FilesRepository,
) : ReactiveUseCase<UploadFileUseCase.Params, Flow<Outcome<TransferProgress, DeviceError>>> {

    data class Params(val serial: String, val localPath: String, val remoteDirectory: String)

    override fun invoke(param: Params): Flow<Outcome<TransferProgress, DeviceError>> = repository.upload(param.serial, param.localPath, param.remoteDirectory)
}
