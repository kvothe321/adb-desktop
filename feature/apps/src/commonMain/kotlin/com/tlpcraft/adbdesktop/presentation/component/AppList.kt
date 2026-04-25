package com.tlpcraft.adbdesktop.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.uikit.components.text.BodySmallText
import com.tlpcraft.adbdesktop.uikit.components.text.LabelSmallText
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

private val PanelWidth = 300.dp

@Composable
fun AppList(
    apps: List<AppInfo>,
    selectedApp: AppInfo?,
    searchQuery: String,
    activeFilter: AppFilter,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onAppSelected: (AppInfo) -> Unit,
    onFilterSelected: (AppFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(PanelWidth)
            .background(colorScheme.surfaceContainer),
    ) {
        SearchField(query = searchQuery, onQueryChanged = onSearchQueryChanged)
        FilterRow(activeFilter = activeFilter, onFilterSelected = onFilterSelected)
        HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))
        when {
            isLoading -> AppListLoadingState()
            errorMessage != null -> AppListErrorState(errorMessage)
            apps.isEmpty() -> AppListEmptyState()
            else -> AppItems(apps = apps, selectedApp = selectedApp, onAppSelected = onAppSelected)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.spacing.sm, vertical = dimensions.spacing.xs),
        placeholder = {
            BodySmallText(text = "Search packages…", color = colorScheme.onSurface.copy(alpha = 0.4f))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colorScheme.onSurface.copy(alpha = 0.5f),
            )
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall,
        shape = RoundedCornerShape(dimensions.radius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.15f),
        ),
    )
}

@Composable
private fun FilterRow(activeFilter: AppFilter, onFilterSelected: (AppFilter) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.spacing.sm, vertical = dimensions.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.xs),
    ) {
        items(AppFilter.entries) { filter ->
            FilterChip(
                selected = filter == activeFilter,
                onClick = { onFilterSelected(filter) },
                label = { LabelSmallText(filter.label) },
            )
        }
    }
}

@Composable
private fun AppItems(apps: List<AppInfo>, selectedApp: AppInfo?, onAppSelected: (AppInfo) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps, key = { it.packageName }) { app ->
            AppRow(
                app = app,
                isSelected = app == selectedApp,
                onClick = { onAppSelected(app) },
            )
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = dimensions.spacing.md, vertical = dimensions.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
    ) {
        AppIconBadge(packageName = app.packageName, size = 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            BodySmallText(
                text = app.displayName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary),
            )
        }
    }
}

@Composable
private fun AppListLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun AppListEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BodySmallText(text = "No packages found.", color = colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun AppListErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BodySmallText(text = message, color = colorScheme.error)
    }
}

@UiKitPreview
@Composable
private fun AppListPreview() = PreviewContext {
    AppList(
        apps = listOf(
            AppInfo("com.google.android.gms"),
            AppInfo("com.example.myapp"),
            AppInfo("com.tlpcraft.adbdesktop"),
            AppInfo("com.android.settings"),
        ),
        selectedApp = AppInfo("com.example.myapp"),
        searchQuery = "",
        activeFilter = AppFilter.ALL,
        onAppSelected = {},
        onFilterSelected = {},
        onSearchQueryChanged = {},
    )
}
