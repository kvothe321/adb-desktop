package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.CpuInfo

interface CpuDataSource {
    /**
     * Collects a [CpuInfo] snapshot for the given [deviceSerial].
     * Implementations must perform all blocking I/O on an appropriate dispatcher.
     */
    suspend fun getCpuStats(deviceSerial: String): CpuInfo
}
