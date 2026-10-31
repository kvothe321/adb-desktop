package com.tlpcraft.adbdesktop.feature.apps.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.feature.apps.resources.Res
import com.tlpcraft.adbdesktop.feature.apps.resources.app_confirm_cancel
import com.tlpcraft.adbdesktop.feature.apps.resources.app_confirm_clear_data_message
import com.tlpcraft.adbdesktop.feature.apps.resources.app_confirm_clear_data_title
import com.tlpcraft.adbdesktop.feature.apps.resources.app_confirm_uninstall_message
import com.tlpcraft.adbdesktop.feature.apps.resources.app_confirm_uninstall_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Dialog
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConfirmActionDialog(
    action: AppAction,
    packageName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUninstall = action == AppAction.UNINSTALL
    Dialog(
        title = stringResource(if (isUninstall) Res.string.app_confirm_uninstall_title else Res.string.app_confirm_clear_data_title),
        onDismissRequest = onDismiss,
        modifier = modifier,
        actions = {
            Button(text = stringResource(Res.string.app_confirm_cancel), onClick = onDismiss, variant = ButtonVariant.Ghost)
            Button(text = stringResource(action.label), onClick = onConfirm, variant = ButtonVariant.Danger)
        },
    ) {
        val message = if (isUninstall) Res.string.app_confirm_uninstall_message else Res.string.app_confirm_clear_data_message
        Text(stringResource(message, packageName), emphasis = TextEmphasis.Medium)
    }
}

@UiKitPreview
@Composable
private fun ConfirmActionDialogPreview() = PreviewContext {
    ConfirmActionDialog(action = AppAction.UNINSTALL, packageName = "com.example.notes", onConfirm = {}, onDismiss = {})
}
