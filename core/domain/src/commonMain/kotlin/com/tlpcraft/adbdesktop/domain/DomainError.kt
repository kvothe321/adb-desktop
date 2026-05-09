package com.tlpcraft.adbdesktop.domain

/**
 * Marker interface for typed errors in the domain layer.
 *
 * All error categories (e.g:[StorageError], [NetworkError], [BusinessError]) should
 * implement this interface, providing a shared contract for error handling across the application.
 *
 * Errors are expressed in domain terms, never in infrastructure terms such as
 * [android.database.sqlite.SQLiteException] or [java.io.IOException]. Translation from
 * infrastructure exceptions to [DomainError] subtypes happens exclusively in the data layer.
 *
 * Implementations should be sealed classes to ensure exhaustive handling at call sites:
 * ```Kotlin
 * sealed class StorageError : Error {
 *     data object NotFound : StorageError() { ... }
 *     data class Unexpected(override val cause: Throwable) : StorageError() { ... }
 * }
 * ```
 *
 * @property cause The underlying throwable that triggered this error, if available.
 * Should only be used for observability (Crashlytics, logging) — never for control flow.
 * @property isFatal Whether this error represents an unrecoverable state.
 * Fatal errors should be reported to Crashlytics and may warrant terminating the current session.
 */
interface DomainError {
    /**
     * A human-readable description of the error, suitable for logging.
     * Not intended for direct display in the UI. One should map to user-facing strings in the presentation layer.
     */
    val message: String

    /**
     * The underlying throwable that triggered this error, if available.
     * Should only be used for observability (Crashlytics, logging), never for control flow.
     */
    val cause: Throwable?

    /**
     * Whether this error represents an unrecoverable state.
     * Fatal errors should be reported to Crashlytics and may warrant terminating the current session.
     */
    val isFatal: Boolean
}
