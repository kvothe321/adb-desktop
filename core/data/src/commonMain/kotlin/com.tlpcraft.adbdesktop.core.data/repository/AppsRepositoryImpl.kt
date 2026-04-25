package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.datasource.AppsDataSource
import com.tlpcraft.adbdesktop.core.data.utils.safeCall
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.repository.AppsRepository

class AppsRepositoryImpl(
    private val appsDataSource: AppsDataSource,
) : AppsRepository {

    override suspend fun getApps(deviceSerial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError> =
        safeCall(::toDeviceError) { appsDataSource.getApps(deviceSerial, filter) }

    override suspend fun getAppDetails(deviceSerial: String, packageName: String): Outcome<AppDetails, DeviceError> =
        safeCall(::toDeviceError) { appsDataSource.getAppDetails(deviceSerial, packageName) }

    override suspend fun executeAction(deviceSerial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> =
        safeCall(::toDeviceError) { appsDataSource.executeAction(deviceSerial, packageName, action) }

    private fun toDeviceError(cause: Throwable): DeviceError = DeviceError.CommandFailed(cause)
}
