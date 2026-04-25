package com.tlpcraft.adbdesktop.presentation

import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo

sealed interface AppsUiState {

    /** ADB is being queried for connected devices. */
    data object Loading : AppsUiState

    /** No device is currently in `device` (online) state. */
    data object NoDevice : AppsUiState

    /** A device is connected and packages are being fetched. */
    data class FetchingApps(
        val deviceSerial: String,
        val activeFilter: AppFilter,
    ) : AppsUiState

    /** Package list loaded successfully. */
    data class Content(
        val deviceSerial: String,
        val activeFilter: AppFilter,
        val apps: List<AppInfo>,
        val searchQuery: String,
    ) : AppsUiState {
        /** Apps filtered by the current [searchQuery], case-insensitive. */
        val filteredApps: List<AppInfo>
            get() = if (searchQuery.isBlank()) {
                apps
            } else {
                apps.filter { it.packageName.contains(searchQuery, ignoreCase = true) }
            }
    }

    data class Error(
        val deviceSerial: String,
        val activeFilter: AppFilter,
        val message: String,
    ) : AppsUiState
}
