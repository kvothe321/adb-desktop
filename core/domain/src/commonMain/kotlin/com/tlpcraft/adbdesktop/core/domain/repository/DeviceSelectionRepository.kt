package com.tlpcraft.adbdesktop.core.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface DeviceSelectionRepository {
    val selectedSerial: StateFlow<String?>

    fun select(serial: String?)
}
