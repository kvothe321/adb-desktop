package com.tlpcraft.adbdesktop.core.domain.usecase.files

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.map
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Folders and links to them first, then by name. */
class ListFilesUseCase(
    private val repository: FilesRepository,
) : UseCase<ListFilesUseCase.Params, Outcome<List<RemoteFile>, DeviceError>> {

    data class Params(val serial: String, val directory: String)

    override suspend fun invoke(param: Params): Outcome<List<RemoteFile>, DeviceError> = repository.list(param.serial, param.directory)
        .map { files -> files.sortedWith(foldersFirstThenName) }

    private companion object {
        val foldersFirstThenName = compareByDescending<RemoteFile> { it.type.canOpen }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
}
