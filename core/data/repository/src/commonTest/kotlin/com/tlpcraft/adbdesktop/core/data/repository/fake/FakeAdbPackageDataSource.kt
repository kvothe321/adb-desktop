package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageDetailsDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto

class FakeAdbPackageDataSource : AdbPackageDataSource {
    val requestedFilters = mutableListOf<PackageFilterDto>()
    val actions = mutableListOf<PackageActionDto>()
    val installed = mutableListOf<List<String>>()

    override suspend fun listPackages(serial: String, filter: PackageFilterDto): List<String> {
        requestedFilters += filter
        return listOf("com.example.notes")
    }

    override suspend fun readPackageDetails(serial: String, packageName: String): PackageDetailsDto = throw AdbException.DeviceNotFound(serial)

    override suspend fun runAction(serial: String, packageName: String, action: PackageActionDto) {
        actions += action
    }

    override suspend fun install(serial: String, apkPaths: List<String>) {
        installed += apkPaths
    }
}
