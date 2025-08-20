package com.hook.vip;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * 针对 com.swhh.fasting.tomato 的 hook
 */
public class TomatoAppHooker {

    private static final int MAX_REMAIN_COUNT = 99; // 永远返回的最大次数

    /**
     * 注册所有 hook
     * @param cl 真实的 ClassLoader
     */
    public static void hook(ClassLoader cl) {
        Log.d("kong", "TomatoAppHooker 开始 hook 方法");

        hookMethod(cl,
                "com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                "getViptype", "4");
        hookMethod(cl,
                "com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                "getIsvalidvip", "1");
        // 新增：永远保持 AI 使用次数不变
        hookRemainUseTimesBean(cl);
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

    /**
     * Hook RemainUseTimesBean 保持次数不变
     */
    private static void hookRemainUseTimesBean(ClassLoader cl) {
        String clazz = "com.swhh.fasting.tomato.mvvm.model.RemainUseTimesBean";

        // hook getCount() 永远返回 MAX_REMAIN_COUNT
        try {
            XposedHelpers.findAndHookMethod(clazz, cl, "getCount", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(MAX_REMAIN_COUNT);
                    Log.d("kong", "[HOOK] RemainUseTimesBean.getCount() -> " + MAX_REMAIN_COUNT);
                }
            });
            Log.d("kong", "✅ Hook RemainUseTimesBean.getCount() 成功");
        } catch (Exception e) {
            Log.e("kong", "❌ Hook RemainUseTimesBean.getCount() 失败", e);
        }

        // hook setCount(int) 不执行，保持 MAX_REMAIN_COUNT
        try {
            XposedHelpers.findAndHookMethod(clazz, cl, "setCount", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = MAX_REMAIN_COUNT; // 强制写入最大值
                    Log.d("kong", "[HOOK] RemainUseTimesBean.setCount() 强制修改为 " + MAX_REMAIN_COUNT);
                }
            });
            Log.d("kong", "✅ Hook RemainUseTimesBean.setCount() 成功");
        } catch (Exception e) {
            Log.e("kong", "❌ Hook RemainUseTimesBean.setCount() 失败", e);
        }
    }
}