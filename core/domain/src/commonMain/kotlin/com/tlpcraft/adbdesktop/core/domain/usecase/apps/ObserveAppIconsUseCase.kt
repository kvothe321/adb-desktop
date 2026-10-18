package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.domain.model.AppIcon
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAppIconsUseCase(
    private val repository: AppsRepository,
) : ReactiveUseCase<ObserveAppIconsUseCase.Params, Flow<Map<String, AppIcon>>> {

    data class Params(val serial: String, val packageNames: List<String>)

    override fun invoke(param: Params): Flow<Map<String, AppIcon>> = repository.observeAppIcons(param.serial, param.packageNames)
}
