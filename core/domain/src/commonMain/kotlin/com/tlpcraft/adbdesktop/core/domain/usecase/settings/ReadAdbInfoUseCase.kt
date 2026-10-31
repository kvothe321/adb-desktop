package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class ReadAdbInfoUseCase(
    private val repository: AdbSettingsRepository,
) : UseCase<Unit, Outcome<AdbInfo, DeviceError>> {

    override suspend fun invoke(param: Unit): Outcome<AdbInfo, DeviceError> = repository.readAdbInfo()
}
