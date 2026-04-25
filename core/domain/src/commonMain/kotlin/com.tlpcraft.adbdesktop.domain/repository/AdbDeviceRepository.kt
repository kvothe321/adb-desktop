package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import kotlinx.coroutines.flow.Flow

interface AdbDeviceRepository {
    fun observeDevices(): Flow<List<AdbDevice>>

    suspend fun getDevices(): Result<List<AdbDevice>>
}
