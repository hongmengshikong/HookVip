package com.hook.vip.service;

import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.libxposed.service.HookedTarget;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/**
 * 模块状态与作用域：直接问 libxposed 框架，不靠猜。
 *
 *   service != null             -> 框架在运行，且认识本模块（= 模块已激活）
 *   service.getScope()          -> 本模块的作用域（界面上的勾选状态就是它）
 *   service.getRunningTargets() -> 当前真正被本模块 Hook 的进程（service API 102）
 */
public final class ModuleStatus {

    public interface Listener {
        void onModuleStatusChanged();
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final CopyOnWriteArrayList<Listener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile XposedService service;
    private static volatile boolean initialised = false;

    private ModuleStatus() {
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        try {
            XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
                @Override
                public void onServiceBind(XposedService s) {
                    service = s;
                    notifyChanged();
                }

                @Override
                public void onServiceDied(XposedService s) {
                    if (service == s) {
                        service = null;
                    }
                    notifyChanged();
                }
            });
        } catch (Throwable ignored) {
            // 框架不支持 service：界面按“未激活”展示
        }
    }

    /** 当前绑定的框架 service，未激活时为 null */
    public static XposedService currentService() {
        return service;
    }

    public static void addListener(Listener listener) {
        LISTENERS.addIfAbsent(listener);
    }

    public static void removeListener(Listener listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyChanged() {
        MAIN.post(() -> {
            for (Listener listener : LISTENERS) {
                try {
                    listener.onModuleStatusChanged();
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /** 框架是否已经连上本模块 */
    public static boolean isActivated() {
        return service != null;
    }

    public static String frameworkName() {
        XposedService s = service;
        if (s == null) {
            return null;
        }
        try {
            return s.getFrameworkName();
        } catch (Throwable t) {
            return null;
        }
    }

    public static String frameworkVersion() {
        XposedService s = service;
        if (s == null) {
            return null;
        }
        try {
            return s.getFrameworkVersion();
        } catch (Throwable t) {
            return null;
        }
    }

    public static int apiVersion() {
        XposedService s = service;
        if (s == null) {
            return -1;
        }
        try {
            return s.getApiVersion();
        } catch (Throwable t) {
            return -1;
        }
    }

    /** 当前作用域；模块未激活时返回空列表（= 界面上全部不勾选） */
    public static List<String> scope() {
        XposedService s = service;
        if (s == null) {
            return Collections.emptyList();
        }
        try {
            List<String> scope = s.getScope();
            return scope == null ? Collections.emptyList() : scope;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    public static boolean scopeContains(String packageName) {
        return scope().contains(packageName);
    }

    /**
     * 当前正在被本模块 Hook 的进程数量。
     *
     * @return 进程数；框架不支持 service API 102 或查询失败时返回 -1
     */
    public static int runningTargetCount() {
        XposedService s = service;
        if (s == null) {
            return -1;
        }
        try {
            List<HookedTarget> targets = s.getRunningTargets();
            return targets == null ? -1 : targets.size();
        } catch (Throwable t) {
            // 框架不支持 getRunningTargets（service API < 102）
            return -1;
        }
    }

    /**
     * 请求把包名加入作用域。
     *
     * @return false 表示模块没激活 / 框架不支持，调用方应该把开关还原
     */
    public static boolean requestScope(List<String> packages, XposedService.OnScopeEventListener callback) {
        XposedService s = service;
        if (s == null) {
            return false;
        }
        try {
            s.requestScope(packages, callback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 把包名移出作用域（会让目标应用不再被 Hook；需要重启目标应用才彻底生效）。
     *
     * @return false 表示模块没激活 / 框架不支持
     */
    public static boolean removeScope(List<String> packages) {
        XposedService s = service;
        if (s == null) {
            return false;
        }
        try {
            s.removeScope(packages);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
