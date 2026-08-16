package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbFileDataSource
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest

class FilesRepositoryImplTest : FunSpec({
    fun remoteFile(type: RemoteFileType) = RemoteFile("big.bin", "/sdcard/big.bin", type, sizeBytes = 100, modified = null, permissions = "-rw-rw----")

    test("upload progress follows the file growing on the device, and ends complete") {
        runTest {
            val source = FakeAdbFileDataSource().apply {
                localSizes["/home/me/big.bin"] = 100
                remoteSizes["/sdcard/big.bin"] = 40
                copyGate = CompletableDeferred()
            }

            FilesRepositoryImpl(source).upload("pixel", "/home/me/big.bin", "/sdcard").test {
                awaitItem() shouldBe Outcome.Success(TransferProgress("big.bin", 40, 100))

                source.copyGate.complete(Unit)

                awaitItem() shouldBe Outcome.Success(TransferProgress("big.bin", 100, 100))
                awaitComplete()
            }
        }
    }

    test("download progress follows the local file") {
        runTest {
            val source = FakeAdbFileDataSource().apply {
                localSizes["/home/me/big.bin"] = 25
                copyGate = CompletableDeferred()
            }

            FilesRepositoryImpl(source).download("pixel", remoteFile(RemoteFileType.FILE), "/home/me").test {
                awaitItem() shouldBe Outcome.Success(TransferProgress("big.bin", 25, 100))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    test("folders have no total") {
        runTest {
            FilesRepositoryImpl(FakeAdbFileDataSource()).download("pixel", remoteFile(RemoteFileType.DIRECTORY), "/home/me").test {
                awaitItem() shouldBe Outcome.Success(TransferProgress("big.bin", 0, null))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    context("cancelling") {
        test("removes a partly uploaded file") {
            runTest {
                val source = FakeAdbFileDataSource().apply {
                    localSizes["/home/me/big.bin"] = 100
                    copyGate = CompletableDeferred()
                }

                FilesRepositoryImpl(source).upload("pixel", "/home/me/big.bin", "/sdcard").test {
                    awaitItem()
                    cancel()
                }

                source.deletedRemote shouldContainExactly listOf("/sdcard/big.bin")
            }
        }

        test("removes a partly downloaded file") {
            runTest {
                val source = FakeAdbFileDataSource().apply { copyGate = CompletableDeferred() }

                FilesRepositoryImpl(source).download("pixel", remoteFile(RemoteFileType.FILE), "/home/me").test {
                    awaitItem()
                    cancel()
                }

                source.deletedLocal shouldContainExactly listOf("/home/me/big.bin")
            }
        }

        test("never removes a folder, which may have existed already") {
            runTest {
                val source = FakeAdbFileDataSource().apply { copyGate = CompletableDeferred() }

                FilesRepositoryImpl(source).upload("pixel", "/home/me/photos", "/sdcard").test {
                    awaitItem()
                    cancel()
                }

                source.deletedRemote.shouldBeEmpty()
            }
        }
    }
})
