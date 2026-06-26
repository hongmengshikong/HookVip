package com.hook.vip.app;

import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

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

    public static void hook(ClassLoader classLoader) {
        Log.d(TAG, "一木记账 Hook 开始...");
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
            Class<?> userClass = XposedHelpers.findClass(
                    "com.wangc.bill.http.entity.User", classLoader);

            XposedHelpers.findAndHookMethod(userClass, "isVip", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });

            XposedHelpers.findAndHookMethod(userClass, "getVipType", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(2);
                }
            });

            XposedHelpers.findAndHookMethod(userClass, "getVipTime", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(VIP_EXPIRE_FOREVER);
                }
            });

            Log.d(TAG, "User getter Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "User getter Hook 失败: " + t);
        }
    }

    // ==================== User Setter ====================

    private static void hookUserSetter(ClassLoader classLoader) {
        try {
            Class<?> userClass = XposedHelpers.findClass(
                    "com.wangc.bill.http.entity.User", classLoader);

            XposedHelpers.findAndHookMethod(userClass, "setVipType", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = 2;
                }
            });

            XposedHelpers.findAndHookMethod(userClass, "setVipTime", long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = VIP_EXPIRE_FOREVER;
                }
            });

            Log.d(TAG, "User setter Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "User setter Hook 失败: " + t);
        }
    }

    // ==================== MyApplication.e() — 修改字段值 ====================

    /**
     * o4.a() / n0.C0() 直接访问 user.vipType 字段(public), 不走 getter
     * 必须在每次获取 User 后通过反射修改字段
     */
    private static void hookMyApplication_e(ClassLoader classLoader) {
        try {
            Class<?> myAppClass = XposedHelpers.findClass(
                    "com.wangc.bill.application.MyApplication", classLoader);

            XposedHelpers.findAndHookMethod(myAppClass, "e", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object user = param.getResult();
                    if (user != null) {
                        try {
                            Field vipTypeField = user.getClass().getDeclaredField("vipType");
                            vipTypeField.setAccessible(true);
                            int oldVipType = vipTypeField.getInt(user);
                            if (oldVipType != 2) {
                                vipTypeField.setInt(user, 2);
                                Log.d(TAG, "[MyApplication.e] vipType: " + oldVipType + " → 2");
                            }
                        } catch (NoSuchFieldException e) {
                            for (Field f : user.getClass().getDeclaredFields()) {
                                if (f.getType() == int.class && f.getName().contains("vip")) {
                                    f.setAccessible(true);
                                    int oldVal = f.getInt(user);
                                    if (oldVal != 2) {
                                        f.setInt(user, 2);
                                        Log.d(TAG, "[MyApplication.e] " + f.getName() + ": " + oldVal + " → 2");
                                    }
                                }
                            }
                        }

                        try {
                            Field vipTimeField = user.getClass().getDeclaredField("vipTime");
                            vipTimeField.setAccessible(true);
                            long oldVipTime = vipTimeField.getLong(user);
                            if (oldVipTime < System.currentTimeMillis()) {
                                vipTimeField.setLong(user, VIP_EXPIRE_FOREVER);
                                Log.d(TAG, "[MyApplication.e] vipTime: " + oldVipTime + " → " + VIP_EXPIRE_FOREVER);
                            }
                        } catch (NoSuchFieldException ignored) {
                        }
                    }
                }
            });

            Log.d(TAG, "MyApplication.e() Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "MyApplication.e() Hook 失败: " + t);
        }
    }

    // ==================== UserDB (本地数据库) ====================

    private static void hookUserDB(ClassLoader classLoader) {
        try {
            Class<?> userDBClass = XposedHelpers.findClass(
                    "com.wangc.bill.database.entity.UserDB", classLoader);

            XposedHelpers.findAndHookMethod(userDBClass, "getVipType", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(2);
                }
            });

            XposedHelpers.findAndHookMethod(userDBClass, "getVipTime", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(VIP_EXPIRE_FOREVER);
                }
            });

            Log.d(TAG, "UserDB Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "UserDB Hook 失败: " + t);
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
            Class<?> httpManagerClass = XposedHelpers.findClass(
                    "com.wangc.bill.http.HttpManager", classLoader);
            Class<?> myCallbackClass = XposedHelpers.findClass(
                    "com.wangc.bill.http.httpUtils.MyCallback", classLoader);
            Class<?> commonBaseJsonClass = XposedHelpers.findClass(
                    "com.wangc.bill.http.protocol.CommonBaseJson", classLoader);
            Class<?> responseClass = XposedHelpers.findClass(
                    "retrofit2.Response", classLoader);

            XposedHelpers.findAndHookMethod(httpManagerClass, "checkVip",
                    myCallbackClass, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "[checkVip] 拦截服务端校验，伪造成功响应");

                    // 构造 CommonBaseJson<Boolean>: code=0, result=true
                    Object fakeBody = commonBaseJsonClass.newInstance();
                    XposedHelpers.callMethod(fakeBody, "setCode", 0);
                    XposedHelpers.callMethod(fakeBody, "setResult", Boolean.TRUE);

                    // 构造 Response<CommonBaseJson<Boolean>> 成功响应
                    Object fakeResponse = XposedHelpers.callStaticMethod(
                            responseClass, "success", fakeBody);

                    // 直接调用回调 onResponse, 跳过 HTTP 请求
                    XposedHelpers.callMethod(param.args[0], "onResponse", fakeResponse);

                    // 阻止原始方法执行
                    param.setResult(null);
                }
            });

            Log.d(TAG, "checkVip Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "checkVip Hook 失败: " + t);
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
            Class<?> o4Class = XposedHelpers.findClass(
                    "com.wangc.bill.manager.o4", classLoader);

            XposedHelpers.findAndHookMethod(o4Class, "a",
                    "androidx.appcompat.app.AppCompatActivity",
                    String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "[o4.a] 拦截: " + param.args[1] + " / " + param.args[2]);
                    param.setResult(null);
                }
            });

            XposedHelpers.findAndHookMethod(o4Class, "b", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });

            Log.d(TAG, "o4 Hook 完成");
        } catch (Throwable t) {
            Log.e(TAG, "o4 Hook 失败: " + t);
        }
    }
}
