package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.TerminalSize
import kotlinx.coroutines.flow.Flow

interface ShellRepository {
    /**
     * Keeps one terminal open while collected. [input] is sent as typed, and the flow emits raw terminal output.
     * The first of [sizes] sets the size of the terminal, later ones resize it.
     */
    fun runSession(serial: String, input: Flow<String>, sizes: Flow<TerminalSize>): Flow<Outcome<String, DeviceError>>
}
