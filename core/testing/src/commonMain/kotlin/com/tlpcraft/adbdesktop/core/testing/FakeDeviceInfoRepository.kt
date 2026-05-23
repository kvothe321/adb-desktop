package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeDeviceInfoRepository(private val infoBySerial: Map<String, DeviceInfo> = emptyMap()) : DeviceInfoRepository {
    override fun observeDeviceInfo(serial: String): Flow<Outcome<DeviceInfo, DeviceError>> =
        infoBySerial[serial]?.let { flowOf(Outcome.Success(it)) } ?: emptyFlow()
}
