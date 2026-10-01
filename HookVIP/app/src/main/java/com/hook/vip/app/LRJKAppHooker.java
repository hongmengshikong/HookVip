package com.hook.vip.app;

import android.app.Activity;
import android.view.ViewGroup;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 懒人驾考（com.jx885.lrjk）的 hook。
 */
public class LRJKAppHooker {

    public static void hook(ClassLoader cl) {
//        hookSplashAds(cl);

        //免登陆
        hookReturn(cl, "a8.e", "p0", true);

        //永久会员
        hookReturn(cl, "a8.e", "u0", true, int.class);

        //解锁速记技巧
        hookReturn(cl, "com.tencent.mmkv.MMKV", "decodeBool", true, String.class, boolean.class);

        //解锁我的权益页面全部权益显示
        hookReturn(cl, "com.jx885.lrjk.cg.model.vo.VipProfileVo", "isLocked", false);
    }

    /** 查方法 -> 固定返回值 */
    private static void hookReturn(ClassLoader cl, String className, String methodName,
                                   Object value, Object... parameterTypes) {
        try {
            Method method = ReflectUtil.findMethod(className, cl, methodName, parameterTypes);
            XposedUtil.hookReturn(method, "lrjk_" + className + "_" + methodName, value);
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook失败: " + className + "." + methodName, t);
        }
    }

    private static void hookSplashAds(ClassLoader cl) {
        try {
            // 阻止广告请求
            intercept(cl, "com.anythink.splashad.api.ATSplashAd", "loadAd");

            // 阻止广告展示
            intercept(cl, "com.anythink.splashad.api.ATSplashAd", "show",
                    Activity.class, ViewGroup.class);

            // 阻止闪屏逻辑（秒进主页）
            intercept(cl, "com.jx885.lrjk.cg.ui.SplashActivity", "Q0");

            XposedUtil.d("hookSplashAds 初始化完成");

        } catch (Throwable t) {
            XposedUtil.e("hookSplashAds 出错: " + t, t);
        }
    }

    /** 直接跳过原方法（等价于旧的 XC_MethodReplacement.replaceHookedMethod 返回 null） */
    private static void intercept(ClassLoader cl, String className, String methodName,
                                  Object... parameterTypes) throws Exception {
        Method method = ReflectUtil.findMethod(className, cl, methodName, parameterTypes);
        XposedUtil.hook(method, "lrjk_" + className + "_" + methodName, chain -> {
            XposedUtil.d("拦截 " + className + "." + methodName + "()");
            return null;
        });
    }
}
