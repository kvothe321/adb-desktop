package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.repository.CpuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeCpuRepository(private val cpuBySerial: Map<String, CpuInfo> = emptyMap()) : CpuRepository {
    override fun observeCpuInfo(serial: String): Flow<Outcome<CpuInfo, DeviceError>> = cpuBySerial[serial]?.let { flowOf(Outcome.Success(it)) } ?: emptyFlow()
}
