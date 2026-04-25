package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.DeviceInfoDataSource
import com.tlpcraft.adbdesktop.core.data.utils.safeFlow
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.domain.repository.DeviceInfoRepository
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val DEVICE_INFO_POLLING_INTERVAL_MS = 30_000L

class DeviceInfoRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val dataSource: DeviceInfoDataSource,
) : DeviceInfoRepository {

    override fun observeDeviceInfo(deviceSerial: String): Flow<Outcome<DeviceInfo, DeviceError>> = safeFlow(errorMapper = ::toDeviceError) {
        flow {
            while (true) {
                emit(dataSource.getDeviceInfo(deviceSerial))
                delay(DEVICE_INFO_POLLING_INTERVAL_MS)
            }
        }
    }.flowOn(dispatcherProvider.io)

    private fun toDeviceError(cause: Throwable): DeviceError = DeviceError.CommandFailed(cause)
}
