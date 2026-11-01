package com.tlpcraft.adbdesktop.feature.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.feature.files.component.DeleteFileDialog
import com.tlpcraft.adbdesktop.feature.files.component.FileRow
import com.tlpcraft.adbdesktop.feature.files.component.NewFolderDialog
import com.tlpcraft.adbdesktop.feature.files.component.TransferBar
import com.tlpcraft.adbdesktop.feature.files.contract.FileListing
import com.tlpcraft.adbdesktop.feature.files.contract.FilesIntent
import com.tlpcraft.adbdesktop.feature.files.contract.FilesSideEffect
import com.tlpcraft.adbdesktop.feature.files.contract.FilesUiState
import com.tlpcraft.adbdesktop.feature.files.contract.NewFolderForm
import com.tlpcraft.adbdesktop.feature.files.contract.TransferState
import com.tlpcraft.adbdesktop.feature.files.resources.Res
import com.tlpcraft.adbdesktop.feature.files.resources.files_drop_hint
import com.tlpcraft.adbdesktop.feature.files.resources.files_empty
import com.tlpcraft.adbdesktop.feature.files.resources.files_load_failed
import com.tlpcraft.adbdesktop.feature.files.resources.files_new_folder
import com.tlpcraft.adbdesktop.feature.files.resources.files_no_device_description
import com.tlpcraft.adbdesktop.feature.files.resources.files_no_device_title
import com.tlpcraft.adbdesktop.feature.files.resources.files_path_label
import com.tlpcraft.adbdesktop.feature.files.resources.files_refresh
import com.tlpcraft.adbdesktop.feature.files.resources.files_retry
import com.tlpcraft.adbdesktop.feature.files.resources.files_title
import com.tlpcraft.adbdesktop.feature.files.resources.files_up
import com.tlpcraft.adbdesktop.feature.files.resources.files_upload
import com.tlpcraft.adbdesktop.feature.files.resources.message_deleted
import com.tlpcraft.adbdesktop.feature.files.resources.message_downloaded
import com.tlpcraft.adbdesktop.feature.files.resources.message_folder_created
import com.tlpcraft.adbdesktop.feature.files.resources.message_operation_failed
import com.tlpcraft.adbdesktop.feature.files.resources.message_transfer_cancelled
import com.tlpcraft.adbdesktop.feature.files.resources.message_uploaded
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.FileDropArea
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Browses the selected device's files. One-shot results are reported through [onShowMessage]. */
@Composable
fun FilesScreen(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: FilesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect -> currentOnShowMessage(effect.message()) }
    }

    FilesContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun FilesContent(uiState: FilesUiState, onIntent: (FilesIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.files_title)) {
            if (uiState.serial != null) FilesActions(isBusy = uiState.isBusy, onIntent = onIntent)
        }
        if (uiState.serial == null) {
            Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(Res.string.files_no_device_title),
                    description = stringResource(Res.string.files_no_device_description),
                    icon = IconName.Folder,
                )
            }
        } else {
            PathBar(pathInput = uiState.pathInput, canGoUp = uiState.canGoUp, onIntent = onIntent)
            Divider()
            uiState.transfer?.let { transfer ->
                TransferBar(transfer = transfer, onCancel = { onIntent(FilesIntent.CancelTransferClicked) })
                Divider()
            }
            FileDropArea(
                onFilesDrop = { onIntent(FilesIntent.FilesDropped(it)) },
                hint = stringResource(Res.string.files_drop_hint),
                modifier = Modifier.weight(1f),
            ) {
                FileListingView(listing = uiState.listing, isBusy = uiState.isBusy, onIntent = onIntent)
            }
        }
        FilesDialogs(newFolder = uiState.newFolder, pendingDelete = uiState.pendingDelete, onIntent = onIntent)
    }
}

@Composable
private fun RowScope.FilesActions(isBusy: Boolean, onIntent: (FilesIntent) -> Unit) {
    if (isBusy) ProgressIndicator()
    Button(
        text = stringResource(Res.string.files_new_folder),
        onClick = { onIntent(FilesIntent.NewFolderClicked) },
        variant = ButtonVariant.Ghost,
        icon = IconName.Add,
        enabled = !isBusy,
    )
    Button(
        text = stringResource(Res.string.files_upload),
        onClick = { onIntent(FilesIntent.UploadClicked) },
        variant = ButtonVariant.Primary,
        icon = IconName.Upload,
        enabled = !isBusy,
    )
}

@Composable
private fun PathBar(pathInput: String, canGoUp: Boolean, onIntent: (FilesIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        IconButton(IconName.ChevronUp, contentDescription = stringResource(Res.string.files_up), onClick = {
            onIntent(FilesIntent.UpClicked)
        }, enabled = canGoUp)
        IconButton(IconName.Refresh, contentDescription = stringResource(Res.string.files_refresh), onClick = { onIntent(FilesIntent.RefreshClicked) })
        TextField(
            value = pathInput,
            onValueChange = { onIntent(FilesIntent.PathChanged(it)) },
            modifier = Modifier.weight(1f),
            placeholder = stringResource(Res.string.files_path_label),
            monospace = true,
            onSubmit = { onIntent(FilesIntent.PathSubmitted) },
        )
    }
}

@Composable
private fun FileListingView(
    listing: FileListing,
    isBusy: Boolean,
    onIntent: (FilesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (listing) {
            FileListing.NoDevice -> Unit

            FileListing.Loading -> ProgressIndicator()

            is FileListing.Failed -> EmptyState(
                title = stringResource(Res.string.files_load_failed),
                description = listing.details,
                tone = Tone.Error,
                icon = IconName.Warning,
                action = {
                    Button(text = stringResource(Res.string.files_retry), onClick = { onIntent(FilesIntent.RefreshClicked) }, icon = IconName.Refresh)
                },
            )

            is FileListing.Loaded -> if (listing.files.isEmpty()) {
                EmptyState(title = stringResource(Res.string.files_empty), icon = IconName.Folder)
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(listing.files, key = { it.path }) { file ->
                        FileRow(
                            file = file,
                            enabled = !isBusy,
                            onClick = { onIntent(FilesIntent.EntryClicked(file)) },
                            onDownload = { onIntent(FilesIntent.DownloadClicked(file)) },
                            onDelete = { onIntent(FilesIntent.DeleteClicked(file)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilesDialogs(newFolder: NewFolderForm?, pendingDelete: RemoteFile?, onIntent: (FilesIntent) -> Unit) {
    when {
        newFolder != null -> NewFolderDialog(
            form = newFolder,
            onNameChange = { onIntent(FilesIntent.NewFolderNameChanged(it)) },
            onSubmit = { onIntent(FilesIntent.NewFolderSubmitted) },
            onDismiss = { onIntent(FilesIntent.NewFolderDismissed) },
        )

        pendingDelete != null -> DeleteFileDialog(
            name = pendingDelete.name,
            onConfirm = { onIntent(FilesIntent.DeleteConfirmed) },
            onDismiss = { onIntent(FilesIntent.DeleteCancelled) },
        )
    }
}

private suspend fun FilesSideEffect.message(): String = when (this) {
    is FilesSideEffect.Uploaded -> getString(Res.string.message_uploaded, directory)
    is FilesSideEffect.Downloaded -> getString(Res.string.message_downloaded, name, directory)
    FilesSideEffect.TransferCancelled -> getString(Res.string.message_transfer_cancelled)
    is FilesSideEffect.Deleted -> getString(Res.string.message_deleted, name)
    is FilesSideEffect.FolderCreated -> getString(Res.string.message_folder_created, name)
    is FilesSideEffect.OperationFailed -> getString(Res.string.message_operation_failed, details)
}

private val previewFiles = listOf(
    RemoteFile("Download", "/sdcard/Download", RemoteFileType.DIRECTORY, sizeBytes = 4_096, "2026-01-28 11:18", "drwxrws---"),
    RemoteFile("notes.txt", "/sdcard/notes.txt", RemoteFileType.FILE, sizeBytes = 2_048, "2026-10-31 23:05", "-rw-rw----"),
)

@UiKitPreview
@Composable
private fun FilesContentLoadedPreview() = PreviewContext {
    FilesContent(uiState = FilesUiState(serial = "emulator-5554", listing = FileListing.Loaded(previewFiles)), onIntent = {})
}

@UiKitPreview
@Composable
private fun FilesContentTransferringPreview() = PreviewContext {
    FilesContent(
        uiState = FilesUiState(
            serial = "emulator-5554",
            listing = FileListing.Loaded(previewFiles),
            isBusy = true,
            transfer = TransferState("big.bin", fraction = 0.4f, position = 1, count = 1),
        ),
        onIntent = {},
    )
}

@UiKitPreview
@Composable
private fun FilesContentFailedPreview() = PreviewContext {
    FilesContent(uiState = FilesUiState(serial = "emulator-5554", listing = FileListing.Failed("Permission denied")), onIntent = {})
}

@UiKitPreview
@Composable
private fun FilesContentNoDevicePreview() = PreviewContext {
    FilesContent(uiState = FilesUiState(), onIntent = {})
}
