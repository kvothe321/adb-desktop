package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class FakeAppsRepository : AppsRepository {
    val apps = MutableStateFlow<Outcome<List<AppInfo>, DeviceError>>(Outcome.Success(emptyList()))
    val details = MutableStateFlow<Outcome<AppDetails, DeviceError>?>(null)
    var actionResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)

    val requestedFilters = mutableListOf<AppFilter>()
    val executedActions = mutableListOf<Pair<String, AppAction>>()
    val installedApks = mutableListOf<List<String>>()
    var installResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)
    val appearances = MutableStateFlow<Map<String, AppAppearance>>(emptyMap())
    val appearanceRequests = mutableListOf<List<String>>()

    override fun observeApps(serial: String, filter: AppFilter): Flow<Outcome<List<AppInfo>, DeviceError>> = apps.onStart { requestedFilters += filter }

    override fun observeAppDetails(serial: String, packageName: String): Flow<Outcome<AppDetails, DeviceError>> =
        details.map { it ?: Outcome.Success(emptyDetails(packageName)) }

    override suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> {
        executedActions += packageName to action
        return actionResult
    }

    override fun observeAppAppearances(serial: String, packageNames: List<String>): Flow<Map<String, AppAppearance>> = appearances.onStart {
        appearanceRequests +=
            packageNames
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
