package com.hook.vip.util;

import android.util.Log;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;

/**
 * 现代 libxposed API（LSPosed API 102）的入口工具。
 *
 * 旧的 {@code XposedHelpers} / {@code XposedBridge} / {@code XC_MethodHook} 在 API 102
 * 里被框架明确禁止调用，所有 hook 都必须走 {@code XposedInterface#hook(...).intercept(...)}。
 *
 * HookEntry 在 {@code onModuleLoaded()} 里把 XposedModule 实例交给这里，各个 AppHooker
 * 就能直接注册 hook / 打日志，不必把 XposedInterface 层层传参（保持原有静态工具类的写法）。
 */
public final class XposedUtil {

    /** 沿用原有 logcat tag，方便按老习惯过滤日志 */
    public static final String TAG = "kong";

    private static volatile XposedInterface xposed;

    private XposedUtil() {
    }

    /** 由 HookEntry 调用一次；必须在任何 hook 之前执行。 */
    public static void attach(XposedInterface instance) {
        xposed = instance;
    }

    public static XposedInterface get() {
        return xposed;
    }

    public static boolean isReady() {
        return xposed != null;
    }

    public static void d(String msg) {
        log(Log.DEBUG, msg, null);
    }

    public static void e(String msg, Throwable tr) {
        log(Log.ERROR, msg, tr);
    }

    public static void log(int priority, String msg, Throwable tr) {
        XposedInterface instance = xposed;
        if (instance == null) {
            // 框架未 attach 时退回普通 logcat，避免因为日志把 hook 流程打断
            if (tr == null) {
                Log.println(priority, TAG, msg);
            } else {
                Log.println(priority, TAG, msg + '\n' + Log.getStackTraceString(tr));
            }
            return;
        }
        instance.log(priority, TAG, msg, tr);
    }

    /**
     * 注册一个 hook。
     *
     * @param origin 目标方法 / 构造器
     * @param id     稳定的 hook id（API 102 特性，重复注册会原子替换旧 hook）；可为 null
     * @param hooker 拦截逻辑，对应过去的 {@code XC_MethodHook}
     */
    public static void hook(Executable origin, String id, XposedInterface.Hooker hooker) {
        XposedInterface instance = xposed;
        if (instance == null) {
            throw new IllegalStateException("XposedInterface 尚未 attach，无法 hook " + origin);
        }
        XposedInterface.HookBuilder builder = instance.hook(origin)
                // hook 内部抛异常时只记日志并放行原方法，避免把目标 App 弄崩
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE);
        if (id != null) {
            builder.setId(id);
        }
        builder.intercept(hooker);
    }

    /** 直接把方法返回值固定成某个常量（等价于旧的 {@code XC_MethodReplacement.returnConstant}）。 */
    public static void hookReturn(Executable origin, String id, Object value) {
        Class<?> returnType = origin instanceof java.lang.reflect.Method
                ? ((java.lang.reflect.Method) origin).getReturnType()
                : void.class;
        Object fixed = ReflectUtil.coerce(returnType, value);
        hook(origin, id, chain -> fixed);
    }
}
