package com.tlpcraft.adbdesktop.feature.devices

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.CaptureScreenshotUseCase
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesSideEffect

/** The quick actions that do their work right on the overview, each ending in the message to show. */
class QuickActionRunner(
    private val captureScreenshot: CaptureScreenshotUseCase,
    private val installApk: InstallApkUseCase,
    private val hostClipboard: HostClipboard,
    private val hostFiles: HostFiles,
) {
    suspend fun copyScreenshot(serial: String): DevicesSideEffect = when (val result = captureScreenshot(serial)) {
        is Outcome.Success -> DevicesSideEffect.ScreenshotCopied.also { hostClipboard.copyPng(result.data) }
        is Outcome.Failure -> DevicesSideEffect.ActionFailed(result.error.detailsForUser())
    }

    suspend fun pickApks(): List<String> = hostFiles.pickFiles(APK_EXTENSION)

    suspend fun install(serial: String, paths: List<String>): DevicesSideEffect = when (val result = installApk(InstallApkUseCase.Params(serial, paths))) {
        is Outcome.Success -> DevicesSideEffect.Installed(paths.size)
        is Outcome.Failure -> DevicesSideEffect.ActionFailed(result.error.detailsForUser())
    }

    private fun DeviceError.detailsForUser(): String = (this as? DeviceError.CommandFailed)?.details ?: message

    private companion object {
        const val APK_EXTENSION = "apk"
    }
}
