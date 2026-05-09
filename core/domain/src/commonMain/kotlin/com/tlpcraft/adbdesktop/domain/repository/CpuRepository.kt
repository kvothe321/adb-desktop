package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import kotlinx.coroutines.flow.Flow

interface CpuRepository {
    /**
     * Returns a continuous stream of [CpuInfo] snapshots for the given [deviceSerial].
     * Each emission is a fresh poll of the device's `/proc/stat`, `nproc`, and
     * `cpufreq` nodes via ADB shell.
     */
    fun observeCpuInfo(deviceSerial: String): Flow<Outcome<CpuInfo, DeviceError>>
}
