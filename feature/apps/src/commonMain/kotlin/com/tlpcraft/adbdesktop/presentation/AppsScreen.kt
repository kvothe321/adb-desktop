package com.tlpcraft.adbdesktop.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppsScreen(selectedDevice: AdbDevice?, viewModel: AppsViewModel = koinViewModel()) {
    LaunchedEffect(selectedDevice) { viewModel.updateSelectedDevice(selectedDevice) }
    val uiState by viewModel.uiState.collectAsState()
    AppsScreenContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
    )
}

@Composable
fun AppsScreenContent(uiState: AppsUiState, onFilterSelected: (AppFilter) -> Unit = {}, onSearchQueryChanged: (String) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is AppsUiState.Loading -> FullscreenMessage(
                primary = "Scanning for devices…",
                showSpinner = true,
            )

            is AppsUiState.NoDevice -> FullscreenMessage(
                primary = "No device selected.",
                secondary = "Go to Devices and select an online device to browse its apps.",
            )

            is AppsUiState.FetchingApps -> {
                FilterBar(activeFilter = uiState.activeFilter, onFilterSelected = onFilterSelected)
                HorizontalDivider()
                FullscreenMessage(
                    primary = "Loading packages…",
                    secondary = uiState.deviceSerial,
                    showSpinner = true,
                )
            }

            is AppsUiState.Error -> {
                FilterBar(activeFilter = uiState.activeFilter, onFilterSelected = onFilterSelected)
                HorizontalDivider()
                FullscreenMessage(
                    primary = "Failed to load packages",
                    secondary = uiState.message,
                    isError = true,
                )
            }

            is AppsUiState.Content -> {
                FilterBar(activeFilter = uiState.activeFilter, onFilterSelected = onFilterSelected)
                HorizontalDivider()
                SearchBar(
                    query = uiState.searchQuery,
                    resultCount = uiState.filteredApps.size,
                    totalCount = uiState.apps.size,
                    onQueryChanged = onSearchQueryChanged,
                )
                HorizontalDivider()
                AppList(apps = uiState.filteredApps)
            }
        }
    }
}

// ── Filter bar ────────────────────────────────────────────────────────────────

@Composable
private fun FilterBar(activeFilter: AppFilter, onFilterSelected: (AppFilter) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == activeFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    resultCount: Int,
    totalCount: Int,
    onQueryChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search packages…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
        )
        Text(
            text = if (query.isBlank()) "$totalCount packages" else "$resultCount / $totalCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── App list ──────────────────────────────────────────────────────────────────

@Composable
private fun AppList(apps: List<AppInfo>) {
    if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No packages match your search.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps, key = { it.packageName }) { app ->
            AppRow(app)
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
private fun AppRow(app: AppInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun FullscreenMessage(
    primary: String,
    secondary: String? = null,
    showSpinner: Boolean = false,
    isError: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showSpinner) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
        }
        Text(
            text = primary,
            style = MaterialTheme.typography.titleMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        if (secondary != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@UiKitPreview
@Composable
private fun AppsScreenPreview() = PreviewContext {
    AppsScreenContent(uiState = AppsUiState.NoDevice)
}

@UiKitPreview
@Composable
private fun AppsScreenLoadingPreview() = PreviewContext {
    AppsScreenContent(
        uiState = AppsUiState.FetchingApps(
            deviceSerial = "emulator-5554",
            activeFilter = AppFilter.ALL,
        ),
    )
}

@UiKitPreview
@Composable
private fun AppsScreenContentPreview() = PreviewContext {
    AppsScreenContent(
        uiState = AppsUiState.Content(
            deviceSerial = "emulator-5554",
            activeFilter = AppFilter.USER,
            apps = listOf(
                AppInfo("com.example.myapp"),
                AppInfo("com.example.another"),
                AppInfo("com.tlpcraft.adbdesktop"),
            ),
            searchQuery = "",
        ),
    )
}
