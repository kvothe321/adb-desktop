package com.tlpcraft.adbdesktop.feature.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.feature.files.resources.Res
import com.tlpcraft.adbdesktop.feature.files.resources.files_title
import com.tlpcraft.adbdesktop.feature.files.resources.files_unavailable_description
import com.tlpcraft.adbdesktop.feature.files.resources.files_unavailable_title
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.stringResource

/** Stateless until the file explorer lands (docs/ROADMAP.md). */
@Composable
fun FilesScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.files_title))
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = stringResource(Res.string.files_unavailable_title),
                description = stringResource(Res.string.files_unavailable_description),
                icon = IconName.Folder,
            )
        }
    }
}

@UiKitPreview
@Composable
private fun FilesScreenPreview() = PreviewContext {
    FilesScreen()
}
