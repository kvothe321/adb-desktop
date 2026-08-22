package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveAppsUseCase(
    private val repository: AppsRepository,
) : ReactiveUseCase<ObserveAppsUseCase.Params, Flow<Outcome<List<AppInfo>, DeviceError>>> {

    data class Params(val serial: String, val filter: AppFilter)

    override fun invoke(param: Params): Flow<Outcome<List<AppInfo>, DeviceError>> = repository.observeApps(param.serial, param.filter)
        .map { outcome -> outcome.map { apps -> apps.sortedBy { it.packageName } } }
}
