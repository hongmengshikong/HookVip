package com.hook.vip.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 模块支持的目标应用目录。
 *
 * <p>注意：这里的包名必须和 {@code app/src/main/resources/META-INF/xposed/scope.list}
 * 以及 {@code HookEntry} 里的分发逻辑保持一致，新增应用时三处一起改。</p>
 */
public final class TargetCatalog {

    private static final List<TargetApp> APPS = Collections.unmodifiableList(Arrays.asList(
            new TargetApp("com.kproduce.weight", "体重日记", "3.5.3"),
            new TargetApp("com.swhh.fasting.tomato", "番茄轻断食", "3.4.7"),
            new TargetApp("com.qyxy.tomato.android", "番茄闪轻", "2.1.7"),
            new TargetApp("com.jx885.lrjk", "懒人驾考", "1.x", "已停止维护"),
            new TargetApp("com.mt.copyidea", "一念", "2.4.0"),
            new TargetApp("com.zzdbwku.zizbnea", "背书匠", "2.2.5"),
            new TargetApp("com.wangc.todolist", "一木清单", "2.4.3"),
            new TargetApp("com.jhyan.yan", "一言", "5.1.0"),
            new TargetApp("com.wangc.bill", "一木记账", "6.5.5"),
            new TargetApp("com.slfteam.qdiary", "Q日记", "1.7.81")
    ));

    private TargetCatalog() {
    }

    /** 全部受支持的应用（不可变） */
    public static List<TargetApp> all() {
        return APPS;
    }

    /** 每次调用都返回一份独立的可变副本，供界面持有 */
    public static List<TargetApp> copy() {
        return new ArrayList<>(APPS);
    }

    public static List<String> packageNames() {
        List<String> names = new ArrayList<>(APPS.size());
        for (TargetApp app : APPS) {
            names.add(app.packageName);
        }
        return names;
    }

    public static boolean contains(String packageName) {
        for (TargetApp app : APPS) {
            if (app.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
