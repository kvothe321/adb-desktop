package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class SetMirrorPinnedUseCase(
    private val repository: AppearanceRepository,
) : UseCase<Boolean, Unit> {

    override suspend fun invoke(param: Boolean) = repository.setMirrorPinned(param)
}
