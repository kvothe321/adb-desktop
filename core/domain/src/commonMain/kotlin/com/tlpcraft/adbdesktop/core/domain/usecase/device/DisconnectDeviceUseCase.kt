package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class DisconnectDeviceUseCase(
    private val repository: DeviceRepository,
) : UseCase<String, Outcome<Unit, DeviceError>> {

    override suspend fun invoke(param: String): Outcome<Unit, DeviceError> = repository.disconnect(param)
}
