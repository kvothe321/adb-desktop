package com.tlpcraft.adbdesktop.helper;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Prints the launcher icon of each package as a PNG. Run with app_process as the shell user:
 * {@code app_process / com.tlpcraft.adbdesktop.helper.AppIcons <size> <package>...}. Every package gets one line,
 * {@code <package> <base64 png>}, or {@code <package> -} when it has no icon to give.
 */
public final class AppIcons {
    private static final String NO_ICON = "-";

    private AppIcons() {
    }

    public static void main(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        PackageManager packageManager = SystemContext.create().getPackageManager();
        PrintStream out = System.out;
        for (int index = 1; index < args.length; index++) {
            String packageName = args[index];
            out.println(packageName + " " + iconOf(packageManager, packageName, size));
            out.flush();
        }
    }

    /**
     * Reads the icon from the app's own resources. PackageManager's getApplicationIcon would fall back to a default icon
     * through the Application object, which a process started by app_process does not have.
     */
    private static String iconOf(PackageManager packageManager, String packageName, int size) {
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            if (app.icon == 0) return NO_ICON;
            Resources resources = packageManager.getResourcesForApplication(app);
            Drawable icon = resources.getDrawableForDensity(app.icon, DisplayMetrics.DENSITY_XXXHIGH, null);
            if (icon == null) return NO_ICON;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            icon.setBounds(0, 0, size, size);
            icon.draw(new Canvas(bitmap));
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, png);
            bitmap.recycle();
            return Base64.encodeToString(png.toByteArray(), Base64.NO_WRAP);
        } catch (PackageManager.NameNotFoundException | RuntimeException error) {
            return NO_ICON;
        }
    }
}
