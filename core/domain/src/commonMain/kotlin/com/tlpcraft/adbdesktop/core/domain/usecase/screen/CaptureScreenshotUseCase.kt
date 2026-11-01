package com.tlpcraft.adbdesktop.core.domain.usecase.screen

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

class CaptureScreenshotUseCase(
    private val repository: ScreenRepository,
) : UseCase<String, Outcome<ByteArray, DeviceError>> {

    override suspend fun invoke(param: String): Outcome<ByteArray, DeviceError> = repository.captureScreenshot(param)
}
