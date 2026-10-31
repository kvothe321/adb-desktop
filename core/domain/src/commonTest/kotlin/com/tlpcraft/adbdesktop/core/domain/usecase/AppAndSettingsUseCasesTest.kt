package com.tlpcraft.adbdesktop.core.domain.usecase

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAdbSettingsRepository
import com.tlpcraft.adbdesktop.core.testing.FakeAppsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class AppAndSettingsUseCasesTest : FunSpec({
    test("apps are sorted by package name") {
        val repository = FakeAppsRepository().apply {
            appsResult = Outcome.Success(listOf(AppInfo("com.zeta"), AppInfo("com.alpha")))
        }

        GetAppsUseCase(repository)(GetAppsUseCase.Params("serial", AppFilter.USER)) shouldBe
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
