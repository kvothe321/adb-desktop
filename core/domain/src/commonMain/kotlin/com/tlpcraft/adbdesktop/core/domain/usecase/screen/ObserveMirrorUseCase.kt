package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveMirrorUseCase(
    private val repository: ScreenRepository,
) : ReactiveUseCase<String, Flow<Outcome<MirrorFrame, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<MirrorFrame, DeviceError>> = repository.observeMirror(param)
}
