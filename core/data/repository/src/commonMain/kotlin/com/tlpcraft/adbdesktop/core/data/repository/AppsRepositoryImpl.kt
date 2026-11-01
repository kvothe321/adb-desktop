package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDto
import com.tlpcraft.adbdesktop.core.data.repository.util.Invalidations
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow

class AppsRepositoryImpl(
    private val packageDataSource: AdbPackageDataSource,
    private val pollInterval: Duration = 5.seconds,
) : AppsRepository {
    private val invalidations = Invalidations()

    override fun observeApps(serial: String, filter: AppFilter): Flow<Outcome<List<AppInfo>, DeviceError>> = invalidations.observe(pollInterval) {
        safeCall(Throwable::toDeviceError) { packageDataSource.listPackages(serial, filter.toDto()).map(::AppInfo) }
    }

    override fun observeAppDetails(serial: String, packageName: String): Flow<Outcome<AppDetails, DeviceError>> = invalidations.observe(pollInterval) {
        safeCall(Throwable::toDeviceError) { packageDataSource.readPackageDetails(serial, packageName).toDomain() }
    }

    override suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.runAction(serial, packageName, action.toDto()) }.also { invalidations.invalidate() }

    override suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.install(serial, apkPaths) }.also { invalidations.invalidate() }
}
