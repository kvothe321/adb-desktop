package com.tlpcraft.adbdesktop.core.data.repository.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbLogcatDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Each stream plays the next script: its batches, then a failure, a normal end, or no end when it is the last. */
class FakeAdbLogcatDataSource(private vararg val streams: Script) : AdbLogcatDataSource {
    class Script(val batches: List<List<LogEntryDto>>, val failure: String? = null)

    val requestedSince = mutableListOf<String?>()

    override fun streamLogs(serial: String, since: String?): Flow<List<LogEntryDto>> = flow {
        val script = streams.getOrNull(requestedSince.size)
        requestedSince += since
        if (script == null) awaitCancellation()
        script.batches.forEach { emit(it) }
        script.failure?.let { throw AdbException.CommandFailed(listOf("logcat"), exitCode = 1, output = it) }
    }

    override suspend fun clearLogs(serial: String) = Unit

    override suspend fun readProcessIds(serial: String, packageName: String): Set<Int> = emptySet()
}
