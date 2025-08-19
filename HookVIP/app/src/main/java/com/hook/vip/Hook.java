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
        if("com.swhh.fasting.tomato".equals(loadPackageParam.packageName)){
            Log.d("kong", "加载目标包：" + loadPackageParam.packageName);
            try {
                XposedHelpers.findAndHookMethod("com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                        loadPackageParam.classLoader,
                        "getViptype",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);

                                Log.d("kong","[BEFORE] 即将调用 getViptype");

                            }
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                // 获取原始返回值
                                Object originalResult = param.getResult();
                                String original = originalResult != null ? (String) originalResult : "null";

                                Log.d("kong", "[HOOK] getViptype() 原始返回值: " + original);

                                // 强制修改为 "4"（终身会员）
                                param.setResult("4");
                                Log.d("kong", "[HOOK] 已将返回值修改为: 4");

                                // 再次确认结果
                                Log.d("kong", "[HOOK] 修改后返回值: " + param.getResult());
                            }
                        });
                // ✅ Hook 注册成功提示
                Log.d("kong", "✅ 成功注册 Hook: LoginResponse$UserRichBean.getViptype()");
            } catch (Exception e) {
                Log.d("kong", "❌ Hook 失败: " + e.getMessage(), e); // 使用 Log.e 并打印堆栈
            }
            try {
                XposedHelpers.findAndHookMethod("com.swhh.fasting.tomato.mvvm.model.LoginResponse$UserRichBean",
                        loadPackageParam.classLoader,
                        "getIsvalidvip",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                Log.d("kong", "[HOOK] 即将调用 getIsvalidvip()");
                            }
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                Object originalResult = param.getResult();
                                String original = originalResult != null ? originalResult.toString() : "null";

                                Log.d("kong", "[HOOK] getIsvalidvip() 原始返回值: " + original);

                                // ✅ 强制修改为 "1"（有效 VIP）
                                param.setResult("1");

                                Log.d("kong", "[HOOK] 已将 getIsvalidvip() 返回值修改为: 1");
                                Log.d("kong", "[HOOK] 修改后结果: " + param.getResult());
                            }
                        });
                // ✅ Hook 注册成功提示
                Log.d("kong", "✅ 成功注册 Hook: LoginResponse$UserRichBean.getIsvalidvip()");
            }catch (Exception e){
                Log.d("kong", "❌ Hook 失败: " + e.getMessage(), e); // 使用 Log.e 并打印堆栈
            }
        }
    }
}