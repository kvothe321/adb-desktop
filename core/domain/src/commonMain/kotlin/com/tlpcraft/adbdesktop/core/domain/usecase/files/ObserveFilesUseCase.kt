package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Folders and links to them first, then by name. */
class ObserveFilesUseCase(
    private val repository: FilesRepository,
) : ReactiveUseCase<ObserveFilesUseCase.Params, Flow<Outcome<List<RemoteFile>, DeviceError>>> {

    data class Params(val serial: String, val directory: String)

    override fun invoke(param: Params): Flow<Outcome<List<RemoteFile>, DeviceError>> = repository.observeFiles(param.serial, param.directory)
        .map { outcome -> outcome.map { files -> files.sortedWith(foldersFirstThenName) } }

    private companion object {
        val foldersFirstThenName = compareByDescending<RemoteFile> { it.type.canOpen }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
}
