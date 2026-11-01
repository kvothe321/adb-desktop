package com.tlpcraft.adbdesktop.feature.apps.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.resources.Res
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_empty
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_load_failed
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_search_placeholder
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.Chip
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.Icon
import com.tlpcraft.adbdesktop.uikit.component.IconSize
import com.tlpcraft.adbdesktop.uikit.component.ListRow
import com.tlpcraft.adbdesktop.uikit.component.PictureIcon
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.SearchField
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppListPane(
    list: AppListState,
    visiblePackages: List<String>,
    filter: AppFilter,
    query: String,
    selectedPackage: String?,
    appearances: Map<String, AppAppearance>,
    onFilterSelect: (AppFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            SearchField(query = query, onQueryChange = onQueryChange, placeholder = stringResource(Res.string.apps_search_placeholder))
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                AppFilter.entries.forEach { option ->
                    Chip(stringResource(option.label), selected = option == filter, onClick = { onFilterSelect(option) })
                }
            }
        }
        Divider()
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (list) {
                AppListState.NoDevice -> Unit

                AppListState.Loading -> ProgressIndicator()

                is AppListState.Failed -> EmptyState(
                    title = stringResource(Res.string.apps_load_failed),
                    description = list.details,
                    tone = Tone.Error,
                )

                is AppListState.Loaded -> if (visiblePackages.isEmpty()) {
                    EmptyState(title = stringResource(Res.string.apps_empty), icon = IconName.Search)
                } else {
                    PackageList(packages = visiblePackages, selectedPackage = selectedPackage, appearances = appearances, onAppClick = onAppClick)
                }
            }
        }
    }
}

@Composable
private fun PackageList(
    packages: List<String>,
    selectedPackage: String?,
    appearances: Map<String, AppAppearance>,
    onAppClick: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(packages, key = { it }) { packageName ->
            ListRow(
                headline = packageName.appTitle(appearances[packageName]),
                supporting = packageName,
                monospaceSupporting = true,
                selected = packageName == selectedPackage,
                onClick = { onAppClick(packageName) },
                leading = {
                    PictureIcon(image = appearances[packageName]?.icon, fallback = IconName.Package, contentDescription = null, size = IconSize.Large)
                },
            )
        }
    }
}

@UiKitPreview
@Composable
private fun AppListPaneLoadedPreview() = PreviewContext {
    val packages = listOf("com.android.settings", "com.example.notes", "com.google.android.gms")
    AppListPane(
        list = AppListState.Loaded(packages),
        visiblePackages = packages,
        filter = AppFilter.USER,
        query = "",
        selectedPackage = "com.example.notes",
        appearances = emptyMap(),
        onFilterSelect = {},
        onQueryChange = {},
        onAppClick = {},
    )
}

@UiKitPreview
@Composable
private fun AppListPaneFailedPreview() = PreviewContext {
    AppListPane(
        list = AppListState.Failed("Device not found: R5CT10ABCDE"),
        visiblePackages = emptyList(),
        filter = AppFilter.ALL,
        query = "",
        selectedPackage = null,
        appearances = emptyMap(),
        onFilterSelect = {},
        onQueryChange = {},
        onAppClick = {},
    )
}
