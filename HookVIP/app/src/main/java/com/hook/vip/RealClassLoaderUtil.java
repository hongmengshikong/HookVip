package com.hook.vip;

import android.app.Application;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class RealClassLoaderUtil {

    // 全局保存真实ClassLoader
    private static ClassLoader realClassLoader = null;

    /**
     * 初始化工具，必须在 StubApplication.attachBaseContext 执行后调用
     * @param stubAppClass StubApplication 类对象
     */
    public static void init(final Class<?> stubAppClass) {
        try {
            // hook attachBaseContext 方法
            de.robv.android.xposed.XposedHelpers.findAndHookMethod(
                    stubAppClass,
                    "attachBaseContext",
                    android.content.Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Application stubApp = (Application) param.thisObject;

                            // 获取真实 Application
                            Application realApp = (Application) stubAppClass.getField("mRealApplication").get(null);
                            if (realApp != null) {
                                realClassLoader = realApp.getClassLoader();
                                Log.d("kong","✅ 获取真实ClassLoader成功: " + realClassLoader);

                                // 在这里立即执行 hook
                                TomatoAppHooker hooker = new TomatoAppHooker(realClassLoader);
                                hooker.hookAll();
                            } else {
                                Log.d("kong","⚠️ 真实Application为空，可能 skipLoad=true");
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            Log.d("kong","❌ RealClassLoaderUtil 初始化失败: " + t);
        }
    }

    /**
     * 获取真实 ClassLoader
     * @return 真实 ClassLoader，如果还没初始化则返回 null
     */
    public static ClassLoader getRealClassLoader() {
        return realClassLoader;
    }

    /**
     * 判断 ClassLoader 是否已经获取成功
     */
    public static boolean isReady() {
        return realClassLoader != null;
    }
}