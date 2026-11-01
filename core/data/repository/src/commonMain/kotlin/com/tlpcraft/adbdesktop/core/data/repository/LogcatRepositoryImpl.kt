package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbLogcatDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class LogcatRepositoryImpl(
    private val logcatDataSource: AdbLogcatDataSource,
    private val processPollInterval: Duration = 2.seconds,
    private val firstReconnectDelay: Duration = 1.seconds,
    private val longestReconnectDelay: Duration = 8.seconds,
) : LogcatRepository {

    override fun observeLogs(serial: String): Flow<Outcome<List<LogEntry>, DeviceError>> = flow {
        val delivered = DeliveredEntries()
        var reconnectDelay = firstReconnectDelay
        while (true) {
            val failure = streamOnce(serial, delivered) { reconnectDelay = firstReconnectDelay }
            if (failure != null) emit(Outcome.Failure(failure))
            delay(reconnectDelay)
            reconnectDelay = (reconnectDelay * 2).coerceAtMost(longestReconnectDelay)
        }
    }

    /** Streams until logcat stops, returning why if it failed. */
    private suspend fun FlowCollector<Outcome<List<LogEntry>, DeviceError>>.streamOnce(
        serial: String,
        delivered: DeliveredEntries,
        onEntries: () -> Unit,
    ): DeviceError? {
        var failure: DeviceError? = null
        delivered.startStream()
        logcatDataSource.streamLogs(serial, since = delivered.lastTimestamp)
            .catch { failure = it.toDeviceError() }
            .collect { batch ->
                val fresh = delivered.takeNew(batch)
                if (fresh.isNotEmpty()) {
                    onEntries()
                    emit(Outcome.Success(fresh.map { it.toDomain() }))
                }
            }
        return failure
    }

    override suspend fun clearLogs(serial: String): Outcome<Unit, DeviceError> = safeCall(Throwable::toDeviceError) { logcatDataSource.clearLogs(serial) }

    override fun observeProcessIds(serial: String, packageName: String): Flow<Set<Int>> = flow {
        while (true) {
            emit(logcatDataSource.readProcessIds(serial, packageName))
            delay(processPollInterval)
        }
    }.distinctUntilChanged()
}

/**
 * Resuming from a timestamp replays the entries at that timestamp, so how many of each were delivered there is counted
 * and that many are skipped on the way back. Identical entries in one stream are all kept, as logs repeat. Timestamps
 * compare as text, which holds within a year.
 */
private class DeliveredEntries {
    var lastTimestamp: String? = null
        private set
    private val deliveredAtLast = mutableMapOf<LogEntryDto, Int>()
    private val replayed = mutableMapOf<LogEntryDto, Int>()
    private var isReplaying = false

    fun startStream() {
        isReplaying = lastTimestamp != null
        replayed.clear()
        replayed.putAll(deliveredAtLast)
    }

    fun takeNew(batch: List<LogEntryDto>): List<LogEntryDto> = batch.filterNot(::isReplay).onEach(::deliver)

    private fun isReplay(entry: LogEntryDto): Boolean {
        val last = lastTimestamp?.takeIf { isReplaying } ?: return false
        return when {
            entry.timestamp > last -> false.also { isReplaying = false }
            entry.timestamp < last -> true
            else -> consumeReplayed(entry)
        }
    }

    private fun consumeReplayed(entry: LogEntryDto): Boolean {
        val left = replayed[entry] ?: 0
        if (left > 0) replayed[entry] = left - 1
        return left > 0
    }

    private fun deliver(entry: LogEntryDto) {
        if (entry.timestamp != lastTimestamp) {
            lastTimestamp = entry.timestamp
            deliveredAtLast.clear()
        }
        deliveredAtLast[entry] = (deliveredAtLast[entry] ?: 0) + 1
    }
}
