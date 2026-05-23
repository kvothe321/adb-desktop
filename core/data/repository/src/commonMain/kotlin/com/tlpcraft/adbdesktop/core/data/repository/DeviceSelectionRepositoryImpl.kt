package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.domain.repository.DeviceSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Kept in memory on purpose: a selection only makes sense for the current session. */
class DeviceSelectionRepositoryImpl : DeviceSelectionRepository {
    private val serial = MutableStateFlow<String?>(null)

    override val selectedSerial: StateFlow<String?> = serial.asStateFlow()

    override fun select(serial: String?) {
        this.serial.value = serial
    }
}
