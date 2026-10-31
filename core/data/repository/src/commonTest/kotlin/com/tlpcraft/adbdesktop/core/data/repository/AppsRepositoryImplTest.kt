package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class AppsRepositoryImplTest : FunSpec({
    test("user apps map to third-party packages") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source).getApps("pixel", AppFilter.USER) shouldBe Outcome.Success(listOf(AppInfo("com.example.notes")))
        source.requestedFilters shouldContainExactly listOf(PackageFilterDto.THIRD_PARTY)
    }

    test("open launches the app") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source).executeAction("pixel", "com.example.notes", AppAction.OPEN)

        source.actions shouldContainExactly listOf(PackageActionDto.LAUNCH)
    }

    test("data source exceptions become typed errors") {
        AppsRepositoryImpl(FakeAdbPackageDataSource()).getAppDetails("pixel", "com.example.notes") shouldBe
            Outcome.Failure(DeviceError.DeviceNotFound("pixel"))
    }
})
