package com.hook.vip.data;

import android.graphics.drawable.Drawable;

/**
 * 一个受支持的目标应用。
 */
public final class TargetApp {

    /** 包名，和 {@code META-INF/xposed/scope.list} 一一对应 */
    public final String packageName;
    /** 应用的中文名（未安装时也用它兜底展示） */
    public final String displayName;
    /** 已验证/适配的版本说明 */
    public final String testedVersion;
    /** 额外备注，可为 null */
    public final String note;

    /** 运行期补充信息 */
    public boolean installed;
    public String versionName;
    public Drawable icon;
    /** PackageManager 解析出来的真实应用名，未安装时为 null */
    public String resolvedLabel;

    /**
     * 是否在 LSPosed 作用域里 —— 这就是界面上那个开关的状态，
     * 也是模块会不会被加载进该应用进程的唯一依据。
     */
    public boolean inScope;

    public TargetApp(String packageName, String displayName, String testedVersion) {
        this(packageName, displayName, testedVersion, null);
    }

    public TargetApp(String packageName, String displayName, String testedVersion, String note) {
        this.packageName = packageName;
        this.displayName = displayName;
        this.testedVersion = testedVersion;
        this.note = note;
    }

    public boolean matches(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        String lower = keyword.toLowerCase();
        return displayName.toLowerCase().contains(lower)
                || packageName.toLowerCase().contains(lower)
                || (resolvedLabel != null && resolvedLabel.toLowerCase().contains(lower));
    }
}
