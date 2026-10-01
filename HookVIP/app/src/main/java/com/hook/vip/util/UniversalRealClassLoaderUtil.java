package com.hook.vip.util;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用真实 ClassLoader 工具类。
 *
 * 通过 Hook {@code Application.attach(Context)} 获取最终真实 ClassLoader，
 * 基本通杀所有加固（壳会在 attachBaseContext 里换成真实 Application）。
 *
 * 已迁移到 libxposed API 102，不再依赖 XposedHelpers / XC_MethodHook。
 */
public class UniversalRealClassLoaderUtil {

    private static final String HOOK_ID = "universal_real_classloader_attach";

    private static volatile ClassLoader realClassLoader = null;
    private static boolean hasInstalledAttachHook = false;
    private static final List<Runnable> readyCallbacks = new ArrayList<>();

    /**
     * 初始化工具类，必须在 HookEntry 的包回调里调用一次
     */
    public static synchronized void init() {
        if (hasInstalledAttachHook) {
            if (realClassLoader != null) {
                XposedUtil.d("⚠️ 已经初始化过，无需重复 hook");
            } else {
                XposedUtil.d("⚠️ 已注册 Application.attach hook，继续等待真实 ClassLoader");
            }
            return;
        }

        hasInstalledAttachHook = true;

        try {
            Method attach = ReflectUtil.findMethod(Application.class, "attach", Context.class);
            XposedUtil.hook(attach, HOOK_ID, chain -> {
                Object result = chain.proceed();

                Object arg = chain.getArg(0);
                if (!(arg instanceof Context)) {
                    return result;
                }
                ClassLoader candidateClassLoader = ((Context) arg).getClassLoader();

                // 某些壳或启动链路会多次触发 Application.attach，这里只消费第一次拿到的有效 ClassLoader
                synchronized (UniversalRealClassLoaderUtil.class) {
                    if (realClassLoader != null) {
                        if (realClassLoader == candidateClassLoader) {
                            XposedUtil.d("ℹ️ ClassLoader 已就绪，忽略重复 attach: " + candidateClassLoader);
                        } else {
                            XposedUtil.d("ℹ️ ClassLoader 已就绪，忽略新的 attach: " + candidateClassLoader
                                    + " @" + System.identityHashCode(candidateClassLoader));
                        }
                        return result;
                    }

                    realClassLoader = candidateClassLoader;
                }

                XposedUtil.d("✅ 获取真实ClassLoader成功: " + candidateClassLoader
                        + " @" + System.identityHashCode(candidateClassLoader));

                // 执行所有等待的回调
                List<Runnable> pending;
                synchronized (UniversalRealClassLoaderUtil.class) {
                    pending = new ArrayList<>(readyCallbacks);
                    readyCallbacks.clear();
                }
                runCallbacks(pending);
                return result;
            });

            XposedUtil.d("⏳ 正在等待 Application.attach 提供真实 ClassLoader...");
        } catch (Throwable t) {
            XposedUtil.e("❌ 注册 Application.attach hook 失败", t);
        }
    }

    /**
     * 兜底：{@code Application.attach} 没有给出 ClassLoader 时，直接采用调用方提供的
     * ClassLoader（一般是包自身的 ClassLoader），并立刻执行等待中的回调。
     */
    public static void adopt(ClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        List<Runnable> pending;
        synchronized (UniversalRealClassLoaderUtil.class) {
            if (realClassLoader != null) {
                return;
            }
            realClassLoader = classLoader;
            pending = new ArrayList<>(readyCallbacks);
            readyCallbacks.clear();
        }
        XposedUtil.d("⚠️ Application.attach 未提供 ClassLoader，退回包自身 ClassLoader: " + classLoader);
        runCallbacks(pending);
    }

    private static void runCallbacks(List<Runnable> pending) {
        for (Runnable r : pending) {
            try {
                r.run();
            } catch (Throwable t) {
                XposedUtil.e("❌ 回调执行出错: " + t.getMessage(), t);
            }
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

    /** 注册回调，当真实 ClassLoader 准备好时执行 */
    public static void onReady(Runnable callback) {
        if (isReady()) {
            callback.run();
            return;
        }
        synchronized (UniversalRealClassLoaderUtil.class) {
            if (realClassLoader != null) {
                callback.run();
                return;
            }
            readyCallbacks.add(callback);
        }
    }
}
