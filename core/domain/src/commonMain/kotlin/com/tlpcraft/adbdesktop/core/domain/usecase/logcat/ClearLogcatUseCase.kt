package com.tlpcraft.adbdesktop.core.domain.usecase.logcat

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class ClearLogcatUseCase(
    private val repository: LogcatRepository,
) : UseCase<String, Outcome<Unit, DeviceError>> {

    override suspend fun invoke(param: String): Outcome<Unit, DeviceError> = repository.clearLogs(param)
}
