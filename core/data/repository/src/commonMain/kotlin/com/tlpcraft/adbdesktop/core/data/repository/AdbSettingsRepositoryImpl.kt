package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import kotlinx.coroutines.flow.Flow

class AdbSettingsRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
) : AdbSettingsRepository {

    override fun observeAdbPathOverride(): Flow<String?> = preferencesDataSource.observeAdbPath()

    override suspend fun setAdbPathOverride(path: String?) = preferencesDataSource.writeAdbPath(path)
}
