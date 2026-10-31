package com.tlpcraft.adbdesktop.domain.service

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides [CoroutineDispatcher] instances for structured concurrency across the application.
 *
 * Abstracting dispatchers behind an interface allows production code to use platform dispatchers
 * while tests can inject controlled alternatives, such as [kotlinx.coroutines.test.UnconfinedTestDispatcher],
 * to ensure deterministic and synchronous execution.
 *
 * @property main Dispatcher confined to the main thread. Used for UI updates and view model operations.
 * @property io Dispatcher optimized for blocking I/O work such as database queries and network requests.
 * @property default Dispatcher optimized for CPU-intensive work such as sorting, parsing, and computation.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
