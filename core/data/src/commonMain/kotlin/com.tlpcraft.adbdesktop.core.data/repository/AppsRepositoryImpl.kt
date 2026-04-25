package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.AppsDataSource
import com.tlpcraft.adbdesktop.core.data.utils.safeCall
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.repository.AppsRepository

class AppsRepositoryImpl(
    private val appsDataSource: AppsDataSource,
) : AppsRepository {

    override suspend fun getApps(deviceSerial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError> = safeCall(::toDeviceError) {
        appsDataSource.getApps(deviceSerial, filter)
    }

    private fun toDeviceError(cause: Throwable): DeviceError = DeviceError.CommandFailed(cause)
}
