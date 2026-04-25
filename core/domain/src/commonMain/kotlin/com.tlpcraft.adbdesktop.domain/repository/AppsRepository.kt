package com.tlpcraft.adbdesktop.domain.repository

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo

interface AppsRepository {
    /**
     * Fetches the list of installed packages on [deviceSerial] that match [filter].
     */
    suspend fun getApps(deviceSerial: String, filter: AppFilter): Outcome<List<AppInfo>, DeviceError>
}
