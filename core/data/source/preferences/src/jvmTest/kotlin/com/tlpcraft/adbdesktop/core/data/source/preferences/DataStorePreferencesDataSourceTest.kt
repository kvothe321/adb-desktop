package com.tlpcraft.adbdesktop.core.data.source.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.logger.AppLogger
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first

@Tags("platform")
class DataStorePreferencesDataSourceTest : FunSpec({
    val directory = Files.createTempDirectory("adb-desktop-settings").toFile()
    val logger = object : AppLogger {
        override fun debug(tag: String, message: String) = Unit

        override fun info(tag: String, message: String) = Unit

        override fun warn(tag: String, message: String, throwable: Throwable?) = Unit

        override fun error(tag: String, message: String, throwable: Throwable?) = Unit
    }

    suspend fun withSource(fileName: String, block: suspend (PreferencesDataSource) -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { File(directory, fileName) }
        try {
            block(DataStorePreferencesDataSource(dataStore, logger))
        } finally {
            scope.cancel()
        }
    }

    afterSpec { directory.deleteRecursively() }

    test("emits every change to the adb path") {
        withSource("observed.preferences_pb") { source ->
            source.observeAdbPath().test {
                awaitItem().shouldBeNull()
                source.writeAdbPath("/opt/adb")
                awaitItem() shouldBe "/opt/adb"
                source.writeAdbPath(null)
                awaitItem().shouldBeNull()
            }
        }
    }

    test("a change to one setting does not re-emit another") {
        withSource("isolated.preferences_pb") { source ->
            source.observeAdbPath().test {
                awaitItem().shouldBeNull()
                source.writeThemeMode("DARK")
                source.writeAdbPath("/opt/adb")
                awaitItem() shouldBe "/opt/adb"
            }
        }
    }

    test("settings survive a restart") {
        withSource("persisted.preferences_pb") { source ->
            source.writeAdbPath("/opt/adb")
            source.writeThemeMode("DARK")
        }

        withSource("persisted.preferences_pb") { source ->
            source.observeAdbPath().first() shouldBe "/opt/adb"
            source.observeThemeMode().first() shouldBe "DARK"
        }
    }
})
