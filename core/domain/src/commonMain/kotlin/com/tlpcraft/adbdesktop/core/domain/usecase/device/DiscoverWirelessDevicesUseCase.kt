package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class DiscoverWirelessDevicesUseCase(
    private val repository: DeviceRepository,
) : UseCase<Unit, Outcome<List<WirelessService>, DeviceError>> {

    override suspend fun invoke(param: Unit): Outcome<List<WirelessService>, DeviceError> = repository.discoverWirelessServices()
}
