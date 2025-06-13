package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.repository.AdbDeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val POLLING_INTERVAL_MS = 3_000L

class AdbDeviceRepositoryImpl(
    private val adbDeviceDataSource: AdbDeviceDataSource
) : AdbDeviceRepository {

    override fun observeDevices(): Flow<List<AdbDevice>> = flow {
        while (true) {
            val devices = adbDeviceDataSource.getDevices()
            emit(devices)
            delay(POLLING_INTERVAL_MS)
        }
    }

    override suspend fun getDevices(): Result<List<AdbDevice>> = runCatching {
        adbDeviceDataSource.getDevices()
    }
}
