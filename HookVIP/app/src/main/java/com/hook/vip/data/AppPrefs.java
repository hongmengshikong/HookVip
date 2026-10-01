package com.hook.vip.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 模块 App 的外观设置。
 *
 * <p>「哪些应用生效」这件事不在这里存 —— 它是 LSPosed 的作用域，
 * 真源在框架侧，界面直接读 {@code ModuleStatus.scope()}。</p>
 */
public final class AppPrefs {

    public static final int DARK_SYSTEM = 0;
    public static final int DARK_ON = 1;
    public static final int DARK_OFF = 2;

    private static final String FILE = "hook_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_MONET = "monet";

    private AppPrefs() {
    }

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int darkMode(Context context) {
        return sp(context).getInt(KEY_DARK_MODE, DARK_SYSTEM);
    }

    public static void setDarkMode(Context context, int mode) {
        sp(context).edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    public static boolean isMonet(Context context) {
        return sp(context).getBoolean(KEY_MONET, true);
    }

    public static void setMonet(Context context, boolean enabled) {
        sp(context).edit().putBoolean(KEY_MONET, enabled).apply();
    }
}
