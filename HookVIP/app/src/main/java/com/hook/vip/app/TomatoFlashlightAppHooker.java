package com.hook.vip.app;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class TomatoFlashlightAppHooker {

    /**
     * 针对目标包名生效的 hook 操作
     */
    public static void hook(ClassLoader cl) {
        // 钩住 hasVip 方法
        hookMethod(cl, "com.yuanlue.tomato.lib_common.data.bean.User", "hasVip", true);

        // 钩住 getVip_type 方法
        hookMethod(cl, "com.yuanlue.tomato.lib_common.data.bean.User", "getVip_type", 6);

        Log.d("kong", "Hook 完成，已修改 VIP 状态为永久会员");
    }

    /**
     * 通用 hook 方法
     */
    private static void hookMethod(ClassLoader cl, String className, String methodName, final Object forceResult) {
        XposedHelpers.findAndHookMethod(className, cl, methodName, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                // 修改返回值
                param.setResult(forceResult);
                Log.d("kong", "[HOOK] 修改 " + methodName + "() 返回值: " + forceResult);
            }
        });
    }
}
