package com.tlpcraft.adbdesktop.feature.files

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.CreateFolderUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DeleteFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DownloadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.ObserveFilesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.UploadFileUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeFilesRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostFiles
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.files.contract.FileListing
import com.tlpcraft.adbdesktop.feature.files.contract.FilesIntent
import com.tlpcraft.adbdesktop.feature.files.contract.FilesSideEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FilesViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(selectedSerial: String? = "pixel", isVisible: Boolean = true) {
        val files = FakeFilesRepository(mapOf("/sdcard" to listOf("Download/", "notes.txt"), "/sdcard/Download" to listOf("a.png"), "/" to listOf("sdcard/")))
        val hostFiles = FakeHostFiles()
        val viewModel = FilesViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(
                ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel")))),
                FakeDeviceSelectionRepository(selectedSerial),
            ),
            observeFiles = ObserveFilesUseCase(files),
            uploadFile = UploadFileUseCase(files),
            downloadFile = DownloadFileUseCase(files),
            deleteFile = DeleteFileUseCase(files),
            createFolder = CreateFolderUseCase(files),
            hostFiles = hostFiles,
        ).apply { reduceIntent(FilesIntent.VisibilityChanged(isVisible)) }

        val state get() = viewModel.uiState.value

        fun listed() = (state.listing as FileListing.Loaded).files

        fun names() = listed().map { it.name }

        fun file(name: String) = listed().first { it.name == name }
    }

    test("without a device nothing is listed") {
        Fixture(selectedSerial = null).state.listing shouldBe FileListing.NoDevice
    }

    test("opens shared storage with folders first") {
        Fixture().names() shouldContainExactly listOf("Download", "notes.txt")
    }

    test("navigates into folders and back up, but files do not open") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(FilesIntent.EntryClicked(fixture.file("notes.txt")))
        fixture.state.directory shouldBe "/sdcard"

        fixture.viewModel.reduceIntent(FilesIntent.EntryClicked(fixture.file("Download")))
        fixture.state.directory shouldBe "/sdcard/Download"
        fixture.names() shouldContainExactly listOf("a.png")

        fixture.viewModel.reduceIntent(FilesIntent.UpClicked)
        fixture.viewModel.reduceIntent(FilesIntent.UpClicked)
        fixture.state.directory shouldBe "/"
        fixture.state.canGoUp.shouldBeFalse()
    }

    test("a typed path opens only when it is absolute") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(FilesIntent.PathChanged("relative/path"))
        fixture.viewModel.reduceIntent(FilesIntent.PathSubmitted)
        fixture.state.directory shouldBe "/sdcard"

        fixture.viewModel.reduceIntent(FilesIntent.PathChanged(" /sdcard/Download "))
        fixture.viewModel.reduceIntent(FilesIntent.PathSubmitted)
        fixture.state.directory shouldBe "/sdcard/Download"
    }

    test("a listing failure shows the device's message") {
        val fixture = Fixture()

        fixture.files.failure = DeviceError.CommandFailed("ls: /secret: Permission denied")

        fixture.state.listing shouldBe FileListing.Failed("ls: /secret: Permission denied")
    }

    test("a file added elsewhere shows up on its own") {
        val fixture = Fixture()

        fixture.files.listing.value = mapOf("/sdcard" to listOf("new.txt"))

        fixture.names() shouldContainExactly listOf("new.txt")
    }

    test("a failing listing recovers on its own") {
        val fixture = Fixture()

        fixture.files.failure = DeviceError.CommandFailed("offline")
        fixture.files.failure = null

        fixture.names() shouldContainExactly listOf("Download", "notes.txt")
    }

    test("nothing is read from the device while the screen is hidden") {
        val fixture = Fixture(isVisible = false)

        fixture.state.listing shouldBe FileListing.Loading

        fixture.viewModel.reduceIntent(FilesIntent.VisibilityChanged(true))

        fixture.names() shouldContainExactly listOf("Download", "notes.txt")
    }

    context("transfers") {
        test("uploads the picked files into the current folder") {
            val fixture = Fixture().apply { hostFiles.pickedFiles = listOf("/home/me/a.txt") }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(FilesIntent.UploadClicked)

                awaitItem() shouldBe FilesSideEffect.Uploaded("/sdcard")
            }
            fixture.files.uploads shouldContainExactly listOf("/home/me/a.txt" to "/sdcard")
            fixture.state.isBusy.shouldBeFalse()
        }

        test("dropped files upload like picked ones") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(FilesIntent.FilesDropped(listOf("/home/me/a.txt", "/home/me/b.txt")))

            fixture.files.uploads shouldContainExactly listOf("/home/me/a.txt" to "/sdcard", "/home/me/b.txt" to "/sdcard")
        }

        test("progress shows while a transfer runs, and it can be cancelled") {
            val fixture = Fixture().apply { files.transferGate = CompletableDeferred() }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(FilesIntent.FilesDropped(listOf("/home/me/a.txt", "/home/me/b.txt")))

                fixture.state.transfer.shouldNotBeNull().fraction shouldBe 0.5f
                fixture.state.transfer.shouldNotBeNull().position shouldBe 1
                fixture.state.isBusy shouldBe true

                fixture.viewModel.reduceIntent(FilesIntent.CancelTransferClicked)

                awaitItem() shouldBe FilesSideEffect.TransferCancelled
            }
            fixture.state.transfer.shouldBeNull()
            fixture.state.isBusy.shouldBeFalse()
            fixture.files.uploads.size shouldBe 1
        }

        test("cancelling the file dialog does nothing") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(FilesIntent.UploadClicked)
            fixture.viewModel.reduceIntent(FilesIntent.DownloadClicked(fixture.file("notes.txt")))

            fixture.files.uploads.shouldBeEmpty()
            fixture.files.downloads.shouldBeEmpty()
        }

        test("downloads into the picked folder") {
            val fixture = Fixture().apply { hostFiles.pickedDirectory = "/home/me" }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(FilesIntent.DownloadClicked(fixture.file("notes.txt")))

                awaitItem() shouldBe FilesSideEffect.Downloaded("notes.txt", "/home/me")
            }
            fixture.files.downloads shouldContainExactly listOf("/sdcard/notes.txt" to "/home/me")
        }

        test("a failed transfer is reported") {
            val fixture = Fixture().apply { hostFiles.pickedDirectory = "/home/me" }
            val file = fixture.file("notes.txt")
            fixture.files.failure = DeviceError.CommandFailed("adb: error: failed to stat")

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(FilesIntent.DownloadClicked(file))

                awaitItem() shouldBe FilesSideEffect.OperationFailed("adb: error: failed to stat")
            }
        }
    }

    context("delete") {
        test("waits for confirmation") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(FilesIntent.DeleteClicked(fixture.file("notes.txt")))
            fixture.files.deleted.shouldBeEmpty()

            fixture.viewModel.reduceIntent(FilesIntent.DeleteCancelled)
            fixture.state.pendingDelete.shouldBeNull()
            fixture.files.deleted.shouldBeEmpty()
        }

        test("removes the file once confirmed") {
            val fixture = Fixture()

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(FilesIntent.DeleteClicked(fixture.file("notes.txt")))
                fixture.viewModel.reduceIntent(FilesIntent.DeleteConfirmed)

                awaitItem() shouldBe FilesSideEffect.Deleted("notes.txt")
            }
            fixture.files.deleted shouldContainExactly listOf("/sdcard/notes.txt")
        }
    }

    context("new folder") {
        test("creates it in the current folder") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(FilesIntent.NewFolderClicked)
            fixture.viewModel.reduceIntent(FilesIntent.NewFolderNameChanged("Backups"))
            fixture.viewModel.reduceIntent(FilesIntent.NewFolderSubmitted)

            fixture.files.createdDirectories shouldContainExactly listOf("/sdcard/Backups")
            fixture.state.newFolder.shouldBeNull()
        }

        test("a name with a slash keeps the form open with an error") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(FilesIntent.NewFolderClicked)
            fixture.viewModel.reduceIntent(FilesIntent.NewFolderNameChanged("a/b"))
            fixture.viewModel.reduceIntent(FilesIntent.NewFolderSubmitted)

            val form = fixture.state.newFolder.shouldNotBeNull()
            form.isInvalid shouldBe true
            fixture.files.createdDirectories.shouldBeEmpty()
            fixture.state.listing.shouldBeInstanceOf<FileListing.Loaded>()
        }
    }
})
