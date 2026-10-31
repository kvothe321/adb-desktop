package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo

interface AppsDataSource {
    suspend fun getApps(deviceSerial: String, filter: AppFilter): List<AppInfo>

    suspend fun getAppDetails(deviceSerial: String, packageName: String): AppDetails

    suspend fun executeAction(deviceSerial: String, packageName: String, action: AppAction)
}
