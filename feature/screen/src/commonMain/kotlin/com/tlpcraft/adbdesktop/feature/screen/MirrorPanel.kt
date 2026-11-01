package com.tlpcraft.adbdesktop.feature.screen

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction
import com.tlpcraft.adbdesktop.feature.screen.contract.DeviceButton
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorSideEffect
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorUiState
import com.tlpcraft.adbdesktop.feature.screen.mirror.MirrorKeyPress
import com.tlpcraft.adbdesktop.feature.screen.mirror.MirrorKeyResult
import com.tlpcraft.adbdesktop.feature.screen.mirror.framePointAt
import com.tlpcraft.adbdesktop.feature.screen.mirror.toMirrorKey
import com.tlpcraft.adbdesktop.feature.screen.resources.Res
import com.tlpcraft.adbdesktop.feature.screen.resources.message_screen_failed
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_back
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_description
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_home
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_no_device_description
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_no_device_title
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_off_description
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_off_title
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_pin
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_power
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_recents
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_rotate
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_start
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_stop
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_title
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_unpin
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_volume_down
import com.tlpcraft.adbdesktop.feature.screen.resources.mirror_volume_up
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ControlSize
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.PixelImage
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.component.VisibilityEffect
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PREVIEW_FRAME_WIDTH = 9
private const val PREVIEW_FRAME_HEIGHT = 20
private const val PREVIEW_FRAME_BYTES = PREVIEW_FRAME_WIDTH * PREVIEW_FRAME_HEIGHT * 4
private const val PREVIEW_FRAME_SHADE: Byte = 120

/** The live device screen, usable with mouse and keyboard. Failures go through [onShowMessage]. */
@Composable
fun MirrorPanel(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: MirrorViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    VisibilityEffect { viewModel.reduceIntent(MirrorIntent.VisibilityChanged(it)) }

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is MirrorSideEffect.Failed -> currentOnShowMessage(getString(Res.string.message_screen_failed, effect.details))
            }
        }
    }

    MirrorContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun MirrorContent(uiState: MirrorUiState, onIntent: (MirrorIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.mirror_title)) {
            if (uiState.serial != null) {
                Button(
                    text = stringResource(if (uiState.isOn) Res.string.mirror_stop else Res.string.mirror_start),
                    onClick = { onIntent(MirrorIntent.MirrorClicked) },
                    icon = if (uiState.isOn) IconName.Stop else IconName.Mirror,
                )
            }
            IconButton(
                icon = IconName.Pin,
                contentDescription = stringResource(if (uiState.isPinned) Res.string.mirror_unpin else Res.string.mirror_pin),
                onClick = { onIntent(MirrorIntent.PinClicked) },
                selected = uiState.isPinned,
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(AppTheme.spacing.md), contentAlignment = Alignment.Center) {
            when {
                uiState.serial == null -> EmptyState(
                    title = stringResource(Res.string.mirror_no_device_title),
                    description = stringResource(Res.string.mirror_no_device_description),
                    icon = IconName.Mirror,
                )

                uiState.mirror is MirrorState.Live -> MirrorSurface(frame = uiState.mirror.frame, onIntent = onIntent)

                uiState.mirror == MirrorState.Starting -> ProgressIndicator()

                else -> EmptyState(
                    title = stringResource(Res.string.mirror_off_title),
                    description = stringResource(Res.string.mirror_off_description),
                    icon = IconName.Mirror,
                )
            }
        }
        if (uiState.mirror is MirrorState.Live) {
            Divider()
            DeviceButtons(onClick = { onIntent(MirrorIntent.DeviceButtonClicked(it)) })
        }
    }
}

@Composable
private fun DeviceButtons(onClick: (DeviceButton) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs, Alignment.CenterHorizontally),
    ) {
        DeviceButton.entries.forEach { button ->
            IconButton(
                icon = button.icon,
                contentDescription = stringResource(button.label),
                onClick = { onClick(button) },
                size = ControlSize.Small,
            )
        }
    }
}

private val DeviceButton.icon: IconName
    get() = when (this) {
        DeviceButton.Back -> IconName.Back
        DeviceButton.Home -> IconName.Home
        DeviceButton.Recents -> IconName.Recents
        DeviceButton.VolumeDown -> IconName.VolumeDown
        DeviceButton.VolumeUp -> IconName.VolumeUp
        DeviceButton.Power -> IconName.Lock
        DeviceButton.Rotate -> IconName.Rotate
    }

private val DeviceButton.label: StringResource
    get() = when (this) {
        DeviceButton.Back -> Res.string.mirror_back
        DeviceButton.Home -> Res.string.mirror_home
        DeviceButton.Recents -> Res.string.mirror_recents
        DeviceButton.VolumeDown -> Res.string.mirror_volume_down
        DeviceButton.VolumeUp -> Res.string.mirror_volume_up
        DeviceButton.Power -> Res.string.mirror_power
        DeviceButton.Rotate -> Res.string.mirror_rotate
    }

/** Clicks and drags become touches, the wheel scrolls, right click is back and middle click is home, as in scrcpy. */
@Composable
private fun MirrorSurface(frame: MirrorFrame, onIntent: (MirrorIntent) -> Unit, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    val currentFrame by rememberUpdatedState(frame)
    val currentOnIntent by rememberUpdatedState(onIntent)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(focusRequester) {
                awaitPointerEventScope {
                    var isTouching = false
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) focusRequester.requestFocus()
                        isTouching = handlePointer(event, size, currentFrame, isTouching) { currentOnIntent(it) }
                    }
                }
            }
            .onPreviewKeyEvent { event -> handleKey(event) { currentOnIntent(it) } }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        PixelImage(
            width = frame.width,
            height = frame.height,
            pixels = frame.pixels,
            contentDescription = stringResource(Res.string.mirror_description),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Sends what [event] means on the device and returns whether a finger is still down afterwards. */
private fun handlePointer(
    event: PointerEvent,
    boxSize: IntSize,
    frame: MirrorFrame,
    isTouching: Boolean,
    onIntent: (MirrorIntent) -> Unit,
): Boolean {
    val change = event.changes.firstOrNull() ?: return isTouching
    val pointer = PointerOnFrame(change.position, boxSize, frame) { onIntent(MirrorIntent.InputGiven(it)) }

    return when (event.type) {
        PointerEventType.Press -> when {
            event.buttons.isSecondaryPressed -> isTouching.also { pointer.send(MirrorInput.BackOrScreenOn) }
            event.buttons.isTertiaryPressed -> isTouching.also { onIntent(MirrorIntent.DeviceButtonClicked(DeviceButton.Home)) }
            else -> isTouching || pointer.touch(TouchAction.Down, isClamped = false)
        }

        PointerEventType.Move -> isTouching.also { if (it) pointer.touch(TouchAction.Move, isClamped = true) }

        PointerEventType.Release -> (isTouching && event.buttons.isPrimaryPressed).also { isStillDown ->
            if (isTouching && !isStillDown) pointer.touch(TouchAction.Up, isClamped = true)
        }

        PointerEventType.Scroll -> isTouching.also { pointer.scroll(horizontal = change.scrollDelta.x, vertical = -change.scrollDelta.y) }

        else -> isTouching
    }
}

private class PointerOnFrame(val position: Offset, val boxSize: IntSize, val frame: MirrorFrame, val send: (MirrorInput) -> Unit) {
    private fun pointAt(isClamped: Boolean) = framePointAt(position, boxSize, frame.width, frame.height, isClamped)

    /** Returns whether the touch landed on the frame. */
    fun touch(action: TouchAction, isClamped: Boolean): Boolean {
        val point = pointAt(isClamped) ?: return false
        send(MirrorInput.Touch(action, point))
        return true
    }

    fun scroll(horizontal: Float, vertical: Float) {
        pointAt(isClamped = false)?.let { send(MirrorInput.Scroll(it, horizontal, vertical)) }
    }
}

private fun handleKey(event: KeyEvent, onIntent: (MirrorIntent) -> Unit): Boolean {
    if (event.type != KeyEventType.KeyDown && event.type != KeyEventType.KeyUp) return false
    val press = MirrorKeyPress(
        key = event.key,
        codePoint = event.utf16CodePoint,
        isDown = event.type == KeyEventType.KeyDown,
        isCtrl = event.isCtrlPressed,
        isAlt = event.isAltPressed,
        isShift = event.isShiftPressed,
        isMeta = event.isMetaPressed,
    )
    return when (val result = press.toMirrorKey()) {
        is MirrorKeyResult.Send -> true.also { onIntent(MirrorIntent.InputGiven(result.input)) }
        MirrorKeyResult.Paste -> true.also { onIntent(MirrorIntent.PasteRequested) }
        MirrorKeyResult.Ignored -> false
    }
}

@UiKitPreview
@Composable
private fun MirrorContentLivePreview() = PreviewContext {
    val frame = MirrorFrame(PREVIEW_FRAME_WIDTH, PREVIEW_FRAME_HEIGHT, pixels = ByteArray(PREVIEW_FRAME_BYTES) { PREVIEW_FRAME_SHADE })
    MirrorContent(uiState = MirrorUiState(serial = "emulator-5554", isOn = true, mirror = MirrorState.Live(frame)), onIntent = {})
}

@UiKitPreview
@Composable
private fun MirrorContentOffPreview() = PreviewContext {
    MirrorContent(uiState = MirrorUiState(serial = "emulator-5554", isPinned = true), onIntent = {})
}

@UiKitPreview
@Composable
private fun MirrorContentNoDevicePreview() = PreviewContext {
    MirrorContent(uiState = MirrorUiState(), onIntent = {})
}
