package com.tlpcraft.adbdesktop.feature.apps.component

import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.feature.apps.resources.Res
import com.tlpcraft.adbdesktop.feature.apps.resources.app_action_clear_cache
import com.tlpcraft.adbdesktop.feature.apps.resources.app_action_clear_data
import com.tlpcraft.adbdesktop.feature.apps.resources.app_action_force_stop
import com.tlpcraft.adbdesktop.feature.apps.resources.app_action_open
import com.tlpcraft.adbdesktop.feature.apps.resources.app_action_uninstall
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_filter_all
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_filter_disabled
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_filter_system
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_filter_user
import org.jetbrains.compose.resources.StringResource

internal val AppFilter.label: StringResource
    get() = when (this) {
        AppFilter.ALL -> Res.string.apps_filter_all
        AppFilter.USER -> Res.string.apps_filter_user
        AppFilter.SYSTEM -> Res.string.apps_filter_system
        AppFilter.DISABLED -> Res.string.apps_filter_disabled
    }

internal val AppAction.label: StringResource
    get() = when (this) {
        AppAction.OPEN -> Res.string.app_action_open
        AppAction.FORCE_STOP -> Res.string.app_action_force_stop
        AppAction.CLEAR_CACHE -> Res.string.app_action_clear_cache
        AppAction.CLEAR_DATA -> Res.string.app_action_clear_data
        AppAction.UNINSTALL -> Res.string.app_action_uninstall
    }

private const val BYTES_PER_UNIT = 1_024.0
private const val ONE_DECIMAL = 10
private val SIZE_UNITS = listOf("B", "KB", "MB", "GB")

internal fun Long.toReadableSize(): String {
    var value = toDouble()
    var unitIndex = 0
    while (value >= BYTES_PER_UNIT && unitIndex < SIZE_UNITS.lastIndex) {
        value /= BYTES_PER_UNIT
        unitIndex++
    }
    val rounded = (value * ONE_DECIMAL).toLong().toDouble() / ONE_DECIMAL
    return "$rounded ${SIZE_UNITS[unitIndex]}"
}

/** Last package segment, capitalized: `com.example.notes` becomes `Notes`. */
internal fun String.packageDisplayName(): String = substringAfterLast('.').replaceFirstChar(Char::uppercaseChar)

/** The app's own name when it has one, otherwise a name made from the package. */
internal fun String.appTitle(appearance: AppAppearance?): String = appearance?.label ?: packageDisplayName()
