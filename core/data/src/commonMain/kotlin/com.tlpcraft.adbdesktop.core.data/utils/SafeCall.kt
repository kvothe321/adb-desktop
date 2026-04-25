package com.tlpcraft.adbdesktop.core.data.utils

import com.tlpcraft.adbdesktop.domain.DomainError
import com.tlpcraft.adbdesktop.domain.Outcome
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a failable ADB [block] and wraps its result in an [Outcome],
 * translating any thrown exception into a typed [E] error via [errorMapper].
 *
 * This is the sole try/catch boundary in the data layer. All ADB operations
 * (device queries, package management, shell commands, file transfers, etc.)
 * must go through [safeCall] — never catch exceptions directly in a repository
 * implementation. One gate, one translation point, one place to blame when a
 * device goes offline or `adb` returns an unexpected exit code.
 *
 * ## Contract
 *
 * - If [block] completes successfully, its return value is wrapped in [Outcome.Success].
 * - If [block] throws a [CancellationException], it is rethrown immediately and unconditionally.
 *   Swallowing a [CancellationException] breaks structured concurrency. The coroutine would
 *   silently continue running after its scope has been cancelled. Don't be that code.
 * - If [block] throws any other [Throwable], [errorMapper] is invoked to translate it
 *   into a typed [E], which is then wrapped in [Outcome.Failure].
 * - [errorMapper] must be a pure, total function — it must handle every possible [Throwable]
 *   and must not throw. An [errorMapper] that throws is a bug, not a feature.
 * - [block] must represent a single, atomic ADB operation. Do not compose multiple
 *   commands inside a single [safeCall]. Use [Outcome.flatMap] to chain them instead.
 *
 * ## Usage
 *
 * ```kotlin
 * override suspend fun installPackage(deviceId: String, apkPath: String): Outcome<Unit, AdbError> =
 *     safeCall(Throwable::toAdbError) {
 *         adbClient.install(deviceId, apkPath)
 *     }
 *
 * override suspend fun forceStopApp(deviceId: String, packageName: String): Outcome<Unit, AdbError> =
 *     safeCall(Throwable::toAdbError) {
 *         adbClient.forceStop(deviceId, packageName)
 *     }
 * ```
 *
 * ## Error mapping
 *
 * [errorMapper] is responsible for translating ADB/IO exceptions into domain errors.
 * It is the last line of defence against [Throwable] leaking into the domain layer:
 *
 * ```kotlin
 * internal fun Throwable.toAdbError(): AdbError = when (this) {
 *     is AdbCommandRejectedException -> AdbError.CommandRejected(message)
 *     is DeviceNotFoundException     -> AdbError.DeviceNotFound
 *     is IOException                 -> AdbError.ConnectionLost(this)
 *     else                           -> AdbError.Unexpected(this)
 * }
 * ```
 *
 * @param T The type of the value produced by [block] on the success path.
 * @param E The typed domain error produced by [errorMapper] on the failure path.
 * @param errorMapper A total function that maps any [Throwable] to a typed [E].
 * Must not throw. Must not return null. Must cover all cases.
 * @param block The suspended ADB operation to execute. Must be atomic.
 * Must not catch its own exceptions, that is [safeCall]'s job.
 * @return [Outcome.Success] wrapping the result of [block], or [Outcome.Failure]
 * wrapping the mapped error.
 *
 * @see safeFlow
 * @see Outcome
 */
@Suppress("TooGenericExceptionCaught")
suspend inline fun <T, E : DomainError> safeCall(errorMapper: (Throwable) -> E, block: suspend () -> T): Outcome<T, E> = try {
    Outcome.Success(block())
} catch (e: CancellationException) {
    // Re-throw to ensure the coroutine stays cancellable
    throw e
} catch (e: Throwable) {
    Outcome.Failure(errorMapper(e))
}
