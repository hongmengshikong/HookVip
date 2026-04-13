package com.hook.vip.app;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class YimuListAppHooker {
    public YimuListAppHooker(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        VIPHook(loadPackageParam.classLoader);

    }
    private static final String TAG = "kong";

    private static void VIPHook(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod(
                "com.wangc.todolist.database.entity.User",
                classLoader,
                "getMemberType",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        Object obj = param.thisObject;

                        Log.d(TAG, "====================");
                        Log.d(TAG, "[User.getMemberType] BEFORE");

                        if (obj != null) {
                            try {
                                String nick = (String) XposedHelpers.callMethod(obj, "getNickName");
                                int userId = (int) XposedHelpers.callMethod(obj, "getUserId");

                                Log.d(TAG, "用户ID: " + userId);
                                Log.d(TAG, "昵称: " + nick);
                            } catch (Throwable t) {
                                Log.d(TAG, "获取用户信息异常: " + t);
                            }
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Object result = param.getResult();

                        Log.d(TAG, "[User.getMemberType] AFTER");
                        Log.d(TAG, "原始返回值: " + result);

                        // 👉 可选：强制修改为永久会员
                         param.setResult("PERMANENT");
                         Log.d(TAG, "已修改返回值为: PERMANENT");
                    }
                });
    }
}
