package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.utils.safeCall
import com.tlpcraft.adbdesktop.core.data.utils.safeFlow
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.repository.AdbDeviceRepository
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val POLLING_INTERVAL_MS = 3_000L

class AdbDeviceRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val adbDeviceDataSource: AdbDeviceDataSource
) : AdbDeviceRepository {

    override fun observeDevices(): Flow<Outcome<List<AdbDevice>, DeviceError>> = safeFlow(
        errorMapper = ::toDeviceError
    ) {
        flow {
            while (true) {
                emit(adbDeviceDataSource.getDevices())
                delay(POLLING_INTERVAL_MS)
            }
        }
    }.flowOn(dispatcherProvider.io)

    override suspend fun getDevices(): Outcome<List<AdbDevice>, DeviceError> = safeCall(
        errorMapper = ::toDeviceError
    ) {
        adbDeviceDataSource.getDevices()
    }

    private fun toDeviceError(cause: Throwable): DeviceError = DeviceError.CommandFailed(cause)
}
