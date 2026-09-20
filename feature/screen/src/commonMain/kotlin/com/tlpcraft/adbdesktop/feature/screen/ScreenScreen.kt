package com.tlpcraft.adbdesktop.feature.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.RecordingState
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenSideEffect
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenUiState
import com.tlpcraft.adbdesktop.feature.screen.contract.Screenshot
import com.tlpcraft.adbdesktop.feature.screen.resources.Res
import com.tlpcraft.adbdesktop.feature.screen.resources.message_recording_discarded
import com.tlpcraft.adbdesktop.feature.screen.resources.message_recording_saved
import com.tlpcraft.adbdesktop.feature.screen.resources.message_screen_failed
import com.tlpcraft.adbdesktop.feature.screen.resources.message_screenshot_copied
import com.tlpcraft.adbdesktop.feature.screen.resources.message_screenshot_saved
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_capture
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_copy
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_empty_description
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_empty_title
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_mirror
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_mirror_description
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_mirror_stop
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_no_device_description
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_no_device_title
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_preview_description
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_record
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_recording
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_save
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_saving
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_stop
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_stopping
import com.tlpcraft.adbdesktop.feature.screen.resources.screen_title
import com.tlpcraft.adbdesktop.uikit.component.Badge
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.PixelImage
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.component.VisibilityEffect
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SECONDS_PER_MINUTE = 60
private const val PREVIEW_FRAME_SIDE = 2
private const val PREVIEW_FRAME_BYTES = PREVIEW_FRAME_SIDE * PREVIEW_FRAME_SIDE * 4
private const val PREVIEW_FRAME_SHADE: Byte = 120

/** Live mirroring, screenshots and screen recordings of the selected device. One-shot results go through [onShowMessage]. */
@Composable
fun ScreenScreen(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: ScreenViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    VisibilityEffect { viewModel.reduceIntent(ScreenIntent.VisibilityChanged(it)) }

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect -> currentOnShowMessage(effect.message()) }
    }

    ScreenContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun ScreenContent(uiState: ScreenUiState, onIntent: (ScreenIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.screen_title)) {
            if (uiState.serial != null) ScreenActions(uiState = uiState, onIntent = onIntent)
        }
        Box(Modifier.weight(1f).fillMaxSize().padding(AppTheme.spacing.lg), contentAlignment = Alignment.Center) {
            when {
                uiState.serial == null -> EmptyState(
                    title = stringResource(Res.string.screen_no_device_title),
                    description = stringResource(Res.string.screen_no_device_description),
                    icon = IconName.Camera,
                )

                uiState.mirror is MirrorState.Live -> PixelImage(
                    width = uiState.mirror.frame.width,
                    height = uiState.mirror.frame.height,
                    pixels = uiState.mirror.frame.pixels,
                    contentDescription = stringResource(Res.string.screen_mirror_description),
                    modifier = Modifier.fillMaxSize(),
                )

                uiState.mirror == MirrorState.Starting || uiState.isCapturing -> ProgressIndicator()

                uiState.screenshot != null -> ScreenshotPreview(uiState.screenshot)

                else -> EmptyState(
                    title = stringResource(Res.string.screen_empty_title),
                    description = stringResource(Res.string.screen_empty_description),
                    icon = IconName.Camera,
                )
            }
        }
    }
}

@Composable
private fun RowScope.ScreenActions(uiState: ScreenUiState, onIntent: (ScreenIntent) -> Unit) {
    uiState.recording.statusText(uiState.recordingSeconds)?.let { status -> Badge(status, tone = Tone.Error) }
    Button(
        text = stringResource(if (uiState.isMirrorOn) Res.string.screen_mirror_stop else Res.string.screen_mirror),
        onClick = { onIntent(ScreenIntent.MirrorClicked) },
        icon = if (uiState.isMirrorOn) IconName.Stop else IconName.Mirror,
    )
    if (uiState.recording == RecordingState.Recording) {
        Button(text = stringResource(Res.string.screen_stop), onClick = {
            onIntent(ScreenIntent.StopClicked)
        }, variant = ButtonVariant.Danger, icon = IconName.Stop)
    } else {
        Button(
            text = stringResource(Res.string.screen_record),
            onClick = { onIntent(ScreenIntent.RecordClicked) },
            icon = IconName.Play,
            enabled = uiState.canRecord,
        )
    }
    if (uiState.screenshot != null) {
        Button(text = stringResource(Res.string.screen_copy), onClick = { onIntent(ScreenIntent.CopyScreenshotClicked) }, variant = ButtonVariant.Ghost)
        Button(text = stringResource(Res.string.screen_save), onClick = { onIntent(ScreenIntent.SaveScreenshotClicked) }, icon = IconName.Download)
    }
    Button(
        text = stringResource(Res.string.screen_capture),
        onClick = { onIntent(ScreenIntent.CaptureClicked) },
        variant = ButtonVariant.Primary,
        icon = IconName.Camera,
        enabled = uiState.canCapture,
    )
}

@Composable
private fun RecordingState.statusText(elapsedSeconds: Int): String? = when (this) {
    RecordingState.Idle -> null
    RecordingState.Recording -> stringResource(Res.string.screen_recording, elapsedSeconds.toClockTime())
    RecordingState.Stopping -> stringResource(Res.string.screen_stopping)
    RecordingState.Saving -> stringResource(Res.string.screen_saving)
}

@Composable
private fun ScreenshotPreview(screenshot: Screenshot) {
    val bitmap = remember(screenshot) { screenshot.png.decodeToImageBitmap() }
    Image(bitmap, contentDescription = stringResource(Res.string.screen_preview_description), modifier = Modifier.fillMaxSize())
}

private fun Int.toClockTime(): String = "${(this / SECONDS_PER_MINUTE).toString().padStart(2, '0')}:${(this % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"

private suspend fun ScreenSideEffect.message(): String = when (this) {
    is ScreenSideEffect.ScreenshotSaved -> getString(Res.string.message_screenshot_saved, path)
    ScreenSideEffect.ScreenshotCopied -> getString(Res.string.message_screenshot_copied)
    is ScreenSideEffect.RecordingSaved -> getString(Res.string.message_recording_saved, path)
    ScreenSideEffect.RecordingDiscarded -> getString(Res.string.message_recording_discarded)
    is ScreenSideEffect.Failed -> getString(Res.string.message_screen_failed, details)
}

@UiKitPreview
@Composable
private fun ScreenContentEmptyPreview() = PreviewContext {
    ScreenContent(uiState = ScreenUiState(serial = "emulator-5554"), onIntent = {})
}

@UiKitPreview
@Composable
private fun ScreenContentMirrorPreview() = PreviewContext {
    val frame = MirrorFrame(PREVIEW_FRAME_SIDE, PREVIEW_FRAME_SIDE, pixels = ByteArray(PREVIEW_FRAME_BYTES) { PREVIEW_FRAME_SHADE })
    ScreenContent(uiState = ScreenUiState(serial = "emulator-5554", isMirrorOn = true, mirror = MirrorState.Live(frame)), onIntent = {})
}

@UiKitPreview
@Composable
private fun ScreenContentRecordingPreview() = PreviewContext {
    ScreenContent(uiState = ScreenUiState(serial = "emulator-5554", recording = RecordingState.Recording), onIntent = {})
}

@UiKitPreview
@Composable
private fun ScreenContentNoDevicePreview() = PreviewContext {
    ScreenContent(uiState = ScreenUiState(), onIntent = {})
}
