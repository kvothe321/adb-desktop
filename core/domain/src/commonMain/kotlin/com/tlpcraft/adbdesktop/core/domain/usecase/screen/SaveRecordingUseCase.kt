package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class SaveRecordingUseCase(
    private val repository: ScreenRepository,
) : UseCase<SaveRecordingUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val localPath: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.saveRecording(param.serial, param.localPath)
}
