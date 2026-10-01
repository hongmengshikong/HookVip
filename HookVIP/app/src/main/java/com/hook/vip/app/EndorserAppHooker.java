package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Method;

/**
 * 背书匠（com.zzdbwku.zizbnea，360 加固）的 hook。
 */
public class EndorserAppHooker {

    private static final String TAG = "kong";

    public static void hook(ClassLoader classLoader) {
        WriteHook(classLoader);
        ReadHook(classLoader);
        UserBeanHook(classLoader);
    }

    // ================= 写入链 =================
    private static void WriteHook(ClassLoader classLoader) {

        // 1️⃣ 服务端模型 -> isVip()
        try {
            Method isVip = ReflectUtil.findMethod("com.jpm.comx.bean.BaseConfigModel", classLoader, "isVip");
            XposedUtil.hook(isVip, "endorser_baseconfig_isvip", chain -> {
                Object original = chain.proceed();
                XposedUtil.d("[isVip] 返回值: " + original + " -> true");
                return Boolean.TRUE;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook BaseConfigModel.isVip 失败", t);
        }

        // 2️⃣ 写入本地缓存 k0.a(key, value) —— 原代码已注释保留
    }

    // ================= 读取链 =================
    private static void ReadHook(ClassLoader classLoader) {

        // 3️⃣ 业务层读取 MobileXUser.vip()
        try {
            Method vip = ReflectUtil.findMethod("com.jpm.comx.module.MobileXUser", classLoader, "vip");
            XposedUtil.hook(vip, "endorser_mobilexuser_vip", chain -> {
                Object original = chain.proceed();
                XposedUtil.d("[MobileXUser.vip] 返回值: " + original + " -> true");
                return Boolean.TRUE;
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook MobileXUser.vip 失败", t);
        }

        // 4️⃣ 底层读取 d1.b(key, default) —— 原代码已注释保留
    }

    // ================= memberExpireDay =================
    private static void UserBeanHook(ClassLoader classLoader) {
        try {
            Method getMemberExpireDay = ReflectUtil.findMethod(
                    "com.jpm.comx.login.model.UserBean", classLoader, "getMemberExpireDay");
            XposedUtil.hook(getMemberExpireDay, "endorser_member_expire_day", chain -> {
                Object original = chain.proceed();
                Object self = chain.getThisObject();
                XposedUtil.d("[UserBean.getMemberExpireDay] 返回值: " + original
                        + (self != null ? " object: " + self : ""));
                return "2099-12-31";
            });
        } catch (Throwable t) {
            XposedUtil.e("❌ Hook UserBean.getMemberExpireDay 失败", t);
        }
    }
}
