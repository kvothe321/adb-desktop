package com.tlpcraft.adbdesktop.feature.files.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.feature.files.contract.TransferState
import com.tlpcraft.adbdesktop.feature.files.resources.Res
import com.tlpcraft.adbdesktop.feature.files.resources.files_transfer_batch
import com.tlpcraft.adbdesktop.feature.files.resources.files_transfer_cancel
import com.tlpcraft.adbdesktop.uikit.component.ControlSize
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.ProgressStyle
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val PERCENT = 100

@Composable
internal fun TransferBar(transfer: TransferState, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Text(transfer.label(), variant = TextVariant.Caption, emphasis = TextEmphasis.Medium, maxLines = 1)
            ProgressIndicator(Modifier.fillMaxWidth(), progress = transfer.fraction, style = ProgressStyle.Linear)
        }
        IconButton(IconName.Close, contentDescription = stringResource(Res.string.files_transfer_cancel), onClick = onCancel, size = ControlSize.Small)
    }
}

@Composable
private fun TransferState.label(): String {
    val name = if (count > 1) stringResource(Res.string.files_transfer_batch, this.name, position, count) else this.name
    return fraction?.let { "$name  ${(it * PERCENT).toInt()}%" } ?: name
}

@UiKitPreview
@Composable
private fun TransferBarPreview() = PreviewContext {
    TransferBar(transfer = TransferState("big.bin", fraction = 0.4f, position = 1, count = 3), onCancel = {})
}

@UiKitPreview
@Composable
private fun TransferBarFolderPreview() = PreviewContext {
    TransferBar(transfer = TransferState("Pictures", fraction = null, position = 1, count = 1), onCancel = {})
}
