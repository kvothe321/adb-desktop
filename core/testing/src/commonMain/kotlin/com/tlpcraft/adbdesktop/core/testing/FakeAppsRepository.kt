package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository

class FakeAppsRepository : AppsRepository {
    var appsResult: Outcome<List<AppInfo>, DeviceError> = Outcome.Success(emptyList())
    var detailsResult: Outcome<AppDetails, DeviceError>? = null
    var actionResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)

    val requestedFilters = mutableListOf<AppFilter>()
    val executedActions = mutableListOf<Pair<String, AppAction>>()
    val installedApks = mutableListOf<List<String>>()
    var installResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)

    override suspend fun getApps(serial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError> {
        requestedFilters += filter
        return appsResult
    }

    override suspend fun getAppDetails(serial: String, packageName: String): Outcome<AppDetails, DeviceError> =
        detailsResult ?: Outcome.Success(emptyDetails(packageName))

    override suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> {
        executedActions += packageName to action
        return actionResult
    }

    override suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError> {
        installedApks += apkPaths
        return installResult
    }

    private fun emptyDetails(packageName: String) = AppDetails(
        packageName = packageName,
        versionName = null,
        versionCode = null,
        targetSdk = null,
        minSdk = null,
        firstInstallTime = null,
        lastUpdateTime = null,
        codeSizeBytes = null,
        dataSizeBytes = null,
        cacheSizeBytes = null,
    )
}
