package com.tlpcraft.adbdesktop.core.domain.usecase

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAdbSettingsRepository
import com.tlpcraft.adbdesktop.core.testing.FakeAppsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class AppAndSettingsUseCasesTest : FunSpec({
    test("apps are sorted by package name") {
        val repository = FakeAppsRepository().apply {
            apps.value = Outcome.Success(listOf(AppInfo("com.zeta"), AppInfo("com.alpha")))
        }

        ObserveAppsUseCase(repository)(ObserveAppsUseCase.Params("serial", AppFilter.USER)).first() shouldBe
            Outcome.Success(listOf(AppInfo("com.alpha"), AppInfo("com.zeta")))
    }

    test("a blank adb path switches back to automatic detection") {
        val repository = FakeAdbSettingsRepository(initialPath = "/opt/adb")

        SetAdbPathOverrideUseCase(repository)("   ")

        repository.adbPath.value.shouldBeNull()
    }

    test("the adb path is trimmed") {
        val repository = FakeAdbSettingsRepository()

        SetAdbPathOverrideUseCase(repository)("  /opt/platform-tools/adb ")

        repository.adbPath.value shouldBe "/opt/platform-tools/adb"
    }
})
