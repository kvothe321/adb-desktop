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
 *
 * When the selection resolves to nothing and exactly one device is online, that device becomes the
 * selection. It is stored rather than derived so it survives a second device connecting.
 */
class ObserveSelectedDeviceUseCase(
    private val observeDevices: ObserveDevicesUseCase,
    private val selectionRepository: DeviceSelectionRepository,
) : ReactiveUseCase<Unit, Flow<Device?>> {

    override fun invoke(param: Unit): Flow<Device?> = combine(
        observeDevices(Unit),
        selectionRepository.selectedSerial,
    ) { devicesOutcome, selectedSerial ->
        val onlineDevices = devicesOutcome.getOrNull().orEmpty().filter { it.isOnline }
        onlineDevices.firstOrNull { it.serial == selectedSerial } ?: onlineDevices.singleOrNull()?.also { selectionRepository.select(it.serial) }
    }.distinctUntilChanged()
}
