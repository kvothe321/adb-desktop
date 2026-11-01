package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** The colors of a terminal. Their shades come from the theme. */
enum class TerminalColor {
    Default,
    Black,
    Red,
    Green,
    Yellow,
    Blue,
    Magenta,
    Cyan,
    White,
    BrightBlack,
    BrightRed,
    BrightGreen,
    BrightYellow,
    BrightBlue,
    BrightMagenta,
    BrightCyan,
    BrightWhite,
}

/** A piece of a [TerminalRow] with one style. */
@Immutable
data class TerminalSpan(
    val text: String,
    val foreground: TerminalColor = TerminalColor.Default,
    val background: TerminalColor = TerminalColor.Default,
    val isBold: Boolean = false,
    val isDim: Boolean = false,
    val isInverted: Boolean = false,
    val isUnderlined: Boolean = false,
)

/** One line of a terminal. [id] stays the same while the line scrolls. */
@Immutable
data class TerminalRow(val id: Long, val spans: List<TerminalSpan>) {
    val text: String
        get() = spans.joinToString("") { it.text }
}

/**
 * A character grid that fills its space and scrolls to its last row, unless the user scrolled back. Text can be
 * selected. [onGridSizeChange] reports how many rows and columns fit, and [onKeyPress] gets every key while focused,
 * returning whether it was used. `modifier` is for layout only.
 */
@Composable
fun TerminalGrid(
    rows: List<TerminalRow>,
    onGridSizeChange: (rows: Int, columns: Int) -> Unit,
    onKeyPress: (KeyEvent) -> Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val style = AppTheme.typography.mono.copy(color = AppTheme.colors.textPrimary)
    val measurer = rememberTextMeasurer()
    val cell = remember(style, measurer) { measurer.measure("M", style).size }
    val focusRequester = remember { FocusRequester() }
    val currentOnGridSizeChange by rememberUpdatedState(onGridSizeChange)
    var isFollowing by remember { mutableStateOf(true) }

    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.canScrollForward }.collect { (isScrolling, canScrollForward) ->
            if (isScrolling) isFollowing = !canScrollForward
        }
    }
    LaunchedEffect(rows, isFollowing) {
        if (isFollowing && rows.isNotEmpty()) listState.scrollToItem(rows.lastIndex)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
            .pointerInput(focusRequester) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    focusRequester.requestFocus()
                }
            }
            .onPreviewKeyEvent { event ->
                onKeyPress(event).also { isUsed -> if (isUsed) isFollowing = true }
            }
            .focusRequester(focusRequester)
            .focusable()
            .padding(AppTheme.spacing.md),
    ) {
        val density = LocalDensity.current
        val rowCount = with(density) { maxHeight.toPx() }.toInt() / cell.height
        val columnCount = with(density) { maxWidth.toPx() }.toInt() / cell.width
        LaunchedEffect(rowCount, columnCount) {
            if (rowCount > 0 && columnCount > 0) currentOnGridSizeChange(rowCount, columnCount)
        }

        SelectionContainer {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(rows, key = { it.id }) { row ->
                    BasicText(text = row.toAnnotatedString(), style = style, softWrap = false, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun TerminalRow.toAnnotatedString(): AnnotatedString {
    val foreground = AppTheme.colors.textPrimary
    val background = AppTheme.colors.surface
    val palette = AppTheme.colors.terminalPalette
    return remember(this, foreground, background, palette) {
        buildAnnotatedString {
            if (spans.isEmpty()) append(" ")
            spans.forEachIndexed { index, span ->
                withStyle(span.toSpanStyle(foreground, background, palette)) {
                    append(if (index == spans.lastIndex) span.paintedText() else span.text)
                }
            }
        }
    }
}

/** Text layout leaves spaces at the end of a line unpainted, which would hide a cursor sitting there. */
internal fun TerminalSpan.paintedText(): String {
    if (!isInverted && background == TerminalColor.Default) return text
    val content = text.trimEnd(' ')
    return content + NON_BREAKING_SPACE.repeat(text.length - content.length)
}

private fun TerminalSpan.toSpanStyle(defaultForeground: Color, defaultBackground: Color, palette: List<Color>): SpanStyle {
    val text = foreground.resolve(palette) ?: defaultForeground
    val fill = background.resolve(palette)
    return SpanStyle(
        color = (if (isInverted) fill ?: defaultBackground else text).let { if (isDim) it.copy(alpha = DIM_ALPHA) else it },
        background = (if (isInverted) text else fill) ?: Color.Unspecified,
        fontWeight = if (isBold) FontWeight.Bold else null,
        textDecoration = if (isUnderlined) TextDecoration.Underline else null,
    )
}

/** The palette lists every color after [TerminalColor.Default], in order. */
private fun TerminalColor.resolve(palette: List<Color>): Color? = palette.getOrNull(ordinal - 1)

private const val DIM_ALPHA = 0.6f
private const val NON_BREAKING_SPACE = "\u00A0"

@UiKitPreview
@Composable
private fun TerminalGridPreview() = PreviewContext {
    TerminalGrid(
        rows = listOf(
            listOf(TerminalSpan("pixel:/ $ ", foreground = TerminalColor.Green), TerminalSpan("ls")),
            listOf(TerminalSpan("Download", foreground = TerminalColor.BrightBlue, isBold = true)),
            emptyList(),
            listOf(TerminalSpan(" PID USER ", isInverted = true)),
        ).mapIndexed { index, spans -> TerminalRow(index.toLong(), spans) },
        onGridSizeChange = { _, _ -> },
        onKeyPress = { false },
    )
}
