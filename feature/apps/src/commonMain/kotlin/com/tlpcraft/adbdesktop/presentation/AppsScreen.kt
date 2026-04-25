package com.tlpcraft.adbdesktop.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.presentation.component.AppActions
import com.tlpcraft.adbdesktop.presentation.component.AppDetails
import com.tlpcraft.adbdesktop.presentation.component.AppList
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppsScreen(selectedDevice: AdbDevice?, viewModel: AppsViewModel = koinViewModel()) {
    LaunchedEffect(selectedDevice) { viewModel.updateSelectedDevice(selectedDevice) }
    val uiState by viewModel.uiState.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val appDetailsState by viewModel.appDetailsState.collectAsState()
    AppsScreenContent(
        uiState = uiState,
        selectedApp = selectedApp,
        appDetailsState = appDetailsState,
        actions = AppActions(
            onOpen = { viewModel.executeAction(AppAction.OPEN) },
            onForceStop = { viewModel.executeAction(AppAction.FORCE_STOP) },
            onClearCache = { viewModel.executeAction(AppAction.CLEAR_CACHE) },
            onClearData = { viewModel.executeAction(AppAction.CLEAR_DATA) },
            onUninstall = { viewModel.executeAction(AppAction.UNINSTALL) },
        ),
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onAppSelected = viewModel::selectApp,
    )
}

@Composable
fun AppsScreenContent(
    uiState: AppsUiState,
    selectedApp: AppInfo? = null,
    appDetailsState: AppDetailsState = AppDetailsState.Empty,
    actions: AppActions = AppActions(),
    onFilterSelected: (AppFilter) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onAppSelected: (AppInfo?) -> Unit = {},
) {
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

            is AppsUiState.FetchingApps -> AppsLayout(
                filteredApps = emptyList(),
                selectedApp = selectedApp,
                appDetailsState = appDetailsState,
                actions = actions,
                searchQuery = "",
                activeFilter = uiState.activeFilter,
                isLoading = true,
                onAppSelected = onAppSelected,
                onFilterSelected = onFilterSelected,
                onSearchQueryChanged = {},
            )

            is AppsUiState.Error -> AppsLayout(
                filteredApps = emptyList(),
                selectedApp = selectedApp,
                appDetailsState = appDetailsState,
                actions = actions,
                searchQuery = "",
                activeFilter = uiState.activeFilter,
                listErrorMessage = uiState.message,
                onAppSelected = onAppSelected,
                onFilterSelected = onFilterSelected,
                onSearchQueryChanged = {},
            )

            is AppsUiState.Content -> AppsLayout(
                filteredApps = uiState.filteredApps,
                selectedApp = selectedApp,
                appDetailsState = appDetailsState,
                actions = actions,
                searchQuery = uiState.searchQuery,
                activeFilter = uiState.activeFilter,
                onAppSelected = onAppSelected,
                onFilterSelected = onFilterSelected,
                onSearchQueryChanged = onSearchQueryChanged,
            )
        }
    }
}

@Composable
private fun AppsLayout(
    filteredApps: List<AppInfo>,
    selectedApp: AppInfo?,
    appDetailsState: AppDetailsState,
    actions: AppActions,
    searchQuery: String,
    activeFilter: AppFilter,
    isLoading: Boolean = false,
    listErrorMessage: String? = null,
    onAppSelected: (AppInfo?) -> Unit,
    onFilterSelected: (AppFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppList(
            apps = filteredApps,
            selectedApp = selectedApp,
            searchQuery = searchQuery,
            activeFilter = activeFilter,
            isLoading = isLoading,
            errorMessage = listErrorMessage,
            onAppSelected = onAppSelected,
            onFilterSelected = onFilterSelected,
            onSearchQueryChanged = onSearchQueryChanged,
        )
        VerticalDivider()
        AppDetails(
            appDetailsState = appDetailsState,
            actions = actions,
        )
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
private fun AppsScreenNoDevicePreview() = PreviewContext {
    AppsScreenContent(uiState = AppsUiState.NoDevice)
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
                AppInfo("com.google.android.apps.maps"),
                AppInfo("com.spotify.music"),
            ),
            searchQuery = "",
        ),
        selectedApp = AppInfo("com.example.myapp"),
        appDetailsState = AppDetailsState.Empty,
    )
}
