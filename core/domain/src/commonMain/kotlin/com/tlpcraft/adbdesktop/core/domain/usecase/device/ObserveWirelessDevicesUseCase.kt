package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveWirelessDevicesUseCase(
    private val repository: DeviceRepository,
) : ReactiveUseCase<Unit, Flow<Outcome<List<WirelessService>, DeviceError>>> {

    override fun invoke(param: Unit): Flow<Outcome<List<WirelessService>, DeviceError>> = repository.observeWirelessServices()
}
