package com.tlpcraft.adbdesktop.core.data.source.adb.helper

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageEventDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/** Listens with the device helper, which registers for the same app changes a launcher hears about. */
class HelperPackageEventDataSource(private val helper: DeviceHelper) : AdbPackageEventDataSource {

    override fun streamPackageChanges(serial: String): Flow<String> = helper.run(serial, MAIN_CLASS, listOf(emptyList()))
        .mapNotNull { line -> line.split(' ').takeIf { it.size == 2 && it[0] in EVENTS }?.get(1) }

    private companion object {
        const val MAIN_CLASS = "PackageEvents"
        val EVENTS = setOf("added", "removed", "changed")
    }
}
