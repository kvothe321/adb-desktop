package com.tlpcraft.adbdesktop.domain.usecase

/**
 * Defines the contract for a Use Case (also known as an Interactor) in Clean Architecture.
 * A Use Case encapsulates a single, specific piece of business logic of the application.
 * It is executed by a presenter/view model and typically interacts with one or more repositories.
 *
 * This interface is designed to be implemented by classes that handle a specific user action or a system event.
 * The execution is performed asynchronously via a suspend function.
 *
 * @param PARAM The type of the input parameters required to execute the use case. Use [Unit] if no parameters are needed.
 * @param RESULT The type of the result produced by the use case. It's recommended to wrap this in a result-handling class (e.g., [RESULT], [Either])
 * to manage success and failure states gracefully.
 */
interface UseCase<in PARAM, out RESULT> {
    /**
     * Executes the business logic of the use case.
     *
     * The `operator` modifier allows the class instance to be invoked as if it were a function,
     * for example: `myUseCase(params)`.
     *
     * As a `suspend` function, it must be called from a coroutine or another suspend function
     * to handle asynchronous operations without blocking the main thread.
     *
     * @param param The input parameters for the use case.
     * @return The [RESULT] of the use case's execution.
     */
    suspend operator fun invoke(param: PARAM): RESULT
}
