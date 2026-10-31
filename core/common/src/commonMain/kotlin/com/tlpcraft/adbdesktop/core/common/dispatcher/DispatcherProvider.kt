package com.tlpcraft.adbdesktop.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/** Injectable dispatchers, so tests can swap in a test dispatcher. */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
