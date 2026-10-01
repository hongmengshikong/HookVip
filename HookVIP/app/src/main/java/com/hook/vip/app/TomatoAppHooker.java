package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 针对 com.swhh.fasting.tomato 的 hook。
 *
 * 已迁移到 libxposed API 102：不再使用 XposedHelpers / XC_MethodHook，
 * 统一走 {@code XposedUtil.hook(method, id, chain -> ...)}。
 * 原来 "afterHookedMethod 里 setResult" 的写法，等价于
 * {@code Object r = chain.proceed(); return 常量;}
 */
public class TomatoAppHooker {

    private static final int MAX_REMAIN_COUNT = 99; // 永远返回的最大次数

    /**
     * 注册所有 hook
     *
     * @param cl 真实的 ClassLoader
     */
    public static void hook(ClassLoader cl) {
        XposedUtil.d("TomatoAppHooker 开始 hook 方法");

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
     * 通用 hook 方法：先执行原方法，再把返回值替换成常量
     */
    private static void hookMethod(ClassLoader cl, String className, String methodName, final Object forceResult) {
        try {
            Method method = ReflectUtil.findMethod(className, cl, methodName);
            final Object fixed = ReflectUtil.coerce(method.getReturnType(), forceResult);
            XposedUtil.hook(method, "tomato_" + methodName, chain -> {
                Object originalResult = chain.proceed();
                XposedUtil.d("[HOOK] " + methodName + "() 原始返回值: " + originalResult
                        + " -> " + fixed);
                return fixed;
            });

            XposedUtil.d("✅ 成功注册 Hook: " + className + "." + methodName);
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook失败: " + className + "." + methodName, t);
        }
    }

    /**
     * Hook RemainUseTimesBean 保持次数不变
     */
    private static void hookRemainUseTimesBean(ClassLoader cl) {
        String clazz = "com.swhh.fasting.tomato.mvvm.model.RemainUseTimesBean";

        // hook getCount() 永远返回 MAX_REMAIN_COUNT
        try {
            Method getCount = ReflectUtil.findMethod(clazz, cl, "getCount");
            XposedUtil.hookReturn(getCount, "tomato_getCount", MAX_REMAIN_COUNT);
            XposedUtil.d("✅ Hook RemainUseTimesBean.getCount() 成功");
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook RemainUseTimesBean.getCount() 失败", t);
        }

        // hook setCount(int) 强制写入最大值后再执行原方法
        try {
            Method setCount = ReflectUtil.findMethod(clazz, cl, "setCount", int.class);
            XposedUtil.hook(setCount, "tomato_setCount", chain -> {
                XposedUtil.d("[HOOK] RemainUseTimesBean.setCount() 强制修改为 " + MAX_REMAIN_COUNT);
                return chain.proceed(new Object[]{MAX_REMAIN_COUNT});
            });
            XposedUtil.d("✅ Hook RemainUseTimesBean.setCount() 成功");
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook RemainUseTimesBean.setCount() 失败", t);
        }
    }
}
