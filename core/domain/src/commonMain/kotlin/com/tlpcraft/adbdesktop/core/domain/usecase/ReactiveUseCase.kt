package com.tlpcraft.adbdesktop.core.domain.usecase

/** A [UseCase] that exposes a stream. Not suspending: the returned flow is the asynchronous boundary. */
interface ReactiveUseCase<in PARAM, out RESULT> {
    operator fun invoke(param: PARAM): RESULT
}
