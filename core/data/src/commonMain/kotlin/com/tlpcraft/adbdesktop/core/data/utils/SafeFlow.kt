package com.tlpcraft.adbdesktop.core.data.utils

import com.tlpcraft.adbdesktop.domain.DomainError
import com.tlpcraft.adbdesktop.domain.Outcome
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Wraps a [Flow]-returning ADB [block] into a [Flow] of [Outcome], translating
 * any thrown exception (whether at construction or during emission) into a typed
 * [E] error via [errorMapper].
 *
 * This is the sole try/catch boundary for reactive ADB operations in the data layer.
 * All [Flow]-based device or package observations must go through [safeFlow]. Never
 * handle exceptions directly in a repository implementation.
 *
 * The reactive counterpart to [safeCall].
 *
 * ## Inlining
 *
 * This function is `inline`. Both [errorMapper] and [block] are marked `crossinline`
 * because they are captured inside nested lambda contexts ([Flow.catch], [flow { }])
 * which do not permit non-local returns. `crossinline` preserves the inlining benefit -
 * zero lambda allocations at the call site while enforcing that restriction.
 *
 * ## Two catch sites - both are necessary
 *
 * Unlike [safeCall], a [Flow]-based operation can fail in two distinct phases:
 *
 * - **Construction**: an exception thrown synchronously when [block] is invoked —
 *   for example, if the ADB server is not running or the device connection cannot
 *   be established before the first emission.
 *   The outer `try/catch` handles this, emitting a single [Outcome.Failure] and
 *   completing the flow.
 * - **Emission**: an exception thrown asynchronously while the [Flow] is active —
 *   for example, the device disconnecting mid-stream or the ADB daemon being killed.
 *   The [Flow.catch] operator handles this, emitting a single [Outcome.Failure] and
 *   completing the flow.
 *
 * Removing either catch site creates a silent gap. Both must remain.
 *
 * ## Contract
 *
 * - Each value emitted by [block] is wrapped in [Outcome.Success] and re-emitted downstream.
 * - If [block] throws during construction, a single [Outcome.Failure] is emitted
 *   and the flow completes.
 * - If the inner [Flow] throws during emission, a single [Outcome.Failure] is emitted
 *   and the flow completes.
 * - [errorMapper] must be a pure, total function — it must handle every possible [Throwable]
 *   and must not throw. An [errorMapper] that throws is a bug, not a feature.
 * - [block] must return a cold [Flow] representing a single, coherent ADB observation.
 *   Do not compose unrelated streams inside a single [safeFlow].
 * - Downstream collectors must not throw. Exceptions thrown by a downstream collector
 *   are not caught by [safeFlow] and will propagate normally.
 *
 * ## Usage
 *
 * ```kotlin
 * override fun observeConnectedDevices(): Flow<Outcome<List<Device>, AdbError>> =
 *     safeFlow(Throwable::toAdbError) {
 *         adbClient.deviceFlow()
 *             .map { it.map { device -> device.toModel() } }
 *             .flowOn(dispatchers.io)
 *     }
 * ```
 *
 * @param T The type of each value emitted by [block] on the success path.
 * @param E The typed domain error produced by [errorMapper] on the failure path.
 * @param errorMapper A total function that maps any [Throwable] to a typed [E].
 * Must not throw. Must not return null. Must cover all cases.
 * @param block A function returning the cold [Flow] to execute and observe.
 * Must not catch its own exceptions, that is [safeFlow]'s job.
 * @return A [Flow] emitting [Outcome.Success] for each upstream value, or a single
 * [Outcome.Failure] if an exception occurs at construction or during emission.
 *
 * @see safeCall
 * @see Outcome
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T, E : DomainError> safeFlow(crossinline errorMapper: (Throwable) -> E, crossinline block: () -> Flow<T>): Flow<Outcome<T, E>> = try {
    block()
        .map<T, Outcome<T, E>> { Outcome.Success(it) }
        .catch { e -> emit(Outcome.Failure(errorMapper(e))) }
} catch (e: CancellationException) {
    // Re-throw to ensure the coroutine stays cancellable
    throw e
} catch (e: Throwable) {
    flow { emit(Outcome.Failure(errorMapper(e))) }
}
