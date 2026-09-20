package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbFileDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakePreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbInfoDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.AdbInfoDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservedDeviceDataTest : FunSpec({
    val file = RemoteFileDto("a.txt", "-rw-rw----", 1, "2026-10-31 23:05")

    fun FilesRepositoryImpl.names(): Flow<List<String>> = observeFiles("pixel", "/sdcard").map { outcome ->
        outcome.shouldBeInstanceOf<Outcome.Success<List<RemoteFile>>>().data.map { it.name }
    }

    test("deleting updates the listing at once") {
        val source = FakeAdbFileDataSource().apply { listing = listOf(file) }
        val repository = FilesRepositoryImpl(source, pollInterval = 1.hours)

        repository.names().test {
            awaitItem() shouldBe listOf("a.txt")

            source.listing = emptyList()
            repository.delete("pixel", "/sdcard/a.txt")

            awaitItem() shouldBe emptyList()
        }
    }

    test("a finished upload updates the listing at once") {
        val source = FakeAdbFileDataSource()
        val repository = FilesRepositoryImpl(source, pollInterval = 1.hours)

        repository.names().test {
            awaitItem() shouldBe emptyList()

            source.listing = listOf(file)
            repository.upload("pixel", "/home/me/a.txt", "/sdcard").collect {}

            awaitItem() shouldBe listOf("a.txt")
        }
    }

    test("a file added elsewhere is noticed") {
        val source = FakeAdbFileDataSource()

        FilesRepositoryImpl(source, pollInterval = 5.milliseconds).names().test {
            awaitItem() shouldBe emptyList()

            source.listing = listOf(file)

            awaitItem() shouldBe listOf("a.txt")
        }
    }

    test("the adb status is read again when the path changes") {
        var version = "37.0.0"
        val source = object : AdbInfoDataSource {
            override suspend fun readInfo() = AdbInfoDto("/usr/bin/adb", version)
        }
        val preferences = FakePreferencesDataSource()

        AdbSettingsRepositoryImpl(preferences, source, pollInterval = 1.hours).observeAdbInfo().test {
            awaitItem() shouldBe Outcome.Success(AdbInfo("/usr/bin/adb", "37.0.0"))

            version = "38.0.0"
            preferences.writeAdbPath("/opt/adb")

            awaitItem() shouldBe Outcome.Success(AdbInfo("/usr/bin/adb", "38.0.0"))
        }
    }
})
