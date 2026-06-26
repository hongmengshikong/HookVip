package com.hook.vip.app;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QDiaryAppHooker {
    public QDiaryAppHooker(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hook(loadPackageParam.classLoader);
    }

    private static void hook(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod(
                "com.slfteam.slib.account.SUsrAcc",
                classLoader,
                "isVip",
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                boolean result= (boolean) param.getResult();
                Log.d("kong", "hook前"+result);
            }
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                boolean result= (boolean) param.getResult();
                Log.d("kong", "hook前"+result);
                param.setResult(true);
                boolean result2= (boolean) param.getResult();
                Log.d("kong", "hook后"+result2);
            }
        });
        XposedHelpers.findAndHookMethod(
                "com.slfteam.slib.account.SUsrAcc",
                classLoader,
                "vipExpired",
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                boolean result= (boolean) param.getResult();
                Log.d("kong", "hook前"+result);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                boolean result= (boolean) param.getResult();
                Log.d("kong", "hook前"+result);
                param.setResult(false);
                boolean result2= (boolean) param.getResult();
                Log.d("kong", "hook后"+result2);
            }
        });


    }

}
