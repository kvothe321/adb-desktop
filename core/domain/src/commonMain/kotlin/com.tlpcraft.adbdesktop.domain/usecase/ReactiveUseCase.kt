package com.tlpcraft.adbdesktop.domain.usecase

/**
 * Defines the contract for a reactive Use Case (also known as an Interactor) in Clean Architecture.
 * A Use Case encapsulates a single, specific piece of business logic of the application.
 * It is executed by a presenter/view model and typically interacts with one or more repositories.
 *
 * This interface is designed for use cases that expose a continuous stream of data or events,
 * returning a [kotlinx.coroutines.flow.Flow] rather than a single value. Unlike [UseCase],
 * the invocation is not suspend - the asynchronous boundary is the [kotlinx.coroutines.flow.Flow] itself.
 *
 * @param PARAM The type of the input parameters required to execute the use case. Use [Unit] if no parameters are needed.
 * @param RESULT The type of the [kotlinx.coroutines.flow.Flow] produced by the use case. It's recommended to wrap
 * the flow's emission type in a result-handling class (e.g., [com.tlpcraft.adbdesktop.domain.Outcome]) to manage
 * success and failure states gracefully.
 *
 * @see UseCase
 */
interface ReactiveUseCase<in PARAM, out RESULT> {

    /**
     * Executes the business logic of the use case and returns a stream of results.
     *
     * The `operator` modifier allows the class instance to be invoked as if it were a function,
     * for example: `myUseCase(params)`.
     *
     * Unlike [UseCase.invoke], this function is not suspend. Collection of the returned
     * [kotlinx.coroutines.flow.Flow] should happen within an appropriate coroutine scope,
     * typically via [kotlinx.coroutines.flow.Flow.stateIn] in a ViewModel.
     *
     * @param param The input parameters for the use case.
     * @return The [RESULT] stream of the use case's execution.
     */
    operator fun invoke(param: PARAM): RESULT
}
