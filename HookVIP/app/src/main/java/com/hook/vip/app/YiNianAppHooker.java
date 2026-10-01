package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 针对 com.mt.copyidea 的 hook
 */
public class YiNianAppHooker {

    private static final String USER_DATA = "com.mt.copyidea.data.api.Api$UserRes$UserData";
    private static final String WX_TOKEN = "com.mt.copyidea.data.bean.api.WXLoginToken$Data";

    public YiNianAppHooker(ClassLoader classLoader) {
        Api(classLoader);
//        WXLoginToken(classLoader);
    }

    private static void Api(ClassLoader cl) {
        try {
            Method isVip = ReflectUtil.findMethod(USER_DATA, cl, "is_vip");
            XposedUtil.hookReturn(isVip, "yinian_is_vip", 1);
            XposedUtil.d("[HOOK] 强制 is_vip = 1");
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook " + USER_DATA + ".is_vip 失败", t);
        }

        try {
            Method getVipEndTime = ReflectUtil.findMethod(USER_DATA, cl, "getVip_end_time");
            XposedUtil.hookReturn(getVipEndTime, "yinian_vip_end_time", "2099-12-31 23:59:59");
            XposedUtil.d("[HOOK] 强制 getVip_end_time = 2099-12-31 23:59:59");
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook " + USER_DATA + ".getVip_end_time 失败", t);
        }
    }

    private static void WXLoginToken(ClassLoader cl) {
        try {
            Method isVip = ReflectUtil.findMethod(WX_TOKEN, cl, "is_vip");
            XposedUtil.hook(isVip, "yinian_wx_is_vip", chain -> {
                XposedUtil.d("[HOOK] WXLoginToken$Data.is_vip");
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook " + WX_TOKEN + ".is_vip 失败", t);
        }

        try {
            Method getVipEnd = ReflectUtil.findMethod(WX_TOKEN, cl, "getVip_end");
            XposedUtil.hook(getVipEnd, "yinian_wx_vip_end", chain -> {
                XposedUtil.d("[HOOK] WXLoginToken$Data.getVip_end");
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook " + WX_TOKEN + ".getVip_end 失败", t);
        }
    }
}
