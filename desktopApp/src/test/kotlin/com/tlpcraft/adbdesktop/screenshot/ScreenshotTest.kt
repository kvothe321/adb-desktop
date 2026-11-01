package com.tlpcraft.adbdesktop.screenshot

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.feature.apps.AppsContent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsUiState
import com.tlpcraft.adbdesktop.feature.devices.DevicesContent
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesUiState
import com.tlpcraft.adbdesktop.feature.files.FilesContent
import com.tlpcraft.adbdesktop.feature.files.contract.FileListing
import com.tlpcraft.adbdesktop.feature.files.contract.FilesUiState
import com.tlpcraft.adbdesktop.feature.logcat.LogcatContent
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogRow
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatUiState
import com.tlpcraft.adbdesktop.feature.screen.MirrorContent
import com.tlpcraft.adbdesktop.feature.screen.ScreenContent
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorUiState
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenUiState
import com.tlpcraft.adbdesktop.feature.settings.SettingsContent
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsUiState
import com.tlpcraft.adbdesktop.feature.shell.ShellContent
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.shell.AppCommandPalette
import com.tlpcraft.adbdesktop.shell.AppShellContent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import com.tlpcraft.adbdesktop.uikit.component.TerminalColor
import com.tlpcraft.adbdesktop.uikit.component.TerminalRow
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan
import com.tlpcraft.adbdesktop.uikit.component.rememberMessageHostState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import java.io.File
import javax.imageio.ImageIO
import kotlin.reflect.KClass

private val outputDirectory: String? = System.getenv("SCREENSHOT_DIR")

class ScreenshotsRequested : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = outputDirectory != null
}

/** Renders every section in the window chrome, in both themes and at several window sizes. Runs only with SCREENSHOT_DIR set. */
@OptIn(ExperimentalTestApi::class)
@EnabledIf(ScreenshotsRequested::class)
class ScreenshotTest : FunSpec({
    val sizes = mapOf("wide" to DpSize(1440.dp, 900.dp), "medium" to DpSize(1100.dp, 760.dp), "narrow" to DpSize(760.dp, 640.dp))

    Section.entries.forEach { section ->
        sizes.forEach { (sizeName, size) ->
            listOf(false, true).forEach { isDark ->
                val name = "${section.name.lowercase()}-$sizeName-${if (isDark) "dark" else "light"}"
                test(name) {
                    runDesktopComposeUiTest(width = size.width.value.toInt(), height = size.height.value.toInt()) {
                        setContent {
                            AppTheme(darkTheme = isDark) {
                                AppShellContent(
                                    uiState = shellState(),
                                    currentSection = section,
                                    isDarkTheme = isDark,
                                    messageHostState = rememberMessageHostState(),
                                    onIntent = {},
                                    onSectionSelect = {},
                                    onPaletteOpen = {},
                                    modifier = Modifier.size(size),
                                    phone = { Mirror(isLive = section != Section.Apps) },
                                ) {
                                    SectionContent(section)
                                }
                            }
                        }
                        File(checkNotNull(outputDirectory)).mkdirs()
                        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(outputDirectory, "$name.png"))
                    }
                }
            }
        }
    }

    listOf(false, true).forEach { isDark ->
        val name = "palette-${if (isDark) "dark" else "light"}"
        test(name) {
            runDesktopComposeUiTest(width = 1100, height = 760) {
                setContent {
                    AppTheme(darkTheme = isDark) {
                        AppCommandPalette(uiState = shellState(), isDarkTheme = isDark, onSectionSelect = {}, onIntent = {}, onDismiss = {})
                    }
                }
                File(checkNotNull(outputDirectory)).mkdirs()
                val roots = onAllNodes(isRoot())
                ImageIO.write(roots[roots.fetchSemanticsNodes().lastIndex].captureToImage().toAwtImage(), "png", File(outputDirectory, "$name.png"))
            }
        }
    }
})

private fun shellState() = AppShellUiState(
    devices = listOf(DeviceOption("R5CWC46JFLH", "Galaxy S24+"), DeviceOption("emulator-5554", "Pixel 9 Pro")),
    selectedSerial = "R5CWC46JFLH",
)

@Composable
private fun Mirror(isLive: Boolean) {
    val width = 90
    val height = 200
    val pixels = ByteArray(width * height * 4) { index ->
        val pixel = index / 4
        val y = pixel / width
        when (index % 4) {
            0 -> (120 + y / 2).toByte()
            1 -> (60 + y / 4).toByte()
            2 -> 40.toByte()
            else -> -1
        }
    }
    val frame = MirrorFrame(width, height, pixels)
    val state = if (isLive) MirrorUiState(serial = "R5CWC46JFLH", isOn = true, mirror = MirrorState.Live(frame)) else MirrorUiState(serial = "R5CWC46JFLH")
    MirrorContent(uiState = state, onIntent = {})
}

@Composable
private fun SectionContent(section: Section) {
    when (section) {
        Section.Devices -> DevicesContent(
            uiState = DevicesUiState(
                list = DeviceListState.Loaded(
                    listOf(
                        DeviceItem("R5CWC46JFLH", "Galaxy S24+", DeviceState.ONLINE, ConnectionType.USB),
                        DeviceItem("192.168.1.42:5555", "Pixel 8", DeviceState.ONLINE, ConnectionType.WIRELESS),
                        DeviceItem("emulator-5554", "Pixel 9 Pro", DeviceState.ONLINE, ConnectionType.EMULATOR),
                        DeviceItem("emulator-5556", null, DeviceState.OFFLINE, ConnectionType.EMULATOR),
                    ),
                ),
                stats = mapOf(
                    "R5CWC46JFLH" to DeviceStats("17", batteryLevel = 87, cpuUsagePercent = 23f),
                    "192.168.1.42:5555" to DeviceStats("15", batteryLevel = 41, cpuUsagePercent = 8f),
                    "emulator-5554" to DeviceStats("16", batteryLevel = 100, cpuUsagePercent = 61f),
                ),
                selectedSerial = "R5CWC46JFLH",
            ),
            onIntent = {},
        )

        Section.Apps -> AppsContent(
            uiState = AppsUiState(
                serial = "R5CWC46JFLH",
                filter = AppFilter.USER,
                list = AppListState.Loaded(apps.map { it.first }),
                selectedPackage = "com.revolut.revolut",
                appearances = apps.associate { (packageName, label) -> packageName to AppAppearance(label, icon = null) },
                details = AppDetailsState.Loaded(
                    AppDetails("com.revolut.revolut", "10.62", 1062, 35, 26, "2026-03-14 09:12", "2026-10-28 18:40", 186_000_000, 412_000_000, 38_000_000),
                ),
            ),
            onIntent = {},
        )

        Section.Files -> FilesContent(
            uiState = FilesUiState(serial = "R5CWC46JFLH", directory = "/sdcard", pathInput = "/sdcard", listing = FileListing.Loaded(files)),
            onIntent = {},
        )

        Section.Logcat -> LogcatContent(uiState = LogcatUiState(serial = "R5CWC46JFLH", rows = logRows), onIntent = {})

        Section.Shell -> ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = terminalRows), onIntent = {})

        Section.Screen -> ScreenContent(uiState = ScreenUiState(serial = "R5CWC46JFLH"), onIntent = {})

        Section.Settings -> SettingsContent(
            uiState = SettingsUiState(adbStatus = AdbStatus.Found(AdbInfo("/opt/android-sdk/platform-tools/adb", "37.0.0")), themeMode = ThemeMode.SYSTEM),
            onIntent = {},
        )
    }
}

private val apps = listOf(
    "com.android.chrome" to "Chrome",
    "com.google.android.apps.maps" to "Maps",
    "com.revolut.revolut" to "Revolut",
    "com.spotify.music" to "Spotify",
    "org.telegram.messenger" to "Telegram",
    "com.example.notes" to null,
    "com.whatsapp" to "WhatsApp",
)

private val files = listOf(
    RemoteFile("Android", "/sdcard/Android", RemoteFileType.DIRECTORY, null, "2026-10-30 21:04", "drwxrwx--x"),
    RemoteFile("DCIM", "/sdcard/DCIM", RemoteFileType.DIRECTORY, null, "2026-11-01 12:40", "drwxrwx--x"),
    RemoteFile("Download", "/sdcard/Download", RemoteFileType.DIRECTORY, null, "2026-11-01 09:15", "drwxrwx--x"),
    RemoteFile("Music", "/sdcard/Music", RemoteFileType.DIRECTORY, null, "2026-09-02 17:22", "drwxrwx--x"),
    RemoteFile("Pictures", "/sdcard/Pictures", RemoteFileType.DIRECTORY, null, "2026-10-21 08:03", "drwxrwx--x"),
    RemoteFile("boarding-pass.pdf", "/sdcard/boarding-pass.pdf", RemoteFileType.FILE, 482_133, "2026-10-29 06:51", "-rw-rw----"),
    RemoteFile("screen-recording.mp4", "/sdcard/screen-recording.mp4", RemoteFileType.FILE, 58_310_204, "2026-11-01 14:02", "-rw-rw----"),
    RemoteFile("secret", "/sdcard/secret", RemoteFileType.DIRECTORY, null, null, "d?????????"),
)

private val logRows = listOf(
    LogEntry("11-01 15:04:11.492", 1060, 1291, LogLevel.DEBUG, "SatelliteController", "isInCarrierRoamingNbIotNtn: satellite is disabled"),
    LogEntry("11-01 15:04:11.493", 687, 1902, LogLevel.WARNING, "IPCThreadState", "Sending oneway calls to frozen process."),
    LogEntry("11-01 15:04:11.611", 2210, 2210, LogLevel.INFO, "ActivityManager", "Start proc 4412:com.revolut.revolut/u0a201 for top-activity"),
    LogEntry("11-01 15:04:11.702", 4412, 4412, LogLevel.INFO, "Revolut", "Cold start finished in 412 ms"),
    LogEntry("11-01 15:04:12.019", 4412, 4440, LogLevel.ERROR, "OkHttp", "java.net.SocketTimeoutException: timeout"),
    LogEntry("11-01 15:04:12.020", 4412, 4440, LogLevel.VERBOSE, "Choreographer", "Skipped 3 frames"),
).mapIndexed { index, entry -> LogRow(index.toLong(), entry) }

private val terminalRows = listOf(
    listOf(TerminalSpan("e3q:/ $ ", foreground = TerminalColor.Green), TerminalSpan("getprop ro.product.model")),
    listOf(TerminalSpan("SM-S926B")),
    listOf(TerminalSpan("e3q:/ $ ", foreground = TerminalColor.Green), TerminalSpan("ls /sdcard")),
    listOf(TerminalSpan("Android  DCIM  ", foreground = TerminalColor.BrightBlue, isBold = true), TerminalSpan("boarding-pass.pdf")),
    listOf(TerminalSpan("e3q:/ $ ", foreground = TerminalColor.Green), TerminalSpan(" ", isInverted = true)),
).mapIndexed { index, spans -> TerminalRow(index.toLong(), spans) }
