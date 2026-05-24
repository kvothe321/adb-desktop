package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** A hairline separator that fills its parent's width or height. `modifier` is for layout only. */
@Composable
fun Divider(modifier: Modifier = Modifier, orientation: DividerOrientation = DividerOrientation.Horizontal) {
    val size = when (orientation) {
        DividerOrientation.Horizontal -> Modifier.fillMaxWidth().height(1.dp)
        DividerOrientation.Vertical -> Modifier.fillMaxHeight().width(1.dp)
    }
    Box(modifier.then(size).background(AppTheme.colors.border))
}

@UiKitPreview
@Composable
private fun DividerPreview() = PreviewContext {
    Column {
        Text("Above")
        Divider()
        Text("Below")
    }
}
