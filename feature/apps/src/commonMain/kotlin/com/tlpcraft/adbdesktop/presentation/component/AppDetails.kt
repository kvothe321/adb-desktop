package com.tlpcraft.adbdesktop.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.AppDetails
import com.tlpcraft.adbdesktop.presentation.AppDetailsState
import com.tlpcraft.adbdesktop.uikit.components.text.BodyMediumText
import com.tlpcraft.adbdesktop.uikit.components.text.BodySmallText
import com.tlpcraft.adbdesktop.uikit.components.text.LabelSmallText
import com.tlpcraft.adbdesktop.uikit.components.text.TitleMediumText
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

private val AppIconDetailSize = 56.dp
private val DetailLabelWidth = 120.dp

/** Callbacks for the action buttons in the details panel. */
data class AppActions(
    val onOpen: () -> Unit = {},
    val onForceStop: () -> Unit = {},
    val onClearCache: () -> Unit = {},
    val onClearData: () -> Unit = {},
    val onUninstall: () -> Unit = {},
)

@Composable
fun AppDetails(appDetailsState: AppDetailsState, actions: AppActions = AppActions()) {
    when (appDetailsState) {
        is AppDetailsState.Empty -> AppDetailsEmptyState()
        is AppDetailsState.Loading -> AppDetailsLoadingState()
        is AppDetailsState.Error -> AppDetailsErrorState(appDetailsState.message)
        is AppDetailsState.Ready -> AppDetailsContent(appDetailsState.details, actions)
    }
}

@Composable
private fun AppDetailsEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs),
        ) {
            BodyMediumText(text = "Select an app", color = colorScheme.onSurface.copy(alpha = 0.4f))
            BodySmallText(
                text = "Pick a package from the list to view its details.",
                color = colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun AppDetailsLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun AppDetailsErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(dimensions.spacing.lg), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs),
        ) {
            BodyMediumText(text = "Failed to load details", color = colorScheme.error)
            BodySmallText(text = message, color = colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun AppDetailsContent(details: AppDetails, actions: AppActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.spacing.lg),
    ) {
        AppHeader(details)
        Spacer(Modifier.height(dimensions.spacing.lg))
        ActionsSection(actions)
        Spacer(Modifier.height(dimensions.spacing.lg))
        DetailsSection(details)
    }
}

@Composable
private fun AppHeader(details: AppDetails) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.md),
    ) {
        AppIconBadge(packageName = details.packageName, size = AppIconDetailSize)
        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
            TitleMediumText(text = details.displayName(), fontWeight = FontWeight.SemiBold)
            SelectionContainer {
                BodySmallText(
                    text = details.packageName,
                    color = colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun ActionsSection(actions: AppActions) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.sm)) {
        SectionHeader(title = "Actions")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
        ) {
            ActionButton(label = "Open App", onClick = actions.onOpen)
            ActionButton(label = "Force Stop", onClick = actions.onForceStop)
            ActionButton(label = "Clear Cache", onClick = actions.onClearCache)
            ActionButton(label = "Clear Data", isDestructive = true, onClick = actions.onClearData)
            ActionButton(label = "Uninstall", isDestructive = true, onClick = actions.onUninstall)
        }
    }
}

@Composable
private fun DetailsSection(details: AppDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
        SectionHeader(title = "Information")
        Spacer(Modifier.height(dimensions.spacing.xs))
        HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(Modifier.height(dimensions.spacing.xs))
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
                DetailRow("Package", details.packageName)
                DetailRow("Version", details.versionName ?: "—")
                DetailRow("Version Code", details.versionCode?.toString() ?: "—")
                DetailRow("Target SDK", details.targetSdk?.toString() ?: "—")
                DetailRow("Min SDK", details.minSdk?.toString() ?: "—")
                DetailRow("Installed", details.firstInstallTime ?: "—")
                DetailRow("Updated", details.lastUpdateTime ?: "—")
                DetailRow("APK Size", details.codeSize?.toHumanReadable() ?: "—")
                DetailRow("Data Size", details.dataSize?.toHumanReadable() ?: "—")
                DetailRow("Cache Size", details.cacheSize?.toHumanReadable() ?: "—")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    LabelSmallText(text = title.uppercase(), color = colorScheme.onSurface.copy(alpha = 0.5f))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.spacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        BodySmallText(
            text = label,
            modifier = Modifier.width(DetailLabelWidth),
            color = colorScheme.onSurface.copy(alpha = 0.6f),
        )
        BodySmallText(text = value)
    }
}

@Composable
private fun ActionButton(label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    val contentColor = if (isDestructive) colorScheme.error else colorScheme.onSurface
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = BorderStroke(
            width = dimensions.stroke.thin,
            color = contentColor.copy(alpha = 0.35f),
        ),
    ) {
        LabelSmallText(label, color = contentColor)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun AppDetails.displayName(): String = packageName.substringAfterLast('.').replaceFirstChar { it.uppercaseChar() }

private fun Long.toHumanReadable(): String = when {
    this < 1_024L -> "$this B"
    this < 1_024L * 1_024 -> "${this / 1_024} KB"
    this < 1_024L * 1_024 * 1_024 -> "${this / (1_024L * 1_024)} MB"
    else -> "${this / (1_024L * 1_024 * 1_024)} GB"
}

// ── Previews ──────────────────────────────────────────────────────────────────

@UiKitPreview
@Composable
private fun AppDetailsEmptyPreview() = PreviewContext {
    AppDetails(appDetailsState = AppDetailsState.Empty)
}

@UiKitPreview
@Composable
private fun AppDetailsLoadingPreview() = PreviewContext {
    AppDetails(appDetailsState = AppDetailsState.Loading)
}

@UiKitPreview
@Composable
private fun AppDetailsReadyPreview() = PreviewContext {
    AppDetails(
        appDetailsState = AppDetailsState.Ready(
            details = AppDetails(
                packageName = "com.example.myapp",
                versionName = "2.4.1",
                versionCode = 241,
                targetSdk = 34,
                minSdk = 26,
                firstInstallTime = "2023-03-12 09:15:00",
                lastUpdateTime = "2024-11-20 14:30:22",
                codeSize = 41_943_040L,
                dataSize = 12_582_912L,
                cacheSize = 2_097_152L,
            ),
        ),
    )
}
