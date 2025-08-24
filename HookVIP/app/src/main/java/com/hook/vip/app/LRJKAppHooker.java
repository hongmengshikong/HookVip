package com.hook.vip.app;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class LRJKAppHooker {
    public static void hook(ClassLoader cl) {
        hookSplashAds(cl);

        //免登陆
        XposedHelpers.findAndHookMethod("a8.e", cl, "k0", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(true);
            }
        });
        //永久会员
        XposedHelpers.findAndHookMethod("a8.e",
                cl,
                "p0",
                int.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(true);
            }
        });
        //解锁速记技巧
        XposedHelpers.findAndHookMethod("com.tencent.mmkv.MMKV",
                cl,
                "decodeBool",
                "java.lang.String",
                boolean.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(true);
            }
        });
        //解锁我的权益页面全部权益显示
        XposedHelpers.findAndHookMethod("com.jx885.lrjk.cg.model.vo.VipProfileVo",
                cl,
                "isLocked",
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(false);
            }
        });

    }
    private static void hookSplashAds(ClassLoader cl) {
        try {
            // 阻止广告请求
            XposedHelpers.findAndHookMethod(
                    "com.anythink.splashad.api.ATSplashAd",
                    cl,
                    "loadAd",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            Log.d("kong", "拦截 ATSplashAd.loadAd()");
                            return null;
                        }
                    }
            );

            // 阻止广告展示
            XposedHelpers.findAndHookMethod(
                    "com.anythink.splashad.api.ATSplashAd",
                    cl,
                    "show",
                    android.app.Activity.class,
                    android.view.ViewGroup.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            Log.d("kong", "拦截 ATSplashAd.show()");
                            return null;
                        }
                    }
            );

            // 阻止闪屏逻辑（秒进主页）
            XposedHelpers.findAndHookMethod(
                    "com.jx885.lrjk.cg.ui.SplashActivity",
                    cl,
                    "Q0",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            Log.d("kong", "拦截 SplashActivity.Q0()");
                            return null;
                        }
                    }
            );

            Log.d("kong", "hookSplashAds 初始化完成");

        } catch (Throwable t) {
            Log.e("kong", "hookSplashAds 出错: " + t);
        }
    }
}
