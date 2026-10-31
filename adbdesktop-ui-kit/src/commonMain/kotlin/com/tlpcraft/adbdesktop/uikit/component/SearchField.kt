package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.resources.Res
import com.tlpcraft.adbdesktop.uikit.resources.search_clear
import org.jetbrains.compose.resources.stringResource

/** A [TextField] for filtering lists, with a clear button once there is a query. `modifier` is for layout only. */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        leadingIcon = IconName.Search,
        modifier = modifier,
        trailing = if (query.isEmpty()) {
            null
        } else {
            { ClearButton(onClick = { onQueryChange("") }) }
        },
    )
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    IconButton(IconName.Close, contentDescription = stringResource(Res.string.search_clear), onClick = onClick, size = ControlSize.Small)
}

@UiKitPreview
@Composable
private fun SearchFieldPreview() = PreviewContext {
    SearchField(query = "com.google", onQueryChange = {}, placeholder = "Search packages")
}
