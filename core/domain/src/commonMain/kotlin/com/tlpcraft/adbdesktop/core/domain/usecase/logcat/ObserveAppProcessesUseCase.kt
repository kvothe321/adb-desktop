package com.tlpcraft.adbdesktop.core.domain.usecase.logcat

import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAppProcessesUseCase(
    private val repository: LogcatRepository,
) : ReactiveUseCase<ObserveAppProcessesUseCase.Params, Flow<Set<Int>>> {

    data class Params(val serial: String, val packageName: String)

    override fun invoke(param: Params): Flow<Set<Int>> = repository.observeProcessIds(param.serial, param.packageName)
}
