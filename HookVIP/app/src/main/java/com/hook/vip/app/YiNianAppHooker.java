package com.hook.vip.app;


import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 针对 com.mt.copyidea 的 hook
 */
public class YiNianAppHooker {
    public YiNianAppHooker(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Api(loadPackageParam.classLoader);
//        WXLoginToken(loadPackageParam.classLoader);

    }
    private static void Api(ClassLoader cl){
        XposedHelpers.findAndHookMethod(
                "com.mt.copyidea.data.api.Api$UserRes$UserData",
                cl,
                "is_vip",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.d("kong","[BEFORE] 即将调用 Api$is_vip");
                        Log.d("kong", "[HOOK] 强制 is_vip = 1");
                        param.setResult(1); // 直接返回 1
                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        // 获取原始返回值
//                        Object originalResult = param.getResult();
////                        String original = originalResult != null ? (String) originalResult : "null";
//
//                        Log.d("kong", "[HOOK] Api$is_vip() 原始返回值: " + originalResult);
//                        param.setResult(1);
//                        Object modifiedResult = param.getResult();
//                        Log.d("kong", "[HOOK] Api$is_vip() 修改返回值: " + modifiedResult);
//                    }
                });
        XposedHelpers.findAndHookMethod(
                "com.mt.copyidea.data.api.Api$UserRes$UserData",
                cl,
                "getVip_end_time",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.d("kong","[BEFORE] 即将调用 Api$getVip_end_time");
                        param.setResult("2099-12-31 23:59:59");
                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        // 获取原始返回值
//                        Object originalResult = param.getResult();
//                        Log.d("kong", "[HOOK] Api$getVip_end_time() 原始返回值: " + originalResult);
//                        param.setResult("2099-12-31 23:59:59");
//                        Object modifiedResult = param.getResult();
//                        Log.d("kong", "[HOOK] Api$getVip_end_time() 修改返回值: " + modifiedResult);
//                    }
                });
    }
    private static void WXLoginToken(ClassLoader cl){
        XposedHelpers.findAndHookMethod(
                "com.mt.copyidea.data.bean.api.WXLoginToken$Data",
                cl,
                "is_vip",
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("kong","[BEFORE] 即将调用 WXLoginToken$is_vip");
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod(
                "com.mt.copyidea.data.bean.api.WXLoginToken$Data",
                cl,
                "getVip_end",
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("kong","[BEFORE] 即将调用 WXLoginToken$getVip_end");
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }
}
