package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDto
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository

class AppsRepositoryImpl(
    private val packageDataSource: AdbPackageDataSource,
) : AppsRepository {

    override suspend fun getApps(serial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.listPackages(serial, filter.toDto()).map(::AppInfo) }

    override suspend fun getAppDetails(serial: String, packageName: String): Outcome<AppDetails, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.readPackageDetails(serial, packageName).toDomain() }

    override suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.runAction(serial, packageName, action.toDto()) }
}
