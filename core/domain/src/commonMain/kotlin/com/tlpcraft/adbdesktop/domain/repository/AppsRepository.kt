package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo

interface AppsRepository {
    suspend fun getApps(deviceSerial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError>

    suspend fun getAppDetails(deviceSerial: String, packageName: String): Outcome<AppDetails, DeviceError>

    suspend fun executeAction(deviceSerial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError>
}
