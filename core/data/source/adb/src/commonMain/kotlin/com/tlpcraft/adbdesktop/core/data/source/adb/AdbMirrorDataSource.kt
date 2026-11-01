package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorFrameDto
import kotlinx.coroutines.flow.Flow

interface AdbMirrorDataSource {
    /** Streams the device screen until cancelled, dropping frames the collector has no time for. */
    fun streamFrames(serial: String): Flow<MirrorFrameDto>
}
