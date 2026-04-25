package com.tlpcraft.adbdesktop.domain.usecase.apps

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.domain.usecase.UseCase

/**
 * Fetches installed packages on a device, filtered by [GetAppsParams.filter].
 *
 * This is a one-shot [UseCase]: call it whenever the selected device or filter changes.
 */
class GetAppsUseCase(
    private val repository: AppsRepository,
) : UseCase<GetAppsUseCase.Params, Outcome<List<AppInfo>, DeviceError>> {

    data class Params(val deviceSerial: String, val filter: AppFilter)

    override suspend fun invoke(param: Params): Outcome<List<AppInfo>, DeviceError> = repository.getApps(param.deviceSerial, param.filter)
}
