package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class SetThemeModeUseCase(
    private val repository: AppearanceRepository,
) : UseCase<ThemeMode, Unit> {

    override suspend fun invoke(param: ThemeMode) = repository.setThemeMode(param)
}
