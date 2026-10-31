package com.tlpcraft.adbdesktop.feature.devices.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesFailure
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_invalid_description
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_invalid_title
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_missing_description
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_missing_title
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_path_label
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_path_placeholder
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_adb_path_save
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_retry
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_unavailable_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.MonospaceText
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Explains why adb cannot be used and, when it is a path problem, lets the user fix it in place. */
@Composable
internal fun AdbSetupPanel(
    failure: DevicesFailure,
    adbPathInput: String,
    onAdbPathChange: (String) -> Unit,
    onAdbPathSubmit: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (failure) {
        is DevicesFailure.Unavailable -> EmptyState(
            title = stringResource(Res.string.devices_unavailable_title),
            description = failure.details,
            icon = IconName.Warning,
            tone = Tone.Error,
            modifier = modifier,
            action = { Button(text = stringResource(Res.string.devices_retry), onClick = onRetry, icon = IconName.Refresh) },
        )

        is DevicesFailure.AdbNotFound -> AdbPathForm(
            title = stringResource(Res.string.devices_adb_missing_title),
            description = stringResource(Res.string.devices_adb_missing_description),
            searchedLocations = failure.searchedLocations,
            adbPathInput = adbPathInput,
            onAdbPathChange = onAdbPathChange,
            onAdbPathSubmit = onAdbPathSubmit,
            modifier = modifier,
        )

        is DevicesFailure.InvalidAdbPath -> AdbPathForm(
            title = stringResource(Res.string.devices_adb_invalid_title),
            description = stringResource(Res.string.devices_adb_invalid_description, failure.path),
            searchedLocations = emptyList(),
            adbPathInput = adbPathInput,
            onAdbPathChange = onAdbPathChange,
            onAdbPathSubmit = onAdbPathSubmit,
            modifier = modifier,
        )
    }
}

@Composable
private fun AdbPathForm(
    title: String,
    description: String,
    searchedLocations: List<String>,
    adbPathInput: String,
    onAdbPathChange: (String) -> Unit,
    onAdbPathSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(title = title, description = description, icon = IconName.Warning, tone = Tone.Warning, modifier = modifier) {
        Column(
            Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            searchedLocations.forEach { MonospaceText(it, emphasis = TextEmphasis.Disabled, maxLines = 1) }
            TextField(
                value = adbPathInput,
                onValueChange = onAdbPathChange,
                label = stringResource(Res.string.devices_adb_path_label),
                placeholder = stringResource(Res.string.devices_adb_path_placeholder),
                monospace = true,
                onSubmit = onAdbPathSubmit,
            )
            Button(text = stringResource(Res.string.devices_adb_path_save), onClick = onAdbPathSubmit, variant = ButtonVariant.Primary)
        }
    }
}

@UiKitPreview
@Composable
private fun AdbSetupPanelMissingPreview() = PreviewContext {
    AdbSetupPanel(
        failure = DevicesFailure.AdbNotFound(listOf("/usr/local/bin/adb", "/usr/bin/adb")),
        adbPathInput = "",
        onAdbPathChange = {},
        onAdbPathSubmit = {},
        onRetry = {},
    )
}

@UiKitPreview
@Composable
private fun AdbSetupPanelUnavailablePreview() = PreviewContext {
    AdbSetupPanel(
        failure = DevicesFailure.Unavailable("'track-devices -l' exited with 1: cannot connect to daemon"),
        adbPathInput = "",
        onAdbPathChange = {},
        onAdbPathSubmit = {},
        onRetry = {},
    )
}
