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
                // 获取 StubApplication 并初始化工具
                Class<?> stubAppClass = loadPackageParam.classLoader.loadClass("com.sagittarius.v6.StubApplication");
                NewBaiduRealClassLoaderUtil.init(stubAppClass);

                // 调用 AppHooker 注册 hook（自动延迟执行）
                TomatoAppHooker.hook();

            } catch (Throwable t) {
                Log.d("kong", "获取 StubApplication 失败: " + t);
            }
        }

    }
}