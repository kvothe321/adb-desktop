package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.CpuDataSource
import com.tlpcraft.adbdesktop.core.data.utils.safeFlow
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.domain.repository.CpuRepository
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val CPU_POLLING_INTERVAL_MS = 2_000L

class CpuRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val cpuDataSource: CpuDataSource,
) : CpuRepository {

    override fun observeCpuInfo(deviceSerial: String): Flow<Outcome<CpuInfo, DeviceError>> = safeFlow(errorMapper = ::toDeviceError) {
        flow {
            while (true) {
                emit(cpuDataSource.getCpuStats(deviceSerial))
                delay(CPU_POLLING_INTERVAL_MS)
            }
        }
    }.flowOn(dispatcherProvider.io)

    private fun toDeviceError(cause: Throwable): DeviceError = DeviceError.CommandFailed(cause)
}
