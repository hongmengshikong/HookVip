package com.hook.vip;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hook.vip.data.AppPrefs;
import com.hook.vip.ui.AppsFragment;
import com.hook.vip.ui.ModuleFragment;
import com.hook.vip.ui.SettingsFragment;

/**
 * 底部三 Tab 外壳：应用 / 模块 / 设置。
 * 参考 Rain_1.1.4 的信息架构，用 Material 3 组件实现。
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_TAB = "selected_tab";

    private Toolbar toolbar;
    private int currentTab = R.id.tab_apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 深色模式要在 setContentView 之前定下来
        applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        int tab = savedInstanceState != null ? savedInstanceState.getInt(STATE_TAB, R.id.tab_apps) : R.id.tab_apps;
        if (bottomNav.getSelectedItemId() == tab) {
            showTab(tab);
        } else {
            bottomNav.setSelectedItemId(tab);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, currentTab);
    }

    private void showTab(int itemId) {
        currentTab = itemId;

        Fragment fragment;
        int title;
        if (itemId == R.id.tab_module) {
            fragment = new ModuleFragment();
            title = R.string.tab_module;
        } else if (itemId == R.id.tab_settings) {
            fragment = new SettingsFragment();
            title = R.string.tab_settings;
        } else {
            fragment = new AppsFragment();
            title = R.string.tab_apps;
        }

        toolbar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void applyDarkMode() {
        int mode = AppPrefs.darkMode(this);
        int nightMode;
        if (mode == AppPrefs.DARK_ON) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (mode == AppPrefs.DARK_OFF) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
