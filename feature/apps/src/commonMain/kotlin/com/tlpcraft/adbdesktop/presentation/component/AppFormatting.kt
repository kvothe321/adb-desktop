package com.tlpcraft.adbdesktop.presentation.component

import com.tlpcraft.adbdesktop.domain.model.AppDetails

internal fun AppDetails.displayName(): String = packageName.substringAfterLast('.').replaceFirstChar { it.uppercaseChar() }

internal fun Long.toHumanReadable(): String = when {
    this < 1_024L -> "$this B"
    this < 1_024L * 1_024 -> "${this / 1_024} KB"
    this < 1_024L * 1_024 * 1_024 -> "${this / (1_024L * 1_024)} MB"
    else -> "${this / (1_024L * 1_024 * 1_024)} GB"
}
