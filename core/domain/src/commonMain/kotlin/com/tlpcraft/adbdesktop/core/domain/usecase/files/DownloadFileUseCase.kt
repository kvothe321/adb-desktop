package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class DownloadFileUseCase(
    private val repository: FilesRepository,
) : ReactiveUseCase<DownloadFileUseCase.Params, Flow<Outcome<TransferProgress, DeviceError>>> {

    data class Params(val serial: String, val file: RemoteFile, val localDirectory: String)

    override fun invoke(param: Params): Flow<Outcome<TransferProgress, DeviceError>> = repository.download(param.serial, param.file, param.localDirectory)
}
