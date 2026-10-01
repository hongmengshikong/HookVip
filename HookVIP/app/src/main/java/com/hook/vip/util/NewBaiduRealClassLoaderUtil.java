package com.hook.vip.util;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具类：获取真实 ClassLoader 并支持回调（百度壳 StubApplication.attachBaseContext 路线）。
 *
 * 已迁移到 libxposed API 102。
 */
public class NewBaiduRealClassLoaderUtil {

    private static volatile ClassLoader realClassLoader = null;
    private static final List<Runnable> readyCallbacks = new ArrayList<>();

    /**
     * 初始化工具，hook StubApplication.attachBaseContext
     */
    public static void init(Class<?> stubAppClass) {
        try {
            Method attachBaseContext = ReflectUtil.findMethod(stubAppClass, "attachBaseContext", Context.class);
            XposedUtil.hook(attachBaseContext, "baidu_real_classloader_attach", chain -> {
                Object result = chain.proceed();

                Application realApp = null;
                try {
                    Field field = ReflectUtil.findField(stubAppClass, "mRealApplication");
                    Object value = field.get(null);
                    if (value instanceof Application) {
                        realApp = (Application) value;
                    }
                } catch (Throwable t) {
                    XposedUtil.e("读取 mRealApplication 失败", t);
                }

                if (realApp == null) {
                    return result;
                }

                List<Runnable> pending;
                synchronized (NewBaiduRealClassLoaderUtil.class) {
                    realClassLoader = realApp.getClassLoader();
                    pending = new ArrayList<>(readyCallbacks);
                    readyCallbacks.clear();
                }
                XposedUtil.d("✅ 获取真实ClassLoader成功: " + realClassLoader);

                // 执行所有回调
                for (Runnable r : pending) {
                    try {
                        r.run();
                    } catch (Throwable t) {
                        XposedUtil.e("❌ 回调执行出错: " + t.getMessage(), t);
                    }
                }
                return result;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ 注册 attachBaseContext hook 失败", t);
        }
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
            return;
        }
        synchronized (NewBaiduRealClassLoaderUtil.class) {
            if (realClassLoader != null) {
                callback.run();
                return;
            }
            readyCallbacks.add(callback);
        }
    }
}
