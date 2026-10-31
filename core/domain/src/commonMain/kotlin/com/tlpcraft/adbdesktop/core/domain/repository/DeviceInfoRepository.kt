package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

interface DeviceInfoRepository {
    fun observeDeviceInfo(serial: String): Flow<Outcome<DeviceInfo, DeviceError>>
}
