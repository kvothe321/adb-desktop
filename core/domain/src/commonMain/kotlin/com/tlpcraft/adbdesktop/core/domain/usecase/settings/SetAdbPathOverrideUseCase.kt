package com.tlpcraft.adbdesktop.core.domain.usecase.settings

import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Stores a custom adb path. A blank path switches back to automatic detection. */
class SetAdbPathOverrideUseCase(
    private val repository: AdbSettingsRepository,
) : UseCase<String?, Unit> {

    override suspend fun invoke(param: String?) {
        repository.setAdbPathOverride(param?.trim()?.takeIf { it.isNotEmpty() })
    }
}
