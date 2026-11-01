package com.tlpcraft.adbdesktop.integration

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.di.appModules
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.koin.dsl.koinApplication

private val PNG_SIGNATURE = listOf<Byte>(-119, 80, 78, 71)

@EnabledIf(DeviceConfigured::class)
class AppIconIntegrationTest : FunSpec({
    val serial = System.getenv("ADB_TEST_SERIAL").orEmpty()
    val apps = koinApplication { modules(appModules) }.koin.get<AppsRepository>()

    test("system apps come with their own icons, drawn as PNGs") {
        val packages = apps.observeApps(serial, AppFilter.SYSTEM).first().shouldBeInstanceOf<Outcome.Success<List<AppInfo>>>().data.map { it.packageName }

        val icons = apps.observeAppIcons(serial, packages).toList().last()

        println("icons: ${icons.size} of ${packages.size} apps")
        icons.keys shouldContain "com.android.settings"
        icons.size shouldBeGreaterThan packages.size / 4
        val settings = icons.getValue("com.android.settings").png
        settings.take(PNG_SIGNATURE.size) shouldBe PNG_SIGNATURE
        ImageIO.read(ByteArrayInputStream(settings)).width shouldBe 96

        adb(serial, "shell", "ls", "/data/local/tmp") shouldNotContain "adb-desktop-helper"
        adb(serial, "shell", "ps", "-A", "-o", "ARGS") shouldNotContain "adbdesktop.helper"
    }
})
