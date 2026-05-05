package com.hook.vip.app;

import android.util.Log;

import java.util.Date;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class YIYanAppHooker {
    private static final String TAG = "kong";

    public static void hook(ClassLoader classLoader) {
        Vip(classLoader);
    }
    private static void Vip(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod(
                "com.jasonhan.GongMing.Beans.User",
                classLoader,
                "isVip",
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean result = (boolean) param.getResult();
                Log.d(TAG, "[isVip] 返回值: " + result);
                param.setResult(true);
            }
        });
        XposedHelpers.findAndHookMethod(
                "com.jasonhan.GongMing.Beans.User",
                classLoader,
                "fillVipData",
                "java.util.Map",
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Map<String, Object> map = (Map<String, Object>) param.args[0];
                if (map != null) {
                    map.put("viptype", 4);
                    map.put("vipdate", new Date());
                    Log.d(TAG,"fillVipData: 已注入终身会员数据");
                }
            }
        });

    }
}
