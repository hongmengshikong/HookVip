package com.hook.vip.app;

import android.util.Log;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WeightAppHooker {

    static {
        System.loadLibrary("dexkit");
    }

    private final ClassLoader hostClassLoader;

    public WeightAppHooker(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.hostClassLoader = loadPackageParam.classLoader;
        String apkPath = loadPackageParam.appInfo.sourceDir;

        try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
            hookUserSettingsKV_e(bridge);
            hookUserSettingsKV_n(bridge);
        } catch (Throwable t) {
            Log.e("kong", "DexKit 初始化失败", t);
        }
    }

    private void hookUserSettingsKV_e(DexKitBridge bridge) {
        try {
            MethodData methodData = bridge.findMethod(
                    FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                                    .paramCount(0)
                                    .returnType("int")
                                    .usingStrings(List.of("user_role"), StringMatchType.Contains)
                            )
            ).single();

            Method method = methodData.getMethodInstance(hostClassLoader);
            XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(1)); // VIP 永久生效
            Log.d("kong", "Hook UserSettingsKV.e() 成功");
        } catch (Exception e) {
            Log.e("kong", "没有找到 UserSettingsKV.e() 方法", e);
        }
    }

    private void hookUserSettingsKV_n(DexKitBridge bridge) {
        try {
            MethodData methodData = bridge.findMethod(
                    FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                                    .paramCount(1)
                                    .paramTypes("int")
                                    .usingStrings(List.of("user_role"), StringMatchType.Contains)
                            )
            ).single();

            Method method = methodData.getMethodInstance(hostClassLoader);
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null); // 阻止写入
                    Log.d("kong", "阻止 UserSettingsKV.n(int) 写入 user_role");
                }
            });
            Log.d("kong", "Hook UserSettingsKV.n(int) 成功");
        } catch (Exception e) {
            Log.e("kong", "没有找到 UserSettingsKV.n(int) 方法", e);
        }
    }
}