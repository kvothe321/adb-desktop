package com.tlpcraft.adbdesktop.feature.files.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.feature.files.contract.NewFolderForm
import com.tlpcraft.adbdesktop.feature.files.resources.Res
import com.tlpcraft.adbdesktop.feature.files.resources.files_cancel
import com.tlpcraft.adbdesktop.feature.files.resources.files_create
import com.tlpcraft.adbdesktop.feature.files.resources.files_delete
import com.tlpcraft.adbdesktop.feature.files.resources.files_delete_message
import com.tlpcraft.adbdesktop.feature.files.resources.files_delete_title
import com.tlpcraft.adbdesktop.feature.files.resources.files_new_folder_invalid
import com.tlpcraft.adbdesktop.feature.files.resources.files_new_folder_label
import com.tlpcraft.adbdesktop.feature.files.resources.files_new_folder_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Dialog
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NewFolderDialog(
    form: NewFolderForm,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        title = stringResource(Res.string.files_new_folder_title),
        onDismissRequest = onDismiss,
        modifier = modifier,
        actions = {
            Button(text = stringResource(Res.string.files_cancel), onClick = onDismiss, variant = ButtonVariant.Ghost)
            Button(text = stringResource(Res.string.files_create), onClick = onSubmit, variant = ButtonVariant.Primary, enabled = form.canSubmit)
        },
    ) {
        TextField(
            value = form.name,
            onValueChange = onNameChange,
            label = stringResource(Res.string.files_new_folder_label),
            error = if (form.isInvalid) stringResource(Res.string.files_new_folder_invalid) else null,
            autoFocus = true,
            onSubmit = onSubmit,
        )
    }
}

@Composable
internal fun DeleteFileDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        title = stringResource(Res.string.files_delete_title, name),
        onDismissRequest = onDismiss,
        modifier = modifier,
        actions = {
            Button(text = stringResource(Res.string.files_cancel), onClick = onDismiss, variant = ButtonVariant.Ghost)
            Button(text = stringResource(Res.string.files_delete), onClick = onConfirm, variant = ButtonVariant.Danger)
        },
    ) {
        Text(stringResource(Res.string.files_delete_message), emphasis = TextEmphasis.Medium)
    }
}

@UiKitPreview
@Composable
private fun NewFolderDialogPreview() = PreviewContext {
    NewFolderDialog(form = NewFolderForm(name = "a/b", isInvalid = true), onNameChange = {}, onSubmit = {}, onDismiss = {})
}

@UiKitPreview
@Composable
private fun DeleteFileDialogPreview() = PreviewContext {
    DeleteFileDialog(name = "notes.txt", onConfirm = {}, onDismiss = {})
}
