package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class GetAppDetailsUseCase(
    private val repository: AppsRepository,
) : UseCase<GetAppDetailsUseCase.Params, Outcome<AppDetails, DeviceError>> {

    data class Params(val serial: String, val packageName: String)

    override suspend fun invoke(param: Params): Outcome<AppDetails, DeviceError> = repository.getAppDetails(param.serial, param.packageName)
}
