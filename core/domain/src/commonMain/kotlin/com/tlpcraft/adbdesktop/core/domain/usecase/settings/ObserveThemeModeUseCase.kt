package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.repository.AppearanceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val repository: AppearanceRepository,
) : ReactiveUseCase<Unit, Flow<ThemeMode>> {

    override fun invoke(param: Unit): Flow<ThemeMode> = repository.observeThemeMode()
}
