package com.tlpcraft.adbdesktop.core.data.source.preferences

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID
import java.util.prefs.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class JvmPreferencesDataSourceTest : FunSpec({
    val node = Preferences.userRoot().node("com/tlpcraft/adbdesktop/test-${UUID.randomUUID()}")
    val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }

    afterSpec { node.removeNode() }

    test("persists and clears the adb path") {
        val source = JvmPreferencesDataSource(node, dispatchers)

        source.observeAdbPath().test {
            awaitItem().shouldBeNull()
            source.writeAdbPath("/opt/adb")
            awaitItem() shouldBe "/opt/adb"
            JvmPreferencesDataSource(node, dispatchers).readAdbPath() shouldBe "/opt/adb"

            source.writeAdbPath(null)
            awaitItem().shouldBeNull()
        }
    }
})
