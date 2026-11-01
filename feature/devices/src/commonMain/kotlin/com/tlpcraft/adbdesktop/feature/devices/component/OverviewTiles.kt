package com.tlpcraft.adbdesktop.feature.devices.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.feature.devices.contract.QuickAction
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.action_apps
import com.tlpcraft.adbdesktop.feature.devices.resources.action_apps_detail
import com.tlpcraft.adbdesktop.feature.devices.resources.action_files
import com.tlpcraft.adbdesktop.feature.devices.resources.action_files_detail
import com.tlpcraft.adbdesktop.feature.devices.resources.action_install
import com.tlpcraft.adbdesktop.feature.devices.resources.action_install_detail
import com.tlpcraft.adbdesktop.feature.devices.resources.action_logcat
import com.tlpcraft.adbdesktop.feature.devices.resources.action_logcat_detail
import com.tlpcraft.adbdesktop.feature.devices.resources.action_screenshot
import com.tlpcraft.adbdesktop.feature.devices.resources.action_screenshot_detail
import com.tlpcraft.adbdesktop.feature.devices.resources.action_shell
import com.tlpcraft.adbdesktop.feature.devices.resources.action_shell_detail
import com.tlpcraft.adbdesktop.uikit.component.Card
import com.tlpcraft.adbdesktop.uikit.component.IconTile
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.ProgressStyle
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

/** One figure about the device, shown large, with a meter when it has a range. */
@Composable
internal fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    fraction: Float? = null,
    tone: Tone = Tone.Accent,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.heightIn(min = STAT_HEIGHT), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Text(label, variant = TextVariant.Label, emphasis = TextEmphasis.Medium, maxLines = 1)
            Text(value, variant = TextVariant.Display, maxLines = 1)
            if (fraction != null) ProgressIndicator(Modifier.fillMaxWidth(), progress = fraction, style = ProgressStyle.Linear, tone = tone)
        }
    }
}

@Composable
internal fun QuickActionTile(
    action: QuickAction,
    isRunning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth(), onClick = onClick.takeUnless { isRunning }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            if (isRunning) ProgressIndicator(Modifier.padding(AppTheme.spacing.sm)) else IconTile(action.icon, tone = Tone.Accent)
            Column(Modifier.weight(1f)) {
                Text(stringResource(action.label), variant = TextVariant.BodyStrong, maxLines = 1)
                Text(stringResource(action.detail), variant = TextVariant.Caption, emphasis = TextEmphasis.Medium, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier = modifier.padding(top = AppTheme.spacing.md), variant = TextVariant.Heading)
}

private val STAT_HEIGHT = 92.dp

private val QuickAction.icon: IconName
    get() = when (this) {
        QuickAction.Screenshot -> IconName.Camera
        QuickAction.InstallApk -> IconName.Upload
        QuickAction.Apps -> IconName.Package
        QuickAction.Files -> IconName.Folder
        QuickAction.Logcat -> IconName.DocumentText
        QuickAction.Shell -> IconName.Terminal
    }

private val QuickAction.label: StringResource
    get() = when (this) {
        QuickAction.Screenshot -> Res.string.action_screenshot
        QuickAction.InstallApk -> Res.string.action_install
        QuickAction.Apps -> Res.string.action_apps
        QuickAction.Files -> Res.string.action_files
        QuickAction.Logcat -> Res.string.action_logcat
        QuickAction.Shell -> Res.string.action_shell
    }

private val QuickAction.detail: StringResource
    get() = when (this) {
        QuickAction.Screenshot -> Res.string.action_screenshot_detail
        QuickAction.InstallApk -> Res.string.action_install_detail
        QuickAction.Apps -> Res.string.action_apps_detail
        QuickAction.Files -> Res.string.action_files_detail
        QuickAction.Logcat -> Res.string.action_logcat_detail
        QuickAction.Shell -> Res.string.action_shell_detail
    }

@UiKitPreview
@Composable
private fun OverviewTilesPreview() = PreviewContext {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        StatCard(label = "Battery", value = "87%", fraction = 0.87f, tone = Tone.Success)
        QuickActionTile(QuickAction.Screenshot, isRunning = false, onClick = {})
    }
}
