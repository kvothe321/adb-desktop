package com.tlpcraft.adbdesktop.feature.files.contract

import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile

const val DEFAULT_DIRECTORY = "/sdcard"

data class FilesUiState(
    val serial: String? = null,
    val directory: String = DEFAULT_DIRECTORY,
    val pathInput: String = DEFAULT_DIRECTORY,
    val listing: FileListing = FileListing.NoDevice,
    val isBusy: Boolean = false,
    val newFolder: NewFolderForm? = null,
    val pendingDelete: RemoteFile? = null,
) {
    val canGoUp: Boolean
        get() = directory != "/"
}

sealed interface FileListing {
    data object NoDevice : FileListing

    data object Loading : FileListing

    data class Loaded(val files: List<RemoteFile>) : FileListing

    data class Failed(val details: String) : FileListing
}

data class NewFolderForm(val name: String = "", val isInvalid: Boolean = false) {
    val canSubmit: Boolean
        get() = name.isNotBlank()
}
