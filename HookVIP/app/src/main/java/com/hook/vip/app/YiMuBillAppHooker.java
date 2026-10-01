package com.hook.vip.app;

import com.hook.vip.util.ReflectUtil;
import com.hook.vip.util.XposedUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 一木记账 (com.wangc.bill) Hook 实现
 *
 * 360加固 - 使用 UniversalRealClassLoaderUtil 获取真实 ClassLoader
 * 功能: 解锁永久会员 + 云备份
 *
 * VIP体系:
 *   User.vipType = 0 普通用户 / 1 限时会员 / 2 永久会员
 *   User.isVip() = vipType != 0
 *
 * 云备份链路:
 *   BackupActivity.H0(true) → t0() → HttpManager.checkVip() → 服务端校验
 *     → 回调 d.onResponse 成功: n0.n2(true) + k2.q() → 备份开启
 *     → 回调 d.onResponse 失败: switch.setChecked(false) + o4.a()
 *     → o4.a(): vipType!=0 → "鉴权失败" / vipType==0 → 弹升级窗
 */
public class YiMuBillAppHooker {

    private static final String TAG = "kong";

    /** 2099-12-31 23:59:59 的时间戳，用于伪装VIP过期时间 */
    private static final long VIP_EXPIRE_FOREVER = 4102444800000L;

    private static final String USER_CLASS = "com.wangc.bill.http.entity.User";
    private static final String USER_DB_CLASS = "com.wangc.bill.database.entity.UserDB";

    public static void hook(ClassLoader classLoader) {
        XposedUtil.d("一木记账 Hook 开始...");
        hookUserGetter(classLoader);
        hookUserSetter(classLoader);
        hookMyApplication_e(classLoader);
        hookUserDB(classLoader);
        hookCheckVip(classLoader);
        hookO4(classLoader);
    }

    // ==================== User Getter ====================

    private static void hookUserGetter(ClassLoader classLoader) {
        try {
            XposedUtil.hookReturn(ReflectUtil.findMethod(USER_CLASS, classLoader, "isVip"),
                    "yimubill_user_is_vip", true);
            XposedUtil.hookReturn(ReflectUtil.findMethod(USER_CLASS, classLoader, "getVipType"),
                    "yimubill_user_get_vip_type", 2);
            XposedUtil.hookReturn(ReflectUtil.findMethod(USER_CLASS, classLoader, "getVipTime"),
                    "yimubill_user_get_vip_time", VIP_EXPIRE_FOREVER);

            XposedUtil.d("User getter Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("User getter Hook 失败: " + t, t);
        }
    }

    // ==================== User Setter ====================

    private static void hookUserSetter(ClassLoader classLoader) {
        try {
            Method setVipType = ReflectUtil.findMethod(USER_CLASS, classLoader, "setVipType", int.class);
            XposedUtil.hook(setVipType, "yimubill_user_set_vip_type",
                    chain -> chain.proceed(new Object[]{2}));

            Method setVipTime = ReflectUtil.findMethod(USER_CLASS, classLoader, "setVipTime", long.class);
            XposedUtil.hook(setVipTime, "yimubill_user_set_vip_time",
                    chain -> chain.proceed(new Object[]{VIP_EXPIRE_FOREVER}));

            XposedUtil.d("User setter Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("User setter Hook 失败: " + t, t);
        }
    }

    // ==================== MyApplication.e() — 修改字段值 ====================

    /**
     * o4.a() / n0.C0() 直接访问 user.vipType 字段(public), 不走 getter
     * 必须在每次获取 User 后通过反射修改字段
     */
    private static void hookMyApplication_e(ClassLoader classLoader) {
        try {
            Method e = ReflectUtil.findMethod("com.wangc.bill.application.MyApplication", classLoader, "e");
            XposedUtil.hook(e, "yimubill_myapplication_e", chain -> {
                Object user = chain.proceed();
                fixVipFields(user);
                return user;
            });

            XposedUtil.d("MyApplication.e() Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("MyApplication.e() Hook 失败: " + t, t);
        }
    }

    private static void fixVipFields(Object user) {
        if (user == null) {
            return;
        }
        try {
            Field vipTypeField = ReflectUtil.findField(user.getClass(), "vipType");
            int oldVipType = vipTypeField.getInt(user);
            if (oldVipType != 2) {
                vipTypeField.setInt(user, 2);
                XposedUtil.d("[MyApplication.e] vipType: " + oldVipType + " → 2");
            }
        } catch (Throwable ignored) {
            // 字段名不一致时退回“按名字猜”的兜底策略
            for (Field field : user.getClass().getDeclaredFields()) {
                if (field.getType() != int.class || !field.getName().contains("vip")) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    int oldValue = field.getInt(user);
                    if (oldValue != 2) {
                        field.setInt(user, 2);
                        XposedUtil.d("[MyApplication.e] " + field.getName() + ": " + oldValue + " → 2");
                    }
                } catch (Throwable ignoredInner) {
                    // 忽略单个字段失败
                }
            }
        }

        try {
            Field vipTimeField = ReflectUtil.findField(user.getClass(), "vipTime");
            long oldVipTime = vipTimeField.getLong(user);
            if (oldVipTime < System.currentTimeMillis()) {
                vipTimeField.setLong(user, VIP_EXPIRE_FOREVER);
                XposedUtil.d("[MyApplication.e] vipTime: " + oldVipTime + " → " + VIP_EXPIRE_FOREVER);
            }
        } catch (Throwable ignored) {
            // 没有 vipTime 字段就算了
        }
    }

    // ==================== UserDB (本地数据库) ====================

    private static void hookUserDB(ClassLoader classLoader) {
        try {
            XposedUtil.hookReturn(ReflectUtil.findMethod(USER_DB_CLASS, classLoader, "getVipType"),
                    "yimubill_userdb_get_vip_type", 2);
            XposedUtil.hookReturn(ReflectUtil.findMethod(USER_DB_CLASS, classLoader, "getVipTime"),
                    "yimubill_userdb_get_vip_time", VIP_EXPIRE_FOREVER);

            XposedUtil.d("UserDB Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("UserDB Hook 失败: " + t, t);
        }
    }

    // ==================== HttpManager.checkVip() — 伪造服务端校验成功 ====================

    /**
     * 拦截 checkVip 服务端校验，直接返回成功
     *
     * 链路: BackupActivity.t0() → HttpManager.checkVip(callback, day)
     *       → HttpService.checkVip(token, userId, day).enqueue(callback)
     *
     * 伪造一个 code=0 / result=true 的响应直接调用回调,
     * 跳过真实的 HTTP 请求, 这样 BackupActivity 的开关就能正常开启
     */
    private static void hookCheckVip(ClassLoader classLoader) {
        try {
            Class<?> myCallbackClass = ReflectUtil.findClass("com.wangc.bill.http.httpUtils.MyCallback", classLoader);
            Class<?> commonBaseJsonClass = ReflectUtil.findClass("com.wangc.bill.http.protocol.CommonBaseJson", classLoader);
            Class<?> responseClass = ReflectUtil.findClass("retrofit2.Response", classLoader);

            Method checkVip = ReflectUtil.findMethod("com.wangc.bill.http.HttpManager", classLoader,
                    "checkVip", myCallbackClass, int.class);

            XposedUtil.hook(checkVip, "yimubill_check_vip", chain -> {
                XposedUtil.d("[checkVip] 拦截服务端校验，伪造成功响应");

                // 构造 CommonBaseJson<Boolean>: code=0, result=true
                Object fakeBody = ReflectUtil.newInstance(commonBaseJsonClass);
                ReflectUtil.callMethod(fakeBody, "setCode", 0);
                ReflectUtil.callMethod(fakeBody, "setResult", Boolean.TRUE);

                // 构造 Response<CommonBaseJson<Boolean>> 成功响应
                Object fakeResponse = ReflectUtil.callStaticMethod(responseClass, "success", fakeBody);

                // 直接调用回调 onResponse, 跳过 HTTP 请求
                ReflectUtil.callMethod(chain.getArg(0), "onResponse", fakeResponse);

                // 阻止原始方法执行
                return null;
            });

            XposedUtil.d("checkVip Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("checkVip Hook 失败: " + t, t);
        }
    }

    // ==================== o4 — 防御性抑制错误提示 ====================

    /**
     * 如果 checkVip Hook 未覆盖所有调用场景, o4.a() 作为最后防线:
     * - vipType!=0 → 静默吞掉 "鉴权失败" toast
     * - vipType==0 → 吞掉弹出升级弹窗
     */
    private static void hookO4(ClassLoader classLoader) {
        try {
            Class<?> activityClass = ReflectUtil.findClass("androidx.appcompat.app.AppCompatActivity", classLoader);

            Method a = ReflectUtil.findMethod("com.wangc.bill.manager.o4", classLoader, "a",
                    activityClass, String.class, String.class);
            XposedUtil.hook(a, "yimubill_o4_a", chain -> {
                XposedUtil.d("[o4.a] 拦截: " + chain.getArg(1) + " / " + chain.getArg(2));
                return null;
            });

            Method b = ReflectUtil.findMethod("com.wangc.bill.manager.o4", classLoader, "b");
            XposedUtil.hookReturn(b, "yimubill_o4_b", true);

            XposedUtil.d("o4 Hook 完成");
        } catch (Throwable t) {
            XposedUtil.e("o4 Hook 失败: " + t, t);
        }
    }
}
