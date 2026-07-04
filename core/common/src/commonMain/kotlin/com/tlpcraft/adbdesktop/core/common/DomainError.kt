package com.tlpcraft.adbdesktop.core.common

/**
 * Contract for typed errors carried by [Outcome.Failure].
 *
 * Errors are described in domain terms. Infrastructure exceptions are translated at the data layer
 * boundary and never reach domain or presentation code.
 */
interface DomainError {
    /** Diagnostic description for logs. Presentation maps errors to localized strings instead. */
    val message: String

    /** The underlying failure, kept for logging only. */
    val cause: Throwable?
}
