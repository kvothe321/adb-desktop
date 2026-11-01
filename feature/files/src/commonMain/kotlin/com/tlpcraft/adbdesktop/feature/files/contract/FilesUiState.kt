package com.tlpcraft.adbdesktop.feature.files.contract

import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile

const val DEFAULT_DIRECTORY = "/sdcard"

data class FilesUiState(
    val serial: String? = null,
    val directory: String = DEFAULT_DIRECTORY,
    val pathInput: String = DEFAULT_DIRECTORY,
    val listing: FileListing = FileListing.NoDevice,
    val isBusy: Boolean = false,
    val transfer: TransferState? = null,
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

/** [fraction] is `null` while the size is unknown, as for folders. [position] counts from 1 in a batch of [count]. */
data class TransferState(val name: String, val fraction: Float?, val position: Int, val count: Int)

data class NewFolderForm(val name: String = "", val isInvalid: Boolean = false) {
    val canSubmit: Boolean
        get() = name.isNotBlank()
}
