package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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

/** A piece of a [TerminalRow] with one style. A [isWide] span is one character drawn two columns wide. */
@Immutable
data class TerminalSpan(
    val text: String,
    val foreground: TerminalColor = TerminalColor.Default,
    val background: TerminalColor = TerminalColor.Default,
    val isBold: Boolean = false,
    val isDim: Boolean = false,
    val isInverted: Boolean = false,
    val isUnderlined: Boolean = false,
    val isWide: Boolean = false,
)

/** One line of a terminal. [id] stays the same while the line scrolls. */
@Immutable
data class TerminalRow(val id: Long, val spans: List<TerminalSpan>) {
    val text: String
        get() = spans.joinToString("") { it.text }
}

/**
 * A character grid that fills its space and scrolls to its last row, unless the user scrolled back. Text can be
 * selected. [onGridSizeChange] reports how many rows and columns fit. While focused, [onKeyPress] gets every key first,
 * returning whether it used it; typed text the keys did not produce, including what an input method composes, arrives
 * through [onTextInput]. `modifier` is for layout only.
 */
@Composable
fun TerminalGrid(
    rows: List<TerminalRow>,
    onGridSizeChange: (rows: Int, columns: Int) -> Unit,
    onKeyPress: (KeyEvent) -> Boolean,
    onTextInput: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val style = AppTheme.typography.mono.copy(color = AppTheme.colors.textPrimary)
    val measurer = rememberTextMeasurer()
    val cell = remember(style, measurer) { measurer.measure("M", style).size }
    val columnWidth = remember(style, measurer) { measurer.measure(MEASURED_COLUMNS, style).size.width.toFloat() / MEASURED_COLUMNS.length }
    val focusRequester = remember { FocusRequester() }
    val currentOnGridSizeChange by rememberUpdatedState(onGridSizeChange)
    var isFollowing by remember { mutableStateOf(true) }
    var isComposing by remember { mutableStateOf(false) }

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
                // Text selection takes focus on press, so typing gets it back once the press ends.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    waitForUpOrCancellation(pass = PointerEventPass.Final)
                    focusRequester.requestFocus()
                }
            }
            .onPreviewKeyEvent { event ->
                !isComposing && onKeyPress(event).also { isUsed -> if (isUsed) isFollowing = true }
            }
            .padding(AppTheme.spacing.md),
    ) {
        val density = LocalDensity.current
        val rowCount = with(density) { maxHeight.toPx() }.toInt() / cell.height
        val columnCount = with(density) { maxWidth.toPx() }.toInt() / cell.width
        LaunchedEffect(rowCount, columnCount) {
            if (rowCount > 0 && columnCount > 0) currentOnGridSizeChange(rowCount, columnCount)
        }

        val wideSize = with(density) { DpSize((columnWidth * 2).toDp(), cell.height.toDp()) }
        SelectionContainer {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(rows, key = { it.id }) { row ->
                    val line = row.toTerminalLine(style, wideSize)
                    BasicText(text = line.text, style = style, softWrap = false, maxLines = 1, inlineContent = line.wideCharacters)
                }
            }
        }
        TextInput(
            focusRequester = focusRequester,
            onTextInput = { text ->
                isFollowing = true
                onTextInput(text)
            },
            onComposingChange = { isComposing = it },
            style = style,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

/**
 * Where typed text arrives, so input methods and dead keys work. It stays out of sight until an input method composes,
 * then shows the text being composed.
 */
@Composable
private fun TextInput(
    focusRequester: FocusRequester,
    onTextInput: (String) -> Unit,
    onComposingChange: (Boolean) -> Unit,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(TextFieldValue()) }
    val isComposing = value.composition != null

    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

    BasicTextField(
        value = value,
        onValueChange = { next ->
            if (next.composition == null) {
                if (next.text.isNotEmpty()) onTextInput(next.text)
                value = TextFieldValue()
            } else {
                value = next
            }
            onComposingChange(next.composition != null)
        },
        modifier = modifier.focusRequester(focusRequester).alpha(if (isComposing) 1f else 0f),
        textStyle = style,
        singleLine = true,
        cursorBrush = SolidColor(AppTheme.colors.accent),
        decorationBox = { field ->
            Box(
                Modifier
                    .background(AppTheme.colors.surfaceRaised, AppTheme.shapes.small)
                    .border(1.dp, AppTheme.colors.accent, AppTheme.shapes.small)
                    .padding(horizontal = AppTheme.spacing.xs),
            ) {
                field()
            }
        },
    )
}

private class TerminalLine(val text: AnnotatedString, val wideCharacters: Map<String, InlineTextContent>)

/** Wide characters become placeholders exactly two columns wide, so the columns after them stay in line whatever the font. */
@Composable
private fun TerminalRow.toTerminalLine(style: TextStyle, wideSize: DpSize): TerminalLine {
    val foreground = AppTheme.colors.textPrimary
    val background = AppTheme.colors.surface
    val palette = AppTheme.colors.terminalPalette
    val density = LocalDensity.current
    return remember(this, foreground, background, palette, style, wideSize, density) {
        val wideCharacters = mutableMapOf<String, InlineTextContent>()
        val text = buildAnnotatedString {
            if (spans.isEmpty()) append(" ")
            spans.forEachIndexed { index, span ->
                val spanStyle = span.toSpanStyle(foreground, background, palette)
                if (span.isWide) {
                    val id = "$WIDE_ID_PREFIX$index"
                    appendInlineContent(id, span.text)
                    wideCharacters[id] =
                        wideCharacter(span.text, style.merge(spanStyle.copy(background = Color.Unspecified)), spanStyle.background, wideSize, density)
                } else {
                    withStyle(spanStyle) { append(if (index == spans.lastIndex) span.paintedText() else span.text) }
                }
            }
        }
        TerminalLine(text, wideCharacters)
    }
}

private fun wideCharacter(
    text: String,
    style: TextStyle,
    fill: Color,
    size: DpSize,
    density: Density,
): InlineTextContent {
    val placeholder = with(density) { Placeholder(size.width.toSp(), size.height.toSp(), PlaceholderVerticalAlign.TextCenter) }
    return InlineTextContent(placeholder) {
        Box(Modifier.fillMaxSize().background(fill), contentAlignment = Alignment.Center) {
            BasicText(text, style = style, softWrap = false, maxLines = 1)
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
private const val MEASURED_COLUMNS = "MMMMMMMMMMMMMMMMMMMM"
private const val WIDE_ID_PREFIX = "wide"
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
            listOf(TerminalSpan("日", isWide = true), TerminalSpan("本", isWide = true), TerminalSpan("|")),
        ).mapIndexed { index, spans -> TerminalRow(index.toLong(), spans) },
        onGridSizeChange = { _, _ -> },
        onKeyPress = { false },
        onTextInput = {},
    )
}
