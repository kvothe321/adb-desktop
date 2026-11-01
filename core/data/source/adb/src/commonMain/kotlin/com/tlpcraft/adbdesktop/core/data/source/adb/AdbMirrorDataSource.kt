package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorFrameDto
import kotlinx.coroutines.flow.Flow

interface AdbMirrorDataSource {
    /** Streams the device screen until cancelled, sending [control] to the device meanwhile and dropping frames the collector has no time for. */
    fun streamFrames(serial: String, control: Flow<MirrorControlDto>): Flow<MirrorFrameDto>
}
