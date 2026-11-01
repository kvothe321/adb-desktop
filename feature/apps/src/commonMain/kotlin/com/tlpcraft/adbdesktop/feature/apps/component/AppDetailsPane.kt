package com.tlpcraft.adbdesktop.feature.apps.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.isDestructive
import com.tlpcraft.adbdesktop.feature.apps.resources.Res
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_apk_size
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_cache_size
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_data_size
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_installed
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_min_sdk
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_target_sdk
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_unknown
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_updated
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_version
import com.tlpcraft.adbdesktop.feature.apps.resources.app_detail_version_code
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_details_empty_description
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_details_empty_title
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_details_failed
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.ControlSize
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.Icon
import com.tlpcraft.adbdesktop.uikit.component.IconSize
import com.tlpcraft.adbdesktop.uikit.component.MonospaceText
import com.tlpcraft.adbdesktop.uikit.component.PictureIcon
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppDetailsPane(
    details: AppDetailsState,
    appearance: AppAppearance?,
    runningAction: AppAction?,
    onActionClick: (AppAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (details) {
            AppDetailsState.Hidden -> EmptyState(
                title = stringResource(Res.string.apps_details_empty_title),
                description = stringResource(Res.string.apps_details_empty_description),
                icon = IconName.Package,
            )

            AppDetailsState.Loading -> ProgressIndicator()

            is AppDetailsState.Failed -> EmptyState(title = stringResource(Res.string.apps_details_failed), description = details.details, tone = Tone.Error)

            is AppDetailsState.Loaded -> AppDetailsBody(details.details, appearance, runningAction, onActionClick)
        }
    }
}

@Composable
private fun AppDetailsBody(
    details: AppDetails,
    appearance: AppAppearance?,
    runningAction: AppAction?,
    onActionClick: (AppAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            PictureIcon(image = appearance?.icon, fallback = IconName.Package, contentDescription = null, size = IconSize.Hero, fallbackTone = Tone.Accent)
            Column {
                Text(details.packageName.appTitle(appearance), variant = TextVariant.Title)
                MonospaceText(details.packageName, emphasis = TextEmphasis.Medium, selectable = true)
            }
        }
        ActionRow(runningAction, onActionClick)
        Divider()
        DetailTable(details)
    }
}

@Composable
private fun ActionRow(runningAction: AppAction?, onActionClick: (AppAction) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        AppAction.entries.forEach { action ->
            Button(
                text = stringResource(action.label),
                onClick = { onActionClick(action) },
                variant = if (action.isDestructive) ButtonVariant.Danger else ButtonVariant.Secondary,
                size = ControlSize.Small,
                enabled = runningAction == null,
                loading = runningAction == action,
            )
        }
    }
}

@Composable
private fun DetailTable(details: AppDetails) {
    val unknown = stringResource(Res.string.app_detail_unknown)
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        DetailRow(Res.string.app_detail_version, details.versionName ?: unknown)
        DetailRow(Res.string.app_detail_version_code, details.versionCode?.toString() ?: unknown)
        DetailRow(Res.string.app_detail_target_sdk, details.targetSdk?.toString() ?: unknown)
        DetailRow(Res.string.app_detail_min_sdk, details.minSdk?.toString() ?: unknown)
        DetailRow(Res.string.app_detail_installed, details.firstInstallTime ?: unknown)
        DetailRow(Res.string.app_detail_updated, details.lastUpdateTime ?: unknown)
        DetailRow(Res.string.app_detail_apk_size, details.codeSizeBytes?.toReadableSize() ?: unknown)
        DetailRow(Res.string.app_detail_data_size, details.dataSizeBytes?.toReadableSize() ?: unknown)
        DetailRow(Res.string.app_detail_cache_size, details.cacheSizeBytes?.toReadableSize() ?: unknown)
    }
}

@Composable
private fun DetailRow(label: StringResource, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(stringResource(label), modifier = Modifier.width(140.dp), emphasis = TextEmphasis.Medium)
        MonospaceText(value, selectable = true)
    }
}

@UiKitPreview
@Composable
private fun AppDetailsPaneLoadedPreview() = PreviewContext {
    AppDetailsPane(
        details = AppDetailsState.Loaded(
            AppDetails(
                packageName = "com.example.notes",
                versionName = "2.4.1",
                versionCode = 241,
                targetSdk = 34,
                minSdk = 26,
                firstInstallTime = "2023-03-12 09:15:00",
                lastUpdateTime = "2024-11-20 14:30:22",
                codeSizeBytes = 42_991_616,
                dataSizeBytes = 12_582_912,
                cacheSizeBytes = null,
            ),
        ),
        appearance = null,
        runningAction = AppAction.FORCE_STOP,
        onActionClick = {},
    )
}

@UiKitPreview
@Composable
private fun AppDetailsPaneHiddenPreview() = PreviewContext {
    AppDetailsPane(details = AppDetailsState.Hidden, appearance = null, runningAction = null, onActionClick = {})
}
