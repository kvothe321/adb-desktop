package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageEventDataSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow

/** Reports what is sent to [changes]; with [canReport] off it ends at once, like a device without the callback. */
class FakeAdbPackageEventDataSource(private val canReport: Boolean = true) : AdbPackageEventDataSource {
    val changes = Channel<String>(Channel.UNLIMITED)
    var listeners = 0
        private set

    override fun streamPackageChanges(serial: String): Flow<String> {
        listeners++
        return if (canReport) changes.receiveAsFlow() else emptyFlow()
    }
}
