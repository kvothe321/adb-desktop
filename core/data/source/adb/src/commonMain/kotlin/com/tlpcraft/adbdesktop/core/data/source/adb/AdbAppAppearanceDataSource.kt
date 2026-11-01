package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.AppAppearanceDto
import kotlinx.coroutines.flow.Flow

interface AdbAppAppearanceDataSource {
    /** Emits each package's name and launcher icon once they are read. */
    fun streamAppearances(serial: String, packageNames: List<String>): Flow<AppAppearanceDto>
}
