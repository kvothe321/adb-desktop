package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageDetailsDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter

internal fun AppFilter.toDto(): PackageFilterDto = when (this) {
    AppFilter.ALL -> PackageFilterDto.ALL
    AppFilter.USER -> PackageFilterDto.THIRD_PARTY
    AppFilter.SYSTEM -> PackageFilterDto.SYSTEM
    AppFilter.DISABLED -> PackageFilterDto.DISABLED
}

internal fun AppAction.toDto(): PackageActionDto = when (this) {
    AppAction.OPEN -> PackageActionDto.LAUNCH
    AppAction.FORCE_STOP -> PackageActionDto.FORCE_STOP
    AppAction.CLEAR_CACHE -> PackageActionDto.CLEAR_CACHE
    AppAction.CLEAR_DATA -> PackageActionDto.CLEAR_DATA
    AppAction.UNINSTALL -> PackageActionDto.UNINSTALL
}

internal fun PackageDetailsDto.toDomain() = AppDetails(
    packageName = packageName,
    versionName = versionName,
    versionCode = versionCode,
    targetSdk = targetSdk,
    minSdk = minSdk,
    firstInstallTime = firstInstallTime,
    lastUpdateTime = lastUpdateTime,
    codeSizeBytes = codeSizeBytes,
    dataSizeBytes = dataSizeBytes,
    cacheSizeBytes = cacheSizeBytes,
)
