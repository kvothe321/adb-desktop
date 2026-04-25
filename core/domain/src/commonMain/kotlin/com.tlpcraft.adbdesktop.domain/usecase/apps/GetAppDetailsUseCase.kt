package com.tlpcraft.adbdesktop.domain.usecase.apps

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.domain.usecase.UseCase

class GetAppDetailsUseCase(
    private val repository: AppsRepository,
) : UseCase<GetAppDetailsUseCase.Params, Outcome<AppDetails, DeviceError>> {

    data class Params(val deviceSerial: String, val packageName: String)

    override suspend fun invoke(param: Params): Outcome<AppDetails, DeviceError> = repository.getAppDetails(param.deviceSerial, param.packageName)
}
