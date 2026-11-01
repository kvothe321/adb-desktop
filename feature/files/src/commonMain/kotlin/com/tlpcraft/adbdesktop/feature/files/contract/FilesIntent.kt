package com.tlpcraft.adbdesktop.feature.files.contract

import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile

sealed interface FilesIntent {
    data class EntryClicked(val file: RemoteFile) : FilesIntent

    data object UpClicked : FilesIntent

    data class PathChanged(val value: String) : FilesIntent

    data object PathSubmitted : FilesIntent

    data class VisibilityChanged(val isVisible: Boolean) : FilesIntent

    data object UploadClicked : FilesIntent

    data class FilesDropped(val paths: List<String>) : FilesIntent

    data object CancelTransferClicked : FilesIntent

    data class DownloadClicked(val file: RemoteFile) : FilesIntent

    data class DeleteClicked(val file: RemoteFile) : FilesIntent

    data object DeleteConfirmed : FilesIntent

    data object DeleteCancelled : FilesIntent

    data object NewFolderClicked : FilesIntent

    data class NewFolderNameChanged(val value: String) : FilesIntent

    data object NewFolderSubmitted : FilesIntent

    data object NewFolderDismissed : FilesIntent
}
