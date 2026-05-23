package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class ExecuteAppActionUseCase(
    private val repository: AppsRepository,
) : UseCase<ExecuteAppActionUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val packageName: String, val action: AppAction)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.executeAction(param.serial, param.packageName, param.action)
}
