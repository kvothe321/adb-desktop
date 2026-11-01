package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveMirrorUseCase(
    private val repository: ScreenRepository,
) : ReactiveUseCase<ObserveMirrorUseCase.Params, Flow<Outcome<MirrorFrame, DeviceError>>> {

    data class Params(val serial: String, val input: Flow<MirrorInput>)

    override fun invoke(param: Params): Flow<Outcome<MirrorFrame, DeviceError>> = repository.observeMirror(param.serial, param.input)
}
