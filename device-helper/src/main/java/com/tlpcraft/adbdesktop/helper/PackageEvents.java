package com.tlpcraft.adbdesktop.helper;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;

import java.io.PrintStream;

/**
 * Prints a line each time an app is installed, removed or updated, until the process is killed. Run with app_process
 * as the shell user: {@code app_process / com.tlpcraft.adbdesktop.helper.PackageEvents}. Lines are
 * {@code <event> <package>}; a first {@code ready} line says the callback is listening.
 */
public final class PackageEvents {
    private static final String SHELL_PACKAGE = "com.android.shell";

    private PackageEvents() {
    }

    public static void main(String[] args) throws Exception {
        Context shell = SystemContext.create().createPackageContext(SHELL_PACKAGE, 0);
        LauncherApps launcherApps = (LauncherApps) shell.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        PrintStream out = System.out;
        launcherApps.registerCallback(new Printer(out), new Handler(Looper.myLooper()));
        out.println("ready");
        out.flush();
        Looper.loop();
    }

    private static final class Printer extends LauncherApps.Callback {
        private final PrintStream out;

        Printer(PrintStream out) {
            this.out = out;
        }

        private void print(String event, String packageName) {
            out.println(event + " " + packageName);
            out.flush();
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            print("added", packageName);
        }

        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            print("removed", packageName);
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            print("changed", packageName);
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            for (String packageName : packageNames) print("changed", packageName);
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            for (String packageName : packageNames) print("changed", packageName);
        }
    }
}
