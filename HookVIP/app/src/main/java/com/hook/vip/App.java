package com.hook.vip;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.hook.vip.data.AppPrefs;
import com.hook.vip.service.ModuleStatus;

/**
 * 应用入口：按设置决定是否启用 Material You 动态取色，并连上 libxposed service。
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 先连框架：界面上的勾选状态就是框架的作用域
        ModuleStatus.init();
        if (AppPrefs.isMonet(this)) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
    }
}
