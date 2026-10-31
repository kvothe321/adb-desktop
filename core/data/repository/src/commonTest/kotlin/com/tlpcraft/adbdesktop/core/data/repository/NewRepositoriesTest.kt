package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakePreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MdnsServiceDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class NewRepositoriesTest : FunSpec({
    test("logcat priorities map to levels") {
        val entry = LogEntryDto("10-31 23:05:50.598", 1, 2, 'W', "Tag", "message")

        entry.toDomain().level shouldBe LogLevel.WARNING
        entry.copy(level = 'F').toDomain().level shouldBe LogLevel.FATAL
    }

    test("files get full paths and a type from their permissions") {
        val directory = RemoteFileDto("Download", "drwxrws---", 4096, "2026-01-28 11:18").toDomain("/sdcard")
        val link = RemoteFileDto("bin", "lrw-r--r--", 11, "2009-01-01 02:00", linkTarget = "/system/bin").toDomain("/")
        val node = RemoteFileDto("null", "crw-rw-rw-", null, "2026-10-31 23:03").toDomain("/dev")

        directory.path shouldBe "/sdcard/Download"
        directory.type shouldBe RemoteFileType.DIRECTORY
        link.path shouldBe "/bin"
        link.type shouldBe RemoteFileType.LINK
        node.type shouldBe RemoteFileType.OTHER
    }

    test("pairing services are told apart from connect services") {
        val source = FakeAdbDeviceDataSource().apply {
            services = listOf(
                MdnsServiceDto("a", "_adb-tls-pairing._tcp", "10.0.0.2:41567"),
                MdnsServiceDto("a", "_adb-tls-connect._tcp", "10.0.0.2:37000"),
            )
        }

        DeviceRepositoryImpl(source, FakePreferencesDataSource()).discoverWirelessServices() shouldBe Outcome.Success(
            listOf(
                WirelessService("a", "10.0.0.2:41567", WirelessService.Kind.PAIRING),
                WirelessService("a", "10.0.0.2:37000", WirelessService.Kind.CONNECT),
            ),
        )
    }

    test("install passes every APK through") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source).install("pixel", listOf("a.apk", "b.apk")) shouldBe Outcome.Success(Unit)
        source.installed shouldContainExactly listOf(listOf("a.apk", "b.apk"))
    }

    context("theme") {
        test("defaults to following the system, also for values it does not know") {
            val preferences = FakePreferencesDataSource().apply { themeMode.value = "PURPLE" }

            AppearanceRepositoryImpl(preferences).observeThemeMode().test {
                awaitItem() shouldBe ThemeMode.SYSTEM
            }
        }

        test("stores the chosen mode") {
            val preferences = FakePreferencesDataSource()

            AppearanceRepositoryImpl(preferences).setThemeMode(ThemeMode.DARK)

            preferences.themeMode.value shouldBe "DARK"
        }
    }
})
