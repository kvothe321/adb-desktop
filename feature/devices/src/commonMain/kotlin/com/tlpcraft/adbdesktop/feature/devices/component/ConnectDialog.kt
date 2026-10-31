package com.tlpcraft.adbdesktop.feature.devices.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormError
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormState
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectMode
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_address_label
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_address_placeholder
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_cancel
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_error_address
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_error_code
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_hint_connect
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_hint_pair
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_mode_connect
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_mode_pair
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_pair_submit
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_pairing_address_label
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_pairing_address_placeholder
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_pairing_code_label
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_pairing_code_placeholder
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_submit
import com.tlpcraft.adbdesktop.feature.devices.resources.connect_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Chip
import com.tlpcraft.adbdesktop.uikit.component.Dialog
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConnectDialog(
    form: ConnectFormState,
    onModeSelect: (ConnectMode) -> Unit,
    onAddressChange: (String) -> Unit,
    onPairingCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPairing = form.mode == ConnectMode.Pair
    Dialog(
        title = stringResource(Res.string.connect_title),
        onDismissRequest = onDismiss,
        modifier = modifier,
        actions = {
            Button(text = stringResource(Res.string.connect_cancel), onClick = onDismiss, variant = ButtonVariant.Ghost)
            Button(
                text = stringResource(if (isPairing) Res.string.connect_pair_submit else Res.string.connect_submit),
                onClick = onSubmit,
                variant = ButtonVariant.Primary,
                enabled = form.canSubmit,
                loading = form.inProgress,
            )
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Chip(stringResource(Res.string.connect_mode_connect), selected = !isPairing, onClick = { onModeSelect(ConnectMode.Connect) })
            Chip(stringResource(Res.string.connect_mode_pair), selected = isPairing, onClick = { onModeSelect(ConnectMode.Pair) })
        }
        Text(stringResource(if (isPairing) Res.string.connect_hint_pair else Res.string.connect_hint_connect), emphasis = TextEmphasis.Medium)
        TextField(
            value = form.address,
            onValueChange = onAddressChange,
            label = stringResource(if (isPairing) Res.string.connect_pairing_address_label else Res.string.connect_address_label),
            placeholder = stringResource(if (isPairing) Res.string.connect_pairing_address_placeholder else Res.string.connect_address_placeholder),
            error = (form.error as? ConnectFormError.InvalidAddress)?.let { stringResource(Res.string.connect_error_address) },
            monospace = true,
            onSubmit = onSubmit,
        )
        if (isPairing) {
            TextField(
                value = form.pairingCode,
                onValueChange = onPairingCodeChange,
                label = stringResource(Res.string.connect_pairing_code_label),
                placeholder = stringResource(Res.string.connect_pairing_code_placeholder),
                error = (form.error as? ConnectFormError.InvalidPairingCode)?.let { stringResource(Res.string.connect_error_code) },
                monospace = true,
                onSubmit = onSubmit,
            )
        }
        (form.error as? ConnectFormError.Failed)?.let { Text(it.reason, variant = TextVariant.Caption, tone = Tone.Error) }
    }
}

@UiKitPreview
@Composable
private fun ConnectDialogConnectPreview() = PreviewContext {
    ConnectDialog(
        form = ConnectFormState(address = "192.168.1.42", error = ConnectFormError.Failed("Connection refused")),
        onModeSelect = {},
        onAddressChange = {},
        onPairingCodeChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}

@UiKitPreview
@Composable
private fun ConnectDialogPairPreview() = PreviewContext {
    ConnectDialog(
        form = ConnectFormState(mode = ConnectMode.Pair, address = "192.168.1.42:37123", pairingCode = "12", error = ConnectFormError.InvalidPairingCode),
        onModeSelect = {},
        onAddressChange = {},
        onPairingCodeChange = {},
        onSubmit = {},
        onDismiss = {},
    )
}
