package com.tlpcraft.adbdesktop.feature.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.feature.apps.component.AppDetailsPane
import com.tlpcraft.adbdesktop.feature.apps.component.AppListPane
import com.tlpcraft.adbdesktop.feature.apps.component.ConfirmActionDialog
import com.tlpcraft.adbdesktop.feature.apps.component.label
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsIntent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsSideEffect
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsUiState
import com.tlpcraft.adbdesktop.feature.apps.resources.Res
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_no_device_description
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_no_device_title
import com.tlpcraft.adbdesktop.feature.apps.resources.apps_title
import com.tlpcraft.adbdesktop.feature.apps.resources.message_action_failed
import com.tlpcraft.adbdesktop.feature.apps.resources.message_action_succeeded
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.SplitPane
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Installed packages of the selected device. One-shot results are reported through [onShowMessage]. */
@Composable
fun AppsScreen(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: AppsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect -> currentOnShowMessage(effect.message()) }
    }

    AppsContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun AppsContent(uiState: AppsUiState, onIntent: (AppsIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.apps_title))
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.list == AppListState.NoDevice) {
                EmptyState(
                    title = stringResource(Res.string.apps_no_device_title),
                    description = stringResource(Res.string.apps_no_device_description),
                    icon = IconName.Devices,
                )
            } else {
                SplitPane(
                    start = {
                        AppListPane(
                            list = uiState.list,
                            visiblePackages = uiState.visiblePackages,
                            filter = uiState.filter,
                            query = uiState.query,
                            selectedPackage = uiState.selectedPackage,
                            onFilterSelect = { onIntent(AppsIntent.FilterSelected(it)) },
                            onQueryChange = { onIntent(AppsIntent.QueryChanged(it)) },
                            onAppClick = { onIntent(AppsIntent.AppClicked(it)) },
                            onRetry = { onIntent(AppsIntent.RetryClicked) },
                        )
                    },
                    end = {
                        AppDetailsPane(
                            details = uiState.details,
                            runningAction = uiState.runningAction,
                            onActionClick = { onIntent(AppsIntent.ActionClicked(it)) },
                        )
                    },
                )
            }
        }
        val pendingAction = uiState.pendingConfirmation
        val selectedPackage = uiState.selectedPackage
        if (pendingAction != null && selectedPackage != null) {
            ConfirmActionDialog(
                action = pendingAction,
                packageName = selectedPackage,
                onConfirm = { onIntent(AppsIntent.ActionConfirmed) },
                onDismiss = { onIntent(AppsIntent.ActionCancelled) },
            )
        }
    }
}

private suspend fun AppsSideEffect.message(): String = when (this) {
    is AppsSideEffect.ActionSucceeded -> getString(Res.string.message_action_succeeded, getString(action.label), packageName)
    is AppsSideEffect.ActionFailed -> getString(Res.string.message_action_failed, getString(action.label), packageName)
}

@UiKitPreview
@Composable
private fun AppsContentNoDevicePreview() = PreviewContext {
    AppsContent(uiState = AppsUiState(), onIntent = {})
}

@UiKitPreview
@Composable
private fun AppsContentLoadedPreview() = PreviewContext {
    AppsContent(
        uiState = AppsUiState(
            serial = "R5CT10ABCDE",
            list = AppListState.Loaded(listOf("com.android.settings", "com.example.notes")),
            selectedPackage = "com.example.notes",
        ),
        onIntent = {},
    )
}
