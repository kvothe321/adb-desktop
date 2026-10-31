package com.tlpcraft.adbdesktop.core.common

/**
 * Result of an operation that either succeeds with [T] or fails with a typed [E].
 *
 * [E] is covariant so an `Outcome<T, DeviceError>` can be widened to `Outcome<T, DomainError>`.
 * It must never appear in an `in` position; [UnsafeVariance] only threads it through transformations.
 */
sealed interface Outcome<out T, out E : DomainError> {
    data class Success<out T>(val data: T) : Outcome<T, Nothing>

    data class Failure<out E : DomainError>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, E : DomainError, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
}

inline fun <T, E : DomainError, R> Outcome<T, E>.flatMap(transform: (T) -> Outcome<R, E>): Outcome<R, E> = when (this) {
    is Outcome.Success -> transform(data)
    is Outcome.Failure -> this
}

inline fun <T, E : DomainError, R> Outcome<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
    is Outcome.Success -> onSuccess(data)
    is Outcome.Failure -> onFailure(error)
}

inline fun <T, E : DomainError> Outcome<T, E>.onSuccess(action: (T) -> Unit): Outcome<T, E> = also { if (it is Outcome.Success) action(it.data) }

inline fun <T, E : DomainError> Outcome<T, E>.onFailure(action: (E) -> Unit): Outcome<T, E> = also { if (it is Outcome.Failure) action(it.error) }

fun <T, E : DomainError> Outcome<T, E>.getOrNull(): T? = (this as? Outcome.Success)?.data
