package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    /** Emits the installed apps on every change, whatever caused it. */
    fun observeApps(serial: String, filter: AppFilter): Flow<Outcome<List<AppInfo>, DeviceError>>

    /** Emits the details of one app on every change. */
    fun observeAppDetails(serial: String, packageName: String): Flow<Outcome<AppDetails, DeviceError>>

    suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError>

    /**
     * Emits the names and launcher icons of [packageNames] found so far, growing as more are read. Apps with neither are
     * left out, and so is everything when they cannot be read: they only decorate.
     */
    fun observeAppAppearances(serial: String, packageNames: List<String>): Flow<Map<String, AppAppearance>>

    /** Several paths are the splits of one app. */
    suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError>
}
