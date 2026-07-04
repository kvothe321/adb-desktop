package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Online devices first, then by serial, so the list stays stable while states change. */
class ObserveDevicesUseCase(
    private val repository: DeviceRepository,
) : ReactiveUseCase<Unit, Flow<Outcome<List<Device>, DeviceError>>> {

    override fun invoke(param: Unit): Flow<Outcome<List<Device>, DeviceError>> = repository.observeDevices()
        .map { outcome -> outcome.map { devices -> devices.sortedWith(onlineFirstThenSerial) } }

    private companion object {
        val onlineFirstThenSerial = compareByDescending<Device> { it.isOnline }.thenBy { it.serial }
    }
}
