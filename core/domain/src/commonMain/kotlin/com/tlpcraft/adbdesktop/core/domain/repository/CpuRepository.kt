package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import kotlinx.coroutines.flow.Flow

interface CpuRepository {
    fun observeCpuInfo(serial: String): Flow<Outcome<CpuInfo, DeviceError>>
}
