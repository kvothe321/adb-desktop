package com.tlpcraft.adbdesktop.component

import adb_desktop.composeapp.generated.resources.Res
import adb_desktop.composeapp.generated.resources.app_logo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.tlpcraft.adbdesktop.navigation.AppsRoute
import com.tlpcraft.adbdesktop.navigation.DevicesRoute
import com.tlpcraft.adbdesktop.navigation.FilesRoute
import com.tlpcraft.adbdesktop.uikit.components.icon.Box
import com.tlpcraft.adbdesktop.uikit.components.icon.Files
import com.tlpcraft.adbdesktop.uikit.components.icon.MonitorMobile
import com.tlpcraft.adbdesktop.uikit.components.icon.UiKitIcons
import com.tlpcraft.adbdesktop.uikit.components.navigation.NavigationDrawerItem
import com.tlpcraft.adbdesktop.uikit.components.text.TitleMediumText
import com.tlpcraft.adbdesktop.uikit.theme.dimensions
import org.jetbrains.compose.resources.painterResource

@Composable
fun SideDrawer(
    selectedSection: NavKey,
    isDark: Boolean,
    onSectionSelected: (NavKey) -> Unit,
    onToggleTheme: () -> Unit,
) {
    PermanentDrawerSheet(modifier = Modifier.width(220.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                modifier = Modifier.size(48.dp),
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(8.dp))
            TitleMediumText("ADB Desktop")

            Spacer(Modifier.height(dimensions.spacing.md))

            NavigationDrawerItem(
                label = "Devices",
                iconVector = UiKitIcons.MonitorMobile,
                selected = selectedSection is DevicesRoute,
                onClick = { onSectionSelected(DevicesRoute) },
            )
            NavigationDrawerItem(
                label = "Apps",
                iconVector = UiKitIcons.Box,
                selected = selectedSection is AppsRoute,
                onClick = { onSectionSelected(AppsRoute) },
            )
            NavigationDrawerItem(
                label = "Files",
                iconVector = UiKitIcons.Files,
                selected = selectedSection is FilesRoute,
                onClick = { onSectionSelected(FilesRoute) },
            )

            Spacer(Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
                IconButton(onClick = onToggleTheme) {
                    Text(
                        text = if (isDark) "☀" else "🌙",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
