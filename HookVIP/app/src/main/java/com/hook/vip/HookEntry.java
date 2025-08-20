package com.hook.vip;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
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
                // 初始化通用工具类
                UniversalRealClassLoaderUtil.init();

                // 注册回调：等真实 ClassLoader 就绪后再调用 TomatoAppHooker
                UniversalRealClassLoaderUtil.onReady(() -> {
                    ClassLoader cl = UniversalRealClassLoaderUtil.getRealClassLoader();
                    Log.d("kong", "准备调用 TomatoAppHooker.hook，传入真实 ClassLoader");
                    TomatoAppHooker.hook(cl);
                });

            } catch (Throwable t) {
                Log.d("kong", "初始化 UniversalRealClassLoaderUtil 失败: " + t);
            }
        }
        // 只针对目标包名生效
        if ("com.qyxy.tomato.android".equals(loadPackageParam.packageName)) {
            Log.d("kong", "加载目标包：" + loadPackageParam.packageName);
            try {
                // 初始化通用工具类
                UniversalRealClassLoaderUtil.init();
                // 注册回调：等真实 ClassLoader 就绪后再调用 TomatoAppHooker
                UniversalRealClassLoaderUtil.onReady(() -> {
                    ClassLoader cl = UniversalRealClassLoaderUtil.getRealClassLoader();
                    Log.d("kong", "准备调用 TomatoFlashlightAppHooker.hook，传入真实 ClassLoader");
                    TomatoFlashlightAppHooker.hook(cl);
                });
            } catch (Throwable t) {
                Log.e("kong", "初始化 UniversalRealClassLoaderUtil 失败: " + t);
            }
        }

    }
}