package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.domain.repository.DeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Selects the device with the given serial, or clears the selection with `null`. */
class SelectDeviceUseCase(
    private val selectionRepository: DeviceSelectionRepository,
) : UseCase<String?, Unit> {

    override suspend fun invoke(param: String?) = selectionRepository.select(param)
}
