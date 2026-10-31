package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.repository.DeviceSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeDeviceSelectionRepository(initialSerial: String? = null) : DeviceSelectionRepository {
    private val serial = MutableStateFlow(initialSerial)

    override val selectedSerial: StateFlow<String?> = serial

    override fun select(serial: String?) {
        this.serial.value = serial
    }
}
