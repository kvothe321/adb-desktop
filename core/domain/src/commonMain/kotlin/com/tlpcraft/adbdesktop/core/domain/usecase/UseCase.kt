package com.tlpcraft.adbdesktop.core.domain.usecase

/** A single piece of business logic, invoked as a function. Use [Unit] when there is no input. */
interface UseCase<in PARAM, out RESULT> {
    suspend operator fun invoke(param: PARAM): RESULT
}
