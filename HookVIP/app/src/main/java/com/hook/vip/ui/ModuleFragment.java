package com.hook.vip.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.hook.vip.R;
import com.hook.vip.data.TargetApp;
import com.hook.vip.data.TargetCatalog;
import com.hook.vip.service.ModuleStatus;

import java.util.List;

/**
 * 「模块」页：模块激活状态、统计与环境信息。
 */
public class ModuleFragment extends Fragment implements ModuleStatus.Listener {

    private MaterialCardView statusCard;
    private TextView statusTitle;
    private TextView statusSummary;
    private TextView statEnabled;
    private TextView statInstalled;
    private TextView infoFramework;
    private TextView infoApi;
    private TextView infoScope;
    private TextView infoRunning;
    private TextView infoModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_module, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusCard = view.findViewById(R.id.status_card);
        statusTitle = view.findViewById(R.id.status_title);
        statusSummary = view.findViewById(R.id.status_summary);
        statEnabled = view.findViewById(R.id.stat_enabled_value);
        statInstalled = view.findViewById(R.id.stat_installed_value);
        infoFramework = view.findViewById(R.id.info_framework);
        infoApi = view.findViewById(R.id.info_api);
        infoScope = view.findViewById(R.id.info_scope);
        infoRunning = view.findViewById(R.id.info_running);
        infoModule = view.findViewById(R.id.info_module);

        MaterialButton refresh = view.findViewById(R.id.btn_refresh);
        refresh.setOnClickListener(v -> refresh());

        ModuleStatus.addListener(this);
        refresh();
    }

    @Override
    public void onDestroyView() {
        ModuleStatus.removeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onModuleStatusChanged() {
        if (isAdded()) {
            refresh();
        }
    }

    private void refresh() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        boolean activated = ModuleStatus.isActivated();
        List<String> scope = ModuleStatus.scope();

        if (!activated) {
            statusTitle.setText(R.string.module_state_inactive);
            statusSummary.setText(R.string.module_state_inactive_summary);
            statusCard.setCardBackgroundColor(MaterialColors.getColor(
                    statusCard, com.google.android.material.R.attr.colorSurfaceContainerHigh));
        } else if (scope.isEmpty()) {
            statusTitle.setText(R.string.module_state_scope_empty);
            statusSummary.setText(R.string.module_state_scope_empty_summary);
            statusCard.setCardBackgroundColor(MaterialColors.getColor(
                    statusCard, com.google.android.material.R.attr.colorPrimaryContainer));
        } else {
            statusTitle.setText(R.string.module_state_active);
            statusSummary.setText(R.string.module_state_ready_summary);
            statusCard.setCardBackgroundColor(MaterialColors.getColor(
                    statusCard, com.google.android.material.R.attr.colorPrimaryContainer));
        }

        // 统计：已勾选 = 作用域里属于本模块支持范围的应用
        java.util.Set<String> scopeSet = new java.util.HashSet<>(scope);
        int enabled = 0;
        for (TargetApp app : TargetCatalog.all()) {
            if (scopeSet.contains(app.packageName)) {
                enabled++;
            }
        }
        statEnabled.setText(String.valueOf(enabled));
        statInstalled.setText(String.valueOf(installedCount(context)));

        // 环境信息
        String unknown = getString(R.string.info_unknown);
        String framework = ModuleStatus.frameworkName();
        String frameworkVersion = ModuleStatus.frameworkVersion();
        String frameworkText = framework == null
                ? unknown
                : framework + (frameworkVersion != null && !frameworkVersion.isEmpty()
                        ? " " + frameworkVersion : "");
        infoFramework.setText(getString(R.string.info_framework, frameworkText));

        int api = ModuleStatus.apiVersion();
        infoApi.setText(getString(R.string.info_api, api < 0 ? unknown : String.valueOf(api)));
        infoScope.setText(getString(R.string.info_scope, scope.size()));

        int running = ModuleStatus.runningTargetCount();
        infoRunning.setText(getString(R.string.info_running, running < 0 ? unknown : String.valueOf(running)));
        infoModule.setText(getString(R.string.info_module, moduleVersion(context)));
    }

    private int installedCount(Context context) {
        int count = 0;
        for (TargetApp app : TargetCatalog.all()) {
            try {
                context.getPackageManager().getApplicationInfo(app.packageName, 0);
                count++;
            } catch (Throwable ignored) {
                // 未安装
            }
        }
        return count;
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
