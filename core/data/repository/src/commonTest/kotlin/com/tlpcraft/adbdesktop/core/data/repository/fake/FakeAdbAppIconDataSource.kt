package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAdbAppIconDataSource(private val icons: Map<String, ByteArray?> = emptyMap(), private val failsAfter: Int? = null) : AdbAppIconDataSource {
    val requested = mutableListOf<List<String>>()

    override fun streamIcons(serial: String, packageNames: List<String>): Flow<Pair<String, ByteArray?>> = flow {
        requested += packageNames
        packageNames.forEachIndexed { index, name ->
            if (index == failsAfter) throw AdbException.DeviceNotFound(serial)
            emit(name to icons[name])
        }
    }
}
