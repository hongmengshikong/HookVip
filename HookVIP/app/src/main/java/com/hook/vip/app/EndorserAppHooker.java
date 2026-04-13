package com.hook.vip.app;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

// 背书匠
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
        XposedHelpers.findAndHookMethod(
                "com.jpm.comx.bean.BaseConfigModel",
                classLoader,
                "isVip",
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();
                        Log.d(TAG, "[isVip] 返回值: " + result);
                        param.setResult(true);

                        // 可选：调用栈
//                        Log.d(TAG, "[isVip] 调用栈:\n" + Log.getStackTraceString(new Throwable()));
                    }
                }
        );

        // 2️⃣ 写入本地缓存 k0.a(key, value)
//        XposedHelpers.findAndHookMethod(
//                "b9.k0",
//                classLoader,
//                "a",
//                "java.lang.String",
//                "java.io.Serializable",
//                new XC_MethodHook() {
//
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//
//                        String key = (String) param.args[0];
//                        Object value = param.args[1];
//
//                        // ⭐ 只关心会员 key（减少噪声）
//                        if ("ur_ax_re_v_ic_ac".equals(key)) {
//                            Log.d(TAG, "[k0.a] 写入会员状态 -> key: " + key + " value: " + value);
//
////                            Log.d(TAG, "[k0.a] 调用栈:\n" +
////                                    Log.getStackTraceString(new Throwable()));
//                        }
//                    }
//                }
//        );
    }

    // ================= 读取链 =================
    private static void ReadHook(ClassLoader classLoader) {

        // 3️⃣ 业务层读取 MobileXUser.vip()
        XposedHelpers.findAndHookMethod(
                "com.jpm.comx.module.MobileXUser",
                classLoader,
                "vip",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, "[MobileXUser.vip] 调用");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();
                        Log.d(TAG, "[MobileXUser.vip] 返回值: " + result);
                        param.setResult(true);


//                        Log.d(TAG, "[MobileXUser.vip] 调用栈:\n" +
//                                Log.getStackTraceString(new Throwable()));
                    }
                }
        );

        // 4️⃣ 底层读取 d1.b(key, default)
//        XposedHelpers.findAndHookMethod(
//                "b9.d1",
//                classLoader,
//                "b",
//                "java.lang.String",
//                boolean.class,
//                new XC_MethodHook() {
//
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//
//                        String key = (String) param.args[0];
//                        boolean def = (boolean) param.args[1];
//
//                        if ("ur_ax_re_v_ic_ac".equals(key)) {
//                            Log.d(TAG, "[d1.b] 读取会员状态 -> key: " + key + " default: " + def);
//                        }
//                    }
//
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                        String key = (String) param.args[0];
//
//                        if ("ur_ax_re_v_ic_ac".equals(key)) {
//                            boolean result = (boolean) param.getResult();
//                            Log.d(TAG, "[d1.b] 返回值: " + result);
//
////                            Log.d(TAG, "[d1.b] 调用栈:\n" +
////                                    Log.getStackTraceString(new Throwable()));
//                        }
//                    }
//                }
//        );
    }
    private static void UserBeanHook(ClassLoader classLoader) {

        // ================= memberFlag =================
        /*
        无效hook点

        XposedHelpers.findAndHookMethod(
                "com.jpm.comx.login.model.UserBean",
                classLoader,
                "getMemberFlag",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // 一般 getter 不需要 before
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object result = param.getResult();

                            Log.d(TAG, "====================");
                            Log.d(TAG, "[UserBean.getMemberFlag]");
                            Log.d(TAG, "返回值: " + result);

                            if (param.thisObject != null) {
                                Log.d(TAG, "对象: " + param.thisObject.toString());
                            }

//                            Log.d(TAG, "调用栈:\n" +
//                                    Log.getStackTraceString(new Throwable()));
                            Log.d(TAG, "====================");

                        } catch (Throwable e) {
                            Log.e(TAG, "getMemberFlag hook error", e);
                        }
                    }
                }
        );
         */

        // ================= memberExpireDay =================
        XposedHelpers.findAndHookMethod(
                "com.jpm.comx.login.model.UserBean",
                classLoader,
                "getMemberExpireDay",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // getter 不需要 before
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object result = param.getResult();

                            Log.d(TAG, "====================");
                            Log.d(TAG, "[UserBean.getMemberExpireDay]");
                            Log.d(TAG, "返回值: " + result);
                            if (param.thisObject != null) {
                                Log.d(TAG, "对象: " + param.thisObject.toString());
                            }
                            param.setResult("2099-12-31");

//                            Log.d(TAG, "调用栈:\n" +
//                                    Log.getStackTraceString(new Throwable()));
                            Log.d(TAG, "====================");

                        } catch (Throwable e) {
                            Log.e(TAG, "getMemberExpireDay hook error", e);
                        }
                    }
                }
        );
    }


}