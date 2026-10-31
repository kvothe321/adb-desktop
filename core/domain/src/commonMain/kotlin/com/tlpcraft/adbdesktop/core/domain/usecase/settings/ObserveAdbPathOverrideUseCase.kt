package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAdbPathOverrideUseCase(
    private val repository: AdbSettingsRepository,
) : ReactiveUseCase<Unit, Flow<String?>> {

    override fun invoke(param: Unit): Flow<String?> = repository.observeAdbPathOverride()
}
