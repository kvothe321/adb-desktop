package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DisconnectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDeviceInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObserveDevicesUseCase)
    factoryOf(::ObserveSelectedDeviceUseCase)
    factoryOf(::SelectDeviceUseCase)
    factoryOf(::ConnectWirelessDeviceUseCase)
    factoryOf(::PairWirelessDeviceUseCase)
    factoryOf(::DisconnectDeviceUseCase)
    factoryOf(::ObserveDeviceInfoUseCase)
    factoryOf(::ObserveCpuInfoUseCase)
    factoryOf(::ObserveAdbPathOverrideUseCase)
    factoryOf(::SetAdbPathOverrideUseCase)
    factoryOf(::GetAppsUseCase)
    factoryOf(::GetAppDetailsUseCase)
    factoryOf(::ExecuteAppActionUseCase)
}
