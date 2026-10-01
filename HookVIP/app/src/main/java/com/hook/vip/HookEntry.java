package com.hook.vip;

import android.os.Handler;
import android.os.Looper;

import com.hook.vip.app.EndorserAppHooker;
import com.hook.vip.app.LRJKAppHooker;
import com.hook.vip.app.QDiaryAppHooker;
import com.hook.vip.app.TomatoAppHooker;
import com.hook.vip.app.TomatoFlashlightAppHooker;
import com.hook.vip.app.WeightAppHooker;
import com.hook.vip.app.YIYanAppHooker;
import com.hook.vip.app.YiMuBillAppHooker;
import com.hook.vip.app.YiNianAppHooker;
import com.hook.vip.app.YimuListAppHooker;
import com.hook.vip.util.UniversalRealClassLoaderUtil;
import com.hook.vip.util.XposedUtil;

import java.util.function.Consumer;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

/**
 * Xposed 入口（由 {@code META-INF/xposed/java_init.list} 指定）。
 *
 * 现代 libxposed API（LSPosed API 102）：
 *   入口继承 {@link XposedModule}，框架会先调用 {@code attachFramework(XposedInterface)}，
 *   因此 {@code onModuleLoaded()} 之后才能使用 hook / log 等接口。
 *
 * 生命周期回调对应关系：
 *   旧版 IXposedHookLoadPackage#handleLoadPackage  ->  onPackageReady（拿到最终 ClassLoader）
 */
public class HookEntry extends XposedModule {

    /** Application.attach 迟迟没给出真实 ClassLoader 时的兜底等待时间 */
    private static final long FALLBACK_DELAY_MS = 3000L;

    // ---------------- 目标包名 ----------------
    private static final String PKG_WEIGHT = "com.kproduce.weight";
    private static final String PKG_TOMATO = "com.swhh.fasting.tomato";
    private static final String PKG_TOMATO_FLASHLIGHT = "com.qyxy.tomato.android";
    private static final String PKG_LRJK = "com.jx885.lrjk";
    private static final String PKG_YINIAN = "com.mt.copyidea";
    private static final String PKG_ENDORSER = "com.zzdbwku.zizbnea";
    private static final String PKG_YIMU_LIST = "com.wangc.todolist";
    private static final String PKG_YIYAN = "com.jhyan.yan";
    private static final String PKG_YIMU_BILL = "com.wangc.bill";
    private static final String PKG_QDIARY = "com.slfteam.qdiary";

    private boolean attached = false;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        // 先把 XposedInterface 交给工具类，后面的 hook / log 都依赖它
        XposedUtil.attach(this);
        XposedUtil.log(android.util.Log.INFO,
                "模块已加载: " + param.getProcessName()
                        + "  framework=" + getFrameworkName() + " " + getFrameworkVersion()
                        + "  api=" + getApiVersion(),
                null);
    }

    /**
     * 包已就绪（AppComponentFactory 已创建最终 ClassLoader）。
     * 这是官方推荐的 Hook 时机，对加固应用拿到的才是真实 ClassLoader。
     */
    @Override
    public void onPackageReady(PackageReadyParam param) {
        String packageName = param.getPackageName();
        ClassLoader classLoader = param.getClassLoader();

        try {
            switch (packageName) {
                // 体重日记：DexKit 定位方法，无需等待真实 ClassLoader
                case PKG_WEIGHT:
                    XposedUtil.d("加载目标包：" + packageName);
                    new WeightAppHooker(classLoader, param.getApplicationInfo().sourceDir);
                    break;

                // 番茄轻断食
                case PKG_TOMATO:
                    withRealClassLoader(packageName, classLoader, TomatoAppHooker::hook);
                    break;

                // 番茄闪轻
                case PKG_TOMATO_FLASHLIGHT:
                    withRealClassLoader(packageName, classLoader, TomatoFlashlightAppHooker::hook);
                    break;

                // 懒人驾考
                case PKG_LRJK:
                    withRealClassLoader(packageName, classLoader, LRJKAppHooker::hook);
                    break;

                // 一念
                case PKG_YINIAN:
                    XposedUtil.d("加载目标包：" + packageName);
                    new YiNianAppHooker(classLoader);
                    break;

                // 背书匠(360加固)
                case PKG_ENDORSER:
                    withRealClassLoader(packageName, classLoader, EndorserAppHooker::hook);
                    break;

                // 一木清单(无加固)
                case PKG_YIMU_LIST:
                    XposedUtil.d("加载目标包：" + packageName);
                    new YimuListAppHooker(classLoader);
                    break;

                // 一言(360加固)
                case PKG_YIYAN:
                    withRealClassLoader(packageName, classLoader, YIYanAppHooker::hook);
                    break;

                // 一木记账(360加固)
                case PKG_YIMU_BILL:
                    withRealClassLoader(packageName, classLoader, YiMuBillAppHooker::hook);
                    break;

                // Q日记
                case PKG_QDIARY:
                    XposedUtil.d("加载目标包：" + packageName);
                    new QDiaryAppHooker(classLoader);
                    break;

                default:
                    // 不在作用域内，直接结束（也可调用 detach() 停止后续回调）
                    return;
            }
        } catch (Throwable t) {
            XposedUtil.e("初始化 " + packageName + " 的 Hook 失败: " + t, t);
        }
    }

    /**
     * 等待 {@code Application.attach} 给出真实 ClassLoader（通杀加固），
     * 再执行 install。兜底：3 秒内没等到就用包自身的 ClassLoader。
     */
    private void withRealClassLoader(String packageName, ClassLoader fallback, Consumer<ClassLoader> install) {
        XposedUtil.d("加载目标包：" + packageName);
        UniversalRealClassLoaderUtil.init();
        UniversalRealClassLoaderUtil.onReady(() -> {
            ClassLoader real = UniversalRealClassLoaderUtil.getRealClassLoader();
            XposedUtil.d("准备 Hook " + packageName + "，真实 ClassLoader: " + real);
            install.accept(real);
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!UniversalRealClassLoaderUtil.isReady()) {
                UniversalRealClassLoaderUtil.adopt(fallback);
            }
        }, FALLBACK_DELAY_MS);
    }
}
