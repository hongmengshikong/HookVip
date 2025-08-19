package com.hook.vip;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 只针对目标包名生效
        if ("com.kproduce.weight".equals(loadPackageParam.packageName)) {
            Log.d("kong", "加载目标包：" + loadPackageParam.packageName);
            try {
                // 调用 AppHooker 执行 DexKit 方法查找 + Hook
                new WeightAppHooker(loadPackageParam);
            } catch (Throwable t) {
                Log.e("kong", "AppHooker 初始化失败", t);
            }
        }
        if ("com.swhh.fasting.tomato".equals(loadPackageParam.packageName)) {
            Log.d("kong", "加载目标包：" + loadPackageParam.packageName);

            try {
                // -------------------------------
                // 1. 获取 StubApplication 类并初始化工具
                // -------------------------------
                Class<?> stubAppClass = loadPackageParam.classLoader.loadClass("com.sagittarius.v6.StubApplication");

                // 初始化工具，hook attachBaseContext 获取真实 ClassLoader
                RealClassLoaderUtil.init(stubAppClass);

                // -------------------------------
                // 2. 打印真实 ClassLoader（如果已经准备好）
                // -------------------------------
                if (RealClassLoaderUtil.isReady()) {
                    ClassLoader realCl = RealClassLoaderUtil.getRealClassLoader();
                    Log.d("kong", "真实ClassLoader: " + realCl);

                    // 使用封装的 TomatoAppHooker 来 hook 所有方法
                    TomatoAppHooker hooker = new TomatoAppHooker(realCl);
                    hooker.hookAll();

                } else {
                    Log.d("kong", "真实ClassLoader尚未准备好，稍后再 hook");
                }

            } catch (Throwable t) {
                Log.d("kong", "获取 StubApplication 失败: " + t);
            }
        }

    }
}