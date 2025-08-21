package com.hook.vip.util;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * 工具类：获取真实 ClassLoader 并支持回调
 */
public class NewBaiduRealClassLoaderUtil {

    private static ClassLoader realClassLoader = null;
    private static final List<Runnable> readyCallbacks = new ArrayList<>();

    /**
     * 初始化工具，hook StubApplication.attachBaseContext
     */
    public static void init(Class<?> stubAppClass) {
        XposedHelpers.findAndHookMethod(stubAppClass, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application realApp = (Application) stubAppClass.getField("mRealApplication").get(null);
                if (realApp != null) {
                    realClassLoader = realApp.getClassLoader();
                    Log.d("kong","✅ 获取真实ClassLoader成功: " + realClassLoader);

                    // 执行所有回调
                    for (Runnable r : readyCallbacks) r.run();
                    readyCallbacks.clear();
                }
            }
        });
    }

    /** 获取真实 ClassLoader */
    public static ClassLoader getRealClassLoader() {
        return realClassLoader;
    }

    /** 是否已准备好 */
    public static boolean isReady() {
        return realClassLoader != null;
    }

    /** 注册回调，当 ClassLoader 准备好时执行 */
    public static void onReady(Runnable callback) {
        if (isReady()) {
            callback.run();
        } else {
            readyCallbacks.add(callback);
        }
    }
}