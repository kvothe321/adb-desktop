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
import java.nio.charset.StandardCharsets;

/**
 * Prints the name and launcher icon of each package. Run with app_process as the shell user:
 * {@code app_process / com.tlpcraft.adbdesktop.helper.AppAppearances <size> <package>...}. Every package gets one line,
 * {@code <package> <base64 png> <base64 name>}, with {@code -} in place of what it does not have.
 */
public final class AppAppearances {
    private static final String NONE = "-";

    private AppAppearances() {
    }

    public static void main(String[] args) throws Exception {
        int size = Integer.parseInt(args[0]);
        PackageManager packageManager = SystemContext.create().getPackageManager();
        PrintStream out = System.out;
        for (int index = 1; index < args.length; index++) {
            String packageName = args[index];
            out.println(packageName + " " + appearanceOf(packageManager, packageName, size));
            out.flush();
        }
    }

    private static String appearanceOf(PackageManager packageManager, String packageName, int size) {
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            Resources resources = packageManager.getResourcesForApplication(app);
            return iconOf(app, resources, size) + " " + labelOf(app, resources);
        } catch (PackageManager.NameNotFoundException | RuntimeException error) {
            return NONE + " " + NONE;
        }
    }

    /**
     * Reads the icon from the app's own resources. PackageManager's getApplicationIcon would fall back to a default icon
     * through the Application object, which a process started by app_process does not have.
     */
    private static String iconOf(ApplicationInfo app, Resources resources, int size) {
        try {
            if (app.icon == 0) return NONE;
            Drawable icon = resources.getDrawableForDensity(app.icon, DisplayMetrics.DENSITY_XXXHIGH, null);
            if (icon == null) return NONE;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            icon.setBounds(0, 0, size, size);
            icon.draw(new Canvas(bitmap));
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, png);
            bitmap.recycle();
            return Base64.encodeToString(png.toByteArray(), Base64.NO_WRAP);
        } catch (RuntimeException error) {
            return NONE;
        }
    }

    /** The name in the device's language. Apps without one get none, where Android would show the package name. */
    private static String labelOf(ApplicationInfo app, Resources resources) {
        try {
            CharSequence label = app.nonLocalizedLabel;
            if (label == null && app.labelRes != 0) label = resources.getText(app.labelRes);
            String text = label == null ? "" : label.toString().trim();
            if (text.isEmpty() || text.equals(app.packageName)) return NONE;
            return Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        } catch (RuntimeException error) {
            return NONE;
        }
    }
}
