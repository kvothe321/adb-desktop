package com.tlpcraft.adbdesktop.core.data.repository.util

import com.tlpcraft.adbdesktop.core.common.DomainError
import com.tlpcraft.adbdesktop.core.common.Outcome
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * The single boundary where data source exceptions become typed errors. Cancellation is rethrown
 * so structured concurrency keeps working.
 */
@Suppress("TooGenericExceptionCaught") // Deliberate: this is the one place every data source failure is translated.
suspend inline fun <T, E : DomainError> safeCall(errorMapper: (Throwable) -> E, block: suspend () -> T): Outcome<T, E> = try {
    Outcome.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Outcome.Failure(errorMapper(e))
}

/** Reactive counterpart of [safeCall]: a failure is emitted once and completes the flow. */
fun <T, E : DomainError> Flow<T>.asOutcomeFlow(errorMapper: (Throwable) -> E): Flow<Outcome<T, E>> = map<T, Outcome<T, E>> { Outcome.Success(it) }
    .catch { emit(Outcome.Failure(errorMapper(it))) }
