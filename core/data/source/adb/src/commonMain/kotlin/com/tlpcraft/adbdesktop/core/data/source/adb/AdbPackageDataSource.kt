package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageDetailsDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto

interface AdbPackageDataSource {
    suspend fun listPackages(serial: String, filter: PackageFilterDto): List<String>

    suspend fun readPackageDetails(serial: String, packageName: String): PackageDetailsDto

    suspend fun runAction(serial: String, packageName: String, action: PackageActionDto)
}
