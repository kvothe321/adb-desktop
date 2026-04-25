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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.AppInfo
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

@Composable
fun AppDetails(app: AppInfo?) {
    if (app == null) {
        AppDetailsEmptyState()
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensions.spacing.lg),
    ) {
        AppHeader(app)
        Spacer(Modifier.height(dimensions.spacing.lg))
        ActionsSection()
        Spacer(Modifier.height(dimensions.spacing.lg))
        DetailsSection(app)
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
private fun AppHeader(app: AppInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.md),
    ) {
        AppIconBadge(packageName = app.packageName, size = AppIconDetailSize)
        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
            TitleMediumText(text = app.displayName(), fontWeight = FontWeight.SemiBold)
            SelectionContainer {
                BodySmallText(
                    text = app.packageName,
                    color = colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun ActionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.sm)) {
        SectionHeader(title = "Actions")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
        ) {
            ActionButton(label = "Open App", onClick = {})
            ActionButton(label = "Force Stop", onClick = {})
            ActionButton(label = "Clear Cache", onClick = {})
            ActionButton(label = "Clear Data", isDestructive = true, onClick = {})
            ActionButton(label = "Uninstall", isDestructive = true, onClick = {})
        }
    }
}

@Composable
private fun DetailsSection(app: AppInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
        SectionHeader(title = "Information")
        Spacer(Modifier.height(dimensions.spacing.xs))
        HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
        Spacer(Modifier.height(dimensions.spacing.xs))
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacing.xs)) {
                DetailRow(label = "Package", value = app.packageName)
                DetailRow(label = "Version", value = "—")
                DetailRow(label = "Version Code", value = "—")
                DetailRow(label = "Target SDK", value = "—")
                DetailRow(label = "Min SDK", value = "—")
                DetailRow(label = "Install Date", value = "—")
                DetailRow(label = "App Size", value = "—")
                DetailRow(label = "Data Size", value = "—")
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

@UiKitPreview
@Composable
private fun AppDetailsEmptyPreview() = PreviewContext {
    AppDetails(app = null)
}

@UiKitPreview
@Composable
private fun AppDetailsPreview() = PreviewContext {
    AppDetails(app = AppInfo("com.example.myapp"))
}
