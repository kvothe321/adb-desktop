package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DisconnectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DiscoverWirelessDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDeviceInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.CreateFolderUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DeleteFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DownloadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.ListFilesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.UploadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ClearLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveAppProcessesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.CaptureScreenshotUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.RecordScreenUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.SaveRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.StopRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ReadAdbInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.shell.RunShellSessionUseCase
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
    factoryOf(::InstallApkUseCase)
    factoryOf(::DiscoverWirelessDevicesUseCase)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::DeleteFileUseCase)
    factoryOf(::DownloadFileUseCase)
    factoryOf(::ListFilesUseCase)
    factoryOf(::UploadFileUseCase)
    factoryOf(::ClearLogcatUseCase)
    factoryOf(::ObserveLogcatUseCase)
    factoryOf(::ObserveAppProcessesUseCase)
    factoryOf(::CaptureScreenshotUseCase)
    factoryOf(::RecordScreenUseCase)
    factoryOf(::SaveRecordingUseCase)
    factoryOf(::StopRecordingUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::ReadAdbInfoUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::RunShellSessionUseCase)
}
