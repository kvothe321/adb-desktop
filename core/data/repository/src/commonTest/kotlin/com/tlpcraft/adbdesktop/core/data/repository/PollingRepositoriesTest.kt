package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbDevicePropertiesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class PollingRepositoriesTest : FunSpec({
    test("cpu usage is the busy share between consecutive samples") {
        runTest {
            val source = FakeAdbDevicePropertiesDataSource(
                listOf(
                    CpuSnapshotDto(busyJiffies = 100, totalJiffies = 1_000, coreCount = 8, maxFrequencyKHz = 2_400_000),
                    CpuSnapshotDto(busyJiffies = 150, totalJiffies = 1_200, coreCount = 8, maxFrequencyKHz = 2_400_000),
                    CpuSnapshotDto(busyJiffies = 150, totalJiffies = 1_200, coreCount = 8, maxFrequencyKHz = null),
                ),
            )

            CpuRepositoryImpl(source).observeCpuInfo("pixel").test {
                awaitItem() shouldBe Outcome.Success(CpuInfo(usagePercent = 25f, coreCount = 8, maxFrequencyMHz = 2_400))
                awaitItem() shouldBe Outcome.Success(CpuInfo(usagePercent = 0f, coreCount = 8, maxFrequencyMHz = null))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    test("device info combines static properties with the battery level") {
        runTest {
            DeviceInfoRepositoryImpl(FakeAdbDevicePropertiesDataSource(emptyList())).observeDeviceInfo("pixel").test {
                awaitItem() shouldBe Outcome.Success(DeviceInfo(manufacturer = "Google", model = "Pixel 8", androidVersion = "15", batteryLevel = 80))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
})
