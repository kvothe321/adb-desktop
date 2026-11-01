package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveAppDetailsUseCase(
    private val repository: AppsRepository,
) : ReactiveUseCase<ObserveAppDetailsUseCase.Params, Flow<Outcome<AppDetails, DeviceError>>> {

    data class Params(val serial: String, val packageName: String)

    override fun invoke(param: Params): Flow<Outcome<AppDetails, DeviceError>> = repository.observeAppDetails(param.serial, param.packageName)
}
