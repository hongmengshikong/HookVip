package com.hook.vip.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.hook.vip.R;
import com.hook.vip.data.AppPrefs;
import com.hook.vip.service.ModuleStatus;

/**
 * 「设置」页：外观 / 行为 / 关于。
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = requireContext();

        // ---------------- 深色模式 ----------------
        RadioGroup darkModeGroup = view.findViewById(R.id.group_dark_mode);
        int mode = AppPrefs.darkMode(context);
        if (mode == AppPrefs.DARK_ON) {
            darkModeGroup.check(R.id.mode_on);
        } else if (mode == AppPrefs.DARK_OFF) {
            darkModeGroup.check(R.id.mode_off);
        } else {
            darkModeGroup.check(R.id.mode_system);
        }
        darkModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int value = checkedId == R.id.mode_on ? AppPrefs.DARK_ON
                    : checkedId == R.id.mode_off ? AppPrefs.DARK_OFF
                    : AppPrefs.DARK_SYSTEM;
            if (value == AppPrefs.darkMode(context)) {
                return;
            }
            AppPrefs.setDarkMode(context, value);
            // AppCompatDelegate 会自己重建 Activity
            AppCompatDelegate.setDefaultNightMode(value == AppPrefs.DARK_ON
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : value == AppPrefs.DARK_OFF
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });

        // ---------------- 动态取色 ----------------
        MaterialSwitch monet = view.findViewById(R.id.switch_monet);
        monet.setChecked(AppPrefs.isMonet(context));
        monet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == AppPrefs.isMonet(context)) {
                return;
            }
            AppPrefs.setMonet(context, isChecked);
            requireActivity().recreate();
        });

        // ---------------- 关于 ----------------
        TextView aboutModule = view.findViewById(R.id.about_module);
        aboutModule.setText(getString(R.string.settings_version) + "："
                + moduleVersion(context));

        TextView aboutFramework = view.findViewById(R.id.about_framework);
        String framework = ModuleStatus.frameworkName();
        String version = ModuleStatus.frameworkVersion();
        int api = ModuleStatus.apiVersion();
        aboutFramework.setText(getString(R.string.settings_framework) + "："
                + (framework != null ? framework : getString(R.string.info_unknown))
                + " " + (version != null ? version : "")
                + (api >= 0 ? " · API " + api : ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) {
            return;
        }
        // 回到设置页时刷新框架信息（模块可能刚被激活）
        TextView aboutFramework = view.findViewById(R.id.about_framework);
        String framework = ModuleStatus.frameworkName();
        String version = ModuleStatus.frameworkVersion();
        int api = ModuleStatus.apiVersion();
        aboutFramework.setText(getString(R.string.settings_framework) + "："
                + (framework != null ? framework : getString(R.string.info_unknown))
                + " " + (version != null ? version : "")
                + (api >= 0 ? " · API " + api : ""));
    }

    private String moduleVersion(Context context) {
        try {
            String version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            return version != null ? version : getString(R.string.info_unknown);
        } catch (Throwable t) {
            return getString(R.string.info_unknown);
        }
    }
}
