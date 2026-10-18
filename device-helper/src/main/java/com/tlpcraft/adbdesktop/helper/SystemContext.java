package com.tlpcraft.adbdesktop.helper;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class SystemContext {
    private SystemContext() {
    }

    /**
     * A process started by app_process has no application, so the system context comes from an ActivityThread made by
     * hand, the way scrcpy's server does it. Adaptive icons read the icon mask through the current application, so one is
     * made on top of that context. The ActivityThread's handler needs a looper.
     */
    static Context create() throws Exception {
        Looper.prepare();
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Constructor<?> constructor = activityThreadClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object activityThread = constructor.newInstance();
        setField(activityThreadClass, "sCurrentActivityThread", null, activityThread);
        fillConfigurationController(activityThreadClass, activityThread);
        Method getSystemContext = activityThreadClass.getDeclaredMethod("getSystemContext");
        Context context = (Context) getSystemContext.invoke(activityThread);

        Application application = new Application();
        Method attachBaseContext = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
        attachBaseContext.setAccessible(true);
        attachBaseContext.invoke(application, context);
        setField(activityThreadClass, "mInitialApplication", activityThread, application);
        return context;
    }

    /**
     * Since Android 12 the configuration lives in a controller only a real app process creates, and some vendors, such
     * as Samsung, read it while making the system context. Older versions have no such class and need nothing.
     */
    private static void fillConfigurationController(Class<?> activityThreadClass, Object activityThread) throws Exception {
        Class<?> controllerClass;
        try {
            controllerClass = Class.forName("android.app.ConfigurationController");
        } catch (ClassNotFoundException olderAndroid) {
            return;
        }
        Constructor<?> constructor = controllerClass.getDeclaredConstructor(Class.forName("android.app.ActivityThreadInternal"));
        constructor.setAccessible(true);
        setField(activityThreadClass, "mConfigurationController", activityThread, constructor.newInstance(activityThread));
    }

    private static void setField(Class<?> owner, String name, Object target, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
