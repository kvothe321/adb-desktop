package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAppAppearancesUseCase(
    private val repository: AppsRepository,
) : ReactiveUseCase<ObserveAppAppearancesUseCase.Params, Flow<Map<String, AppAppearance>>> {

    data class Params(val serial: String, val packageNames: List<String>)

    override fun invoke(param: Params): Flow<Map<String, AppAppearance>> = repository.observeAppAppearances(param.serial, param.packageNames)
}
