package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import kotlinx.coroutines.flow.Flow

interface AdbDeviceRepository {
    fun observeDevices(): Flow<Outcome<List<AdbDevice>, DeviceError>>

    suspend fun getDevices(): Outcome<List<AdbDevice>, DeviceError>
}
