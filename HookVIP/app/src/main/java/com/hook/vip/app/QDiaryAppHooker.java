package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * Q日记（com.slfteam.qdiary）的 hook。
 */
public class QDiaryAppHooker {

    private static final String TAG = "kong";
    private static final String USER_ACC = "com.slfteam.slib.account.SUsrAcc";

    public QDiaryAppHooker(ClassLoader classLoader) {
        hook(classLoader);
    }

    private static void hook(ClassLoader classLoader) {
        try {
            Method isVip = ReflectUtil.findMethod(USER_ACC, classLoader, "isVip");
            XposedUtil.hook(isVip, "qdiary_is_vip", chain -> {
                Object original = chain.proceed();
                XposedUtil.d("hook前" + original);
                XposedUtil.d("hook后true");
                return Boolean.TRUE;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook SUsrAcc.isVip 失败", t);
        }

        try {
            Method vipExpired = ReflectUtil.findMethod(USER_ACC, classLoader, "vipExpired");
            XposedUtil.hook(vipExpired, "qdiary_vip_expired", chain -> {
                Object original = chain.proceed();
                XposedUtil.d("hook前" + original);
                XposedUtil.d("hook后false");
                return Boolean.FALSE;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook SUsrAcc.vipExpired 失败", t);
        }
    }
}
