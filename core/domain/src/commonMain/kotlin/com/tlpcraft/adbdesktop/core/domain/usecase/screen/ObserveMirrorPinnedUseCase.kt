package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveMirrorPinnedUseCase(
    private val repository: AppearanceRepository,
) : ReactiveUseCase<Unit, Flow<Boolean>> {

    override fun invoke(param: Unit): Flow<Boolean> = repository.observeMirrorPinned()
}
