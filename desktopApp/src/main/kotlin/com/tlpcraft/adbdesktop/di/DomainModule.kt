package com.tlpcraft.adbdesktop.di

import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DisconnectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDeviceInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveWirelessDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.CreateFolderUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DeleteFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DownloadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.ObserveFilesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.UploadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ClearLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveAppProcessesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.CaptureScreenshotUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.RecordScreenUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.SaveRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.StopRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
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
    factoryOf(::ObserveAppsUseCase)
    factoryOf(::ObserveAppDetailsUseCase)
    factoryOf(::ExecuteAppActionUseCase)
    factoryOf(::InstallApkUseCase)
    factoryOf(::ObserveWirelessDevicesUseCase)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::DeleteFileUseCase)
    factoryOf(::DownloadFileUseCase)
    factoryOf(::ObserveFilesUseCase)
    factoryOf(::UploadFileUseCase)
    factoryOf(::ClearLogcatUseCase)
    factoryOf(::ObserveLogcatUseCase)
    factoryOf(::ObserveAppProcessesUseCase)
    factoryOf(::CaptureScreenshotUseCase)
    factoryOf(::ObserveMirrorUseCase)
    factoryOf(::RecordScreenUseCase)
    factoryOf(::SaveRecordingUseCase)
    factoryOf(::StopRecordingUseCase)
    factoryOf(::ObserveThemeModeUseCase)
    factoryOf(::ObserveAdbInfoUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::RunShellSessionUseCase)
}
