package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo

interface AppsDataSource {
    /**
     * Runs `pm list packages [flag]` on [deviceSerial] and returns the parsed list of packages.
     * All blocking I/O must be performed on an appropriate dispatcher by the implementation.
     */
    suspend fun getApps(deviceSerial: String, filter: AppFilter): List<AppInfo>
}
