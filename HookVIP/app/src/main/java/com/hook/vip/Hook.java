package com.hook.vip;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if(loadPackageParam.packageName.equals("com.kproduce.weight")){
            try {
                XposedHelpers.findAndHookMethod("oe1", loadPackageParam.classLoader, "e", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.d("kong","[BEFORE] 即将调用 oe1.e()");
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Object originalResult = param.getResult();
                        Log.d("kong","[AFTER] oe1.e() 原始返回值: " + originalResult);
                        param.setResult(1); // 强制返回 1 (VIP)
                        Object modifiedResult = param.getResult();
                        Log.d("kong","[AFTER] oe1.e() 已被修改，新返回值: " + modifiedResult);
                    }
                });
            } catch (Exception e) {
                Log.d("kong", "❌ Hook 失败: " + e.getMessage(), e); // 使用 Log.e 并打印堆栈
            }
        }
    }
}