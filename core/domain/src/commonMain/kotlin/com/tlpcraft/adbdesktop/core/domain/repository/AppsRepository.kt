package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo

interface AppsRepository {
    suspend fun getApps(serial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError>

    suspend fun getAppDetails(serial: String, packageName: String): Outcome<AppDetails, DeviceError>

    suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError>

    /** Several paths are the splits of one app. */
    suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError>
}
