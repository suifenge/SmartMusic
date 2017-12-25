package com.suifeng.lib.playerengine.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Reflect util
 */

public class ReflectUtil {

    private static final String TAG = "ReflectUtil";

    public ReflectUtil() {}

    private static Object findObjectInR(String packageName, String className) {
        try {
            Class managerClass = Class.forName(packageName + ".R");
            Class[] classes = managerClass.getClasses();
            Class[] clz = classes;
            int length = classes.length;

            for(int var5 = 0; var5 < length; ++var5) {
                Class c = clz[var5];
                int i = c.getModifiers();
                String name = c.getName();
                String s = Modifier.toString(i);
                if(s.contains("static") && name.endsWith("$" + className)) {
                    return c.getConstructor(new Class[0]).newInstance(new Object[0]);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "exception: " + e.getMessage());
        }

        return null;
    }

    public static Class<?> findClassInR(String packageName, String className) {
        Object obj = findObjectInR(packageName, className);
        return obj != null ? obj.getClass() : null;
    }

    public static int getIntegerInR(Context context, Class<?> classR, String filedName) {
        if(classR != null) {
            try {
                Field intField = classR.getField(filedName);
                int sourceId = intField.getInt(intField);
                return sourceId;
            } catch (NoSuchFieldException noSuchFieldException) {
                Log.d(TAG, "exception: " + noSuchFieldException.getMessage());
            } catch (IllegalAccessException illegalAccessException) {
                Log.d(TAG, "exception: " + illegalAccessException.getMessage());
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.d(TAG, "exception: " + illegalArgumentException.getMessage());
            } catch (Resources.NotFoundException notFoundException) {
                Log.d(TAG, "exception: " + notFoundException.getMessage());
            }
        }

        return -1;
    }

}
