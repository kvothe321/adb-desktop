package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppAppearanceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.AppAppearanceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAdbAppAppearanceDataSource(
    private val icons: Map<String, ByteArray?> = emptyMap(),
    private val labels: Map<String, String> = emptyMap(),
    private val failsAfter: Int? = null,
) : AdbAppAppearanceDataSource {
    val requested = mutableListOf<List<String>>()

    override fun streamAppearances(serial: String, packageNames: List<String>): Flow<AppAppearanceDto> = flow {
        requested += packageNames
        packageNames.forEachIndexed { index, name ->
            if (index == failsAfter) throw AdbException.DeviceNotFound(serial)
            emit(AppAppearanceDto(name, labels[name], icons[name]))
        }
    }
}
