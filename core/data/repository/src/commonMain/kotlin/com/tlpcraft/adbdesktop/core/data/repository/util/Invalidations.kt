package com.tlpcraft.adbdesktop.core.data.repository.util

import kotlin.time.Duration
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

/** Keeps device data current: our own changes refresh it at once, the [Duration] catches everything done elsewhere. */
internal class Invalidations {
    private val signals = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun invalidate() {
        signals.tryEmit(Unit)
    }

    /** Runs [read] on collection, on every [invalidate] and every [interval]. Emits only what changed. */
    fun <T> observe(interval: Duration, read: suspend () -> T): Flow<T> = merge(signals, ticks(interval))
        .onStart { emit(Unit) }
        .conflate()
        .map { read() }
        .distinctUntilChanged()

    private fun ticks(interval: Duration): Flow<Unit> = flow {
        while (true) {
            delay(interval)
            emit(Unit)
        }
    }
}
