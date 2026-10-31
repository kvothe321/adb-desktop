package com.tlpcraft.adbdesktop.domain

/**
 * Represents the outcome of an operation that can either succeed with a value of type [T],
 * or fail with a typed error of type [E].
 *
 * [E] is declared covariant (out) to allow assignment compatibility across [DomainError] subtypes.
 * For example, an [Outcome]<Unit, StorageError> can be widened to [Outcome]<Unit, Error>.
 * [UnsafeVariance] is used in composition functions solely to thread [E] through transformations.
 * [E] must never appear in a consuming (in) position in any method added to this class.
 *
 * Usage:
 * ```Kotlin
 * suspend fun deleteStudySet(id: String): Outcome<Unit, StorageError>
 *
 * viewModelScope.launch {
 *     repo.deleteStudySet(id).fold(
 *         onSuccess = { uiState = UiState.Idle },
 *         onFailure = { error -> uiState = UiState.Error(error.message) }
 *     )
 * }
 * ```
 *
 * @param T The type of the value carried on the success path.
 * @param E The type of the error carried on the failure path. Must implement [DomainError].
 */
sealed class Outcome<out T, out E : DomainError> {

    /**
     * Represents a successful outcome carrying a value of type [T].
     *
     * @param T The type of the success value.
     * @property data The value produced by the successful operation.
     */
    data class Success<out T>(val data: T) : Outcome<T, Nothing>()

    /**
     * Represents a failed outcome carrying a typed error of type [E].
     *
     * @param E The type of the error. Must implement [DomainError].
     * @property error The error that caused the operation to fail.
     */
    data class Failure<out E : DomainError>(val error: E) : Outcome<Nothing, E>()

    /**
     * Transforms the success value using [transform], leaving any [Failure] unchanged.
     *
     * @param R The type of the transformed success value.
     * @param transform A function applied to the success value.
     * @return A new [Outcome] with the transformed value, or the original [Failure].
     */
    inline fun <R> map(transform: (T) -> R): Outcome<R, @UnsafeVariance E> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Chains a subsequent operation on the success value, short-circuiting on [Failure].
     *
     * Useful for sequencing operations where each step depends on the previous one succeeding:
     * ```
     * repo.getStudySet(id)
     *     .flatMap { set -> repo.getStudySetCharacters(set.id) }
     *     .fold(...)
     * ```
     *
     * @param R The type of the success value produced by [transform].
     * @param transform A function that takes the success value and returns a new [Outcome].
     * @return The [Outcome] returned by [transform], or the original [Failure] unchanged.
     */
    inline fun <R> flatMap(transform: (T) -> Outcome<R, @UnsafeVariance E>): Outcome<R, @UnsafeVariance E> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    /**
     * Collapses this [Outcome] into a single value of type [R] by applying either
     * [onSuccess] or [onFailure] depending on which path was taken.
     *
     * This is the primary way to consume an [Outcome] at a call site:
     * ```
     * val message = outcome.fold(
     *     onSuccess = { "Deleted successfully" },
     *     onFailure = { error -> error.message }
     * )
     * ```
     *
     * @param R The type of the resulting value.
     * @param onSuccess Applied to the success value if this is a [Success].
     * @param onFailure Applied to the error if this is a [Failure].
     * @return The result of whichever function was applied.
     */
    inline fun <R> fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }

    /**
     * Executes [action] with the success value if this is a [Success], then returns
     * this [Outcome] unchanged. A no-op on [Failure].
     *
     * Intended for side effects such as logging or analytics on the success path,
     * without interrupting a chain.
     *
     * @param action A side-effecting function applied to the success value.
     * @return This [Outcome], unchanged.
     */
    inline fun onSuccess(action: (T) -> Unit): Outcome<T, E> = also { if (it is Success) action(it.data) }

    /**
     * Executes [action] with the error if this is a [Failure], then returns
     * this [Outcome] unchanged. A no-op on [Success].
     *
     * Intended for side effects such as logging or Crashlytics reporting on the failure path,
     * without interrupting a chain.
     *
     * @param action A side-effecting function applied to the error.
     * @return This [Outcome], unchanged.
     */
    inline fun onFailure(action: (E) -> Unit): Outcome<T, E> = also { if (it is Failure) action(it.error) }
}
