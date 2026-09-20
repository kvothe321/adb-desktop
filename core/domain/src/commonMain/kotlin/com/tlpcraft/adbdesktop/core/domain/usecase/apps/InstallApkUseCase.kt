package com.tlpcraft.adbdesktop.core.domain.usecase.apps

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class InstallApkUseCase(
    private val repository: AppsRepository,
) : UseCase<InstallApkUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val serial: String, val apkPaths: List<String>)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> = repository.install(param.serial, param.apkPaths)
}
