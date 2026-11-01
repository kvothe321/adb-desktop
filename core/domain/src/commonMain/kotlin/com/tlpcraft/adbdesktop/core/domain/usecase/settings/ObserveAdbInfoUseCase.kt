package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAdbInfoUseCase(
    private val repository: AdbSettingsRepository,
) : ReactiveUseCase<Unit, Flow<Outcome<AdbInfo, DeviceError>>> {

    override fun invoke(param: Unit): Flow<Outcome<AdbInfo, DeviceError>> = repository.observeAdbInfo()
}
