package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.getOrNull
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * The device the user works with, or `null`. A selected device that goes offline or disappears
 * resolves to `null` and comes back on its own when it reconnects.
 */
class ObserveSelectedDeviceUseCase(
    private val observeDevices: ObserveDevicesUseCase,
    private val selectionRepository: DeviceSelectionRepository,
) : ReactiveUseCase<Unit, Flow<Device?>> {

    override fun invoke(param: Unit): Flow<Device?> = combine(
        observeDevices(Unit),
        selectionRepository.selectedSerial,
    ) { devicesOutcome, selectedSerial ->
        devicesOutcome.getOrNull()?.firstOrNull { it.serial == selectedSerial && it.isOnline }
    }.distinctUntilChanged()
}
