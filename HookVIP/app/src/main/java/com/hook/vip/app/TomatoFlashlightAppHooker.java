package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 番茄闪轻（com.qyxy.tomato.android）的 hook。
 */
public class TomatoFlashlightAppHooker {

    private static final String USER_CLASS = "com.yuanlue.tomato.lib_common.data.bean.User";

    /**
     * 针对目标包名生效的 hook 操作
     */
    public static void hook(ClassLoader cl) {
        // 钩住 hasVip 方法
        hookMethod(cl, USER_CLASS, "hasVip", true);

        // 钩住 getVip_type 方法
        hookMethod(cl, USER_CLASS, "getVip_type", 6);

        XposedUtil.d("Hook 完成，已修改 VIP 状态为永久会员");
    }

    /**
     * 通用 hook 方法：直接把返回值固定成常量
     */
    private static void hookMethod(ClassLoader cl, String className, String methodName, Object forceResult) {
        try {
            Method method = ReflectUtil.findMethod(className, cl, methodName);
            XposedUtil.hookReturn(method, "tomato_flash_" + methodName, forceResult);
            XposedUtil.d("[HOOK] 修改 " + methodName + "() 返回值: " + forceResult);
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook失败: " + className + "." + methodName, t);
        }
    }
}
