package com.hook.vip;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * 通用真实 ClassLoader 工具类
 * 通过 Hook Application.attach(Context) 获取最终真实 ClassLoader
 * 基本通杀所有加固
 */
public class UniversalRealClassLoaderUtil {

    private static ClassLoader realClassLoader = null;
    private static final List<Runnable> readyCallbacks = new ArrayList<>();

    /**
     * 初始化工具类
     * 必须在 handleLoadPackage 里调用一次
     */
    public static void init() {
        if (realClassLoader != null) {
            Log.d("kong", "⚠️ 已经初始化过，无需重复 hook");
            return;
        }

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.args[0];
                realClassLoader = context.getClassLoader();

                Log.d("kong", "✅ 获取真实ClassLoader成功: " + realClassLoader);

                // 执行所有等待的回调
                for (Runnable r : readyCallbacks) {
                    try {
                        r.run();
                    } catch (Throwable t) {
                        Log.e("kong", "❌ 回调执行出错: " + t.getMessage());
                    }
                }
                readyCallbacks.clear();
            }
        });

        Log.d("kong", "⏳ 正在等待 Application.attach 提供真实 ClassLoader...");
    }

    /** 获取真实 ClassLoader */
    public static ClassLoader getRealClassLoader() {
        return realClassLoader;
    }

    /** 是否已准备好 */
    public static boolean isReady() {
        return realClassLoader != null;
    }

    /** 注册回调，当真实 ClassLoader 准备好时执行 */
    public static void onReady(Runnable callback) {
        if (isReady()) {
            callback.run();
        } else {
            readyCallbacks.add(callback);
        }
    }
}
