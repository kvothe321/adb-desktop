package com.tlpcraft.adbdesktop.domain.usecase.apps

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.domain.usecase.UseCase

class ExecuteAppActionUseCase(
    private val repository: AppsRepository,
) : UseCase<ExecuteAppActionUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val deviceSerial: String, val packageName: String, val action: AppAction)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.executeAction(param.deviceSerial, param.packageName, param.action)
}
