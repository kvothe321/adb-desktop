package com.tlpcraft.adbdesktop.core.common.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

/** Collects this flow only while [active] is `true`, and starts it over each time it turns `true` again. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.whileActive(active: Flow<Boolean>): Flow<T> = active.distinctUntilChanged().flatMapLatest { isActive ->
    if (isActive) this else emptyFlow()
}
