package com.tlpcraft.adbdesktop.feature.files

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.feature.files.contract.FileListing
import com.tlpcraft.adbdesktop.feature.files.contract.FilesIntent
import com.tlpcraft.adbdesktop.feature.files.contract.FilesUiState
import com.tlpcraft.adbdesktop.feature.files.contract.TransferState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class FilesContentTest : FunSpec({
    test("folders open, and each row offers download and delete") {
        runComposeUiTest {
            val intents = mutableListOf<FilesIntent>()
            val folder = RemoteFile("Download", "/sdcard/Download", RemoteFileType.DIRECTORY, 4_096, "2026-01-28 11:18", "drwxrws---")
            setContent {
                AppTheme {
                    FilesContent(uiState = FilesUiState(serial = "pixel", listing = FileListing.Loaded(listOf(folder))), onIntent = { intents += it })
                }
            }

            onNodeWithText("Download").performClick()
            onAllNodesWithContentDescription("Delete", useUnmergedTree = true)[0].performClick()
            onNodeWithText("Upload").performClick()

            intents shouldContain FilesIntent.EntryClicked(folder)
            intents shouldContain FilesIntent.DeleteClicked(folder)
            intents shouldContain FilesIntent.UploadClicked
        }
    }

    test("a running transfer shows its progress and can be cancelled") {
        runComposeUiTest {
            val intents = mutableListOf<FilesIntent>()
            setContent {
                AppTheme {
                    FilesContent(
                        uiState = FilesUiState(
                            serial = "pixel",
                            listing = FileListing.Loaded(emptyList()),
                            isBusy = true,
                            transfer = TransferState("big.bin", fraction = 0.4f, position = 2, count = 3),
                        ),
                        onIntent = { intents += it },
                    )
                }
            }

            onNodeWithText("big.bin (2 of 3)  40%").assertExists()
            onNodeWithContentDescription("Cancel transfer", useUnmergedTree = true).performClick()

            intents shouldContain FilesIntent.CancelTransferClicked
        }
    }

    test("a failed folder shows the reason and can be retried") {
        runComposeUiTest {
            val intents = mutableListOf<FilesIntent>()
            setContent {
                AppTheme {
                    FilesContent(uiState = FilesUiState(serial = "pixel", listing = FileListing.Failed("Permission denied")), onIntent = { intents += it })
                }
            }

            onNodeWithText("Permission denied").assertExists()
            onNodeWithText("Retry").performClick()

            intents shouldContain FilesIntent.RefreshClicked
        }
    }
})
