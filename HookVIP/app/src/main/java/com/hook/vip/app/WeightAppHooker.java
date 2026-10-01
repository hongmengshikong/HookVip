package com.hook.vip.app;

import com.hook.vip.util.XposedUtil;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 体重日记（com.kproduce.weight）的 hook。
 *
 * 方法定位仍然依赖 DexKit；注册 hook 的部分已迁移到 libxposed API 102。
 */
public class WeightAppHooker {

    static {
        System.loadLibrary("dexkit");
    }

    private final ClassLoader hostClassLoader;

    public WeightAppHooker(ClassLoader hostClassLoader, String apkPath) {
        this.hostClassLoader = hostClassLoader;

        try (DexKitBridge bridge = DexKitBridge.create(apkPath)) {
            hookUserSettingsKV_e(bridge);
            hookUserSettingsKV_n(bridge);
        } catch (Throwable t) {
            XposedUtil.e("DexKit 初始化失败", t);
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
            // VIP 永久生效（等价于旧的 XC_MethodReplacement.returnConstant(1)）
            XposedUtil.hookReturn(method, "weight_user_settings_e", 1);
            XposedUtil.d("Hook UserSettingsKV.e() 成功");
        } catch (Throwable e) {
            XposedUtil.e("没有找到 UserSettingsKV.e() 方法", e);
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
            XposedUtil.hook(method, "weight_user_settings_n", chain -> {
                // 阻止写入：直接丢弃这次调用
                XposedUtil.d("阻止 UserSettingsKV.n(int) 写入 user_role");
                return null;
            });
            XposedUtil.d("Hook UserSettingsKV.n(int) 成功");
        } catch (Throwable e) {
            XposedUtil.e("没有找到 UserSettingsKV.n(int) 方法", e);
        }
    }
}
