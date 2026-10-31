package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class GetAppsUseCase(
    private val repository: AppsRepository,
) : UseCase<GetAppsUseCase.Params, Outcome<List<AppInfo>, DeviceError>> {

    data class Params(val serial: String, val filter: AppFilter)

    override suspend fun invoke(param: Params): Outcome<List<AppInfo>, DeviceError> = repository.getApps(param.serial, param.filter)
        .map { apps -> apps.sortedBy { it.packageName } }
}
