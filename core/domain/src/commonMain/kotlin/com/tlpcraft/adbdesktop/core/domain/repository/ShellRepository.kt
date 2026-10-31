package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import kotlinx.coroutines.flow.Flow

interface ShellRepository {
    /** Keeps one shell open while collected. Each [commands] value runs in it; the flow emits output as it arrives. */
    fun runSession(serial: String, commands: Flow<String>): Flow<Outcome<String, DeviceError>>
}
