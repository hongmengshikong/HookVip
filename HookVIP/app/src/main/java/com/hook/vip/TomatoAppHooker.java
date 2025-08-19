package com.hook.vip;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * 针对 com.swhh.fasting.tomato 的 hook
 */
public class TomatoAppHooker {

    /**
     * 注册所有 hook
     */
    public static void hook() {
        NewBaiduRealClassLoaderUtil.onReady(() -> {
            ClassLoader cl = NewBaiduRealClassLoaderUtil.getRealClassLoader();
            Log.d("kong", "TomatoAppHooker 开始 hook 方法");

            hookMethod(cl,
                    "com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                    "getViptype", "4");
            hookMethod(cl,
                    "com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                    "getIsvalidvip", "1");
        });
    }

    /**
     * 通用 hook 方法
     */
    private static void hookMethod(ClassLoader cl, String className, String methodName, final String forceResult) {
        try {
            XposedHelpers.findAndHookMethod(className, cl, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d("kong", "[HOOK] 即将调用 " + methodName + "()");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object originalResult = param.getResult();
                    String original = originalResult != null ? originalResult.toString() : "null";

                    Log.d("kong", "[HOOK] " + methodName + "() 原始返回值: " + original);

                    param.setResult(forceResult);

                    Log.d("kong", "[HOOK] 已将返回值修改为: " + forceResult);
                    Log.d("kong", "[HOOK] 修改后返回值: " + param.getResult());
                }
            });

            Log.d("kong", "✅ 成功注册 Hook: " + className + "." + methodName);
        } catch (Exception e) {
            Log.e("kong", "❌ Hook失败: " + className + "." + methodName, e);
        }
    }
}