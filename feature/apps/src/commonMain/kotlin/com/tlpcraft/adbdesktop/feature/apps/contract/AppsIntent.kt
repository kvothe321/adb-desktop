package com.tlpcraft.adbdesktop.feature.apps.contract

import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter

sealed interface AppsIntent {
    data class FilterSelected(val filter: AppFilter) : AppsIntent

    data class QueryChanged(val value: String) : AppsIntent

    data class AppClicked(val packageName: String) : AppsIntent

    data class ActionClicked(val action: AppAction) : AppsIntent

    data object ActionConfirmed : AppsIntent

    data object ActionCancelled : AppsIntent

    data object RetryClicked : AppsIntent

    data object InstallClicked : AppsIntent
}
