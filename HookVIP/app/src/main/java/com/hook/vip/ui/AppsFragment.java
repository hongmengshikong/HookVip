package com.hook.vip.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hook.vip.R;
import com.hook.vip.data.TargetApp;
import com.hook.vip.data.TargetCatalog;
import com.hook.vip.service.ModuleStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.libxposed.service.XposedService;

/**
 * 「应用」页：列出所有受支持的目标应用，每个一个开关。
 *
 * <p>开关就是 LSPosed 的作用域本身：打开 = 把该应用加入作用域，
 * 关闭 = 移出作用域。所以在 LSPosed 管理页面里勾过的应用，
 * 这里会自动是打开状态；默认（作用域为空）则全部不勾选。</p>
 */
public class AppsFragment extends Fragment implements ModuleStatus.Listener {

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    /** 作用域变更后等框架落库，再回读一次 */
    private static final long SCOPE_REFRESH_DELAY_MS = 700L;

    private final List<TargetApp> allApps = new ArrayList<>();
    private final List<TargetApp> visibleApps = new ArrayList<>();

    private AppAdapter adapter;
    private TextView countView;
    private TextView hintView;
    private TextView emptyView;
    private EditText searchInput;
    private String keyword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        countView = view.findViewById(R.id.apps_count);
        hintView = view.findViewById(R.id.apps_hint);
        emptyView = view.findViewById(R.id.empty);
        searchInput = view.findViewById(R.id.search_input);

        adapter = new AppAdapter(this::onToggle);
        RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 每次都是整表重绑，关掉默认动画，避免 notifyDataSetChanged 期间列表抖动
        list.setItemAnimator(null);
        list.setAdapter(adapter);

        allApps.clear();
        allApps.addAll(TargetCatalog.copy());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s == null ? "" : s.toString().trim();
                applyFilter();
            }
        });

        MaterialButton enableAll = view.findViewById(R.id.btn_enable_all);
        enableAll.setOnClickListener(v -> requestScope(TargetCatalog.packageNames(), true));
        MaterialButton disableAll = view.findViewById(R.id.btn_disable_all);
        disableAll.setOnClickListener(v -> requestScope(TargetCatalog.packageNames(), false));

        ModuleStatus.addListener(this);
        reloadScope();
        loadInstalledAsync();
    }

    @Override
    public void onDestroyView() {
        ModuleStatus.removeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onModuleStatusChanged() {
        if (isAdded()) {
            reloadScope();
        }
    }

    // ---------------------------------------------------------------- 作用域

    /** 从框架回读作用域，刷新所有开关 */
    private void reloadScope() {
        if (getContext() == null) {
            return;
        }
        Set<String> scope = new HashSet<>(ModuleStatus.scope());
        for (TargetApp app : allApps) {
            app.inScope = scope.contains(app.packageName);
        }
        if (hintView != null) {
            hintView.setText(ModuleStatus.isActivated()
                    ? R.string.apps_hint_active
                    : R.string.apps_hint_inactive);
        }
        applyFilter();
    }

    private void onToggle(TargetApp app, boolean checked) {
        requestScope(Collections.singletonList(app.packageName), checked);
    }

    /**
     * @param checked true = 加入作用域，false = 移出作用域
     */
    private void requestScope(List<String> packages, boolean checked) {
        if (!ModuleStatus.isActivated()) {
            toast(getString(R.string.apps_need_module));
            applyFilter();
            return;
        }

        if (!checked) {
            if (!ModuleStatus.removeScope(packages)) {
                toast(getString(R.string.apps_scope_failed));
                applyFilter();
                return;
            }
            toast(getString(R.string.apps_scope_removed, packages.size()));
            MAIN.postDelayed(this::reloadScope, SCOPE_REFRESH_DELAY_MS);
            return;
        }

        boolean accepted = ModuleStatus.requestScope(packages, new XposedService.OnScopeEventListener() {
            @Override
            public void onScopeRequestApproved(@NonNull List<String> approved) {
                toast(getString(R.string.apps_scope_added, approved.size()));
                MAIN.postDelayed(AppsFragment.this::reloadScope, SCOPE_REFRESH_DELAY_MS);
            }

            @Override
            public void onScopeRequestFailed(@NonNull String message) {
                toast(getString(R.string.apps_scope_request_failed, message));
                MAIN.postDelayed(AppsFragment.this::reloadScope, SCOPE_REFRESH_DELAY_MS);
            }
        });

        if (!accepted) {
            toast(getString(R.string.apps_scope_failed));
            applyFilter();
        }
    }

    private void toast(String text) {
        MAIN.post(() -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------------- 列表

    private void applyFilter() {
        visibleApps.clear();
        for (TargetApp app : allApps) {
            if (app.matches(keyword)) {
                visibleApps.add(app);
            }
        }
        if (adapter != null) {
            adapter.submit(visibleApps);
        }
        if (emptyView != null) {
            emptyView.setVisibility(visibleApps.isEmpty() ? View.VISIBLE : View.GONE);
        }
        updateCount();
    }

    private void updateCount() {
        if (countView == null) {
            return;
        }
        int checked = 0;
        for (TargetApp app : allApps) {
            if (app.inScope) {
                checked++;
            }
        }
        countView.setText(getString(R.string.apps_count_summary, visibleApps.size(), checked));
    }

    // ---------------------------------------------------------------- 安装信息

    /**
     * 在后台线程用 PackageManager 解析真实应用名 / 图标 / 版本号，
     * 解析结果攒成 map 后回到主线程一次性写回，避免主线程读到写了一半的对象。
     */
    private void loadInstalledAsync() {
        final Context appContext = requireContext().getApplicationContext();
        final List<String> packages = new ArrayList<>(TargetCatalog.packageNames());
        new Thread(() -> {
            PackageManager pm = appContext.getPackageManager();
            final Map<String, Object[]> resolved = new HashMap<>();
            for (String packageName : packages) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    resolved.put(packageName, new Object[]{
                            pm.getApplicationLabel(info).toString(),
                            pm.getApplicationIcon(info),
                            pm.getPackageInfo(packageName, 0).versionName
                    });
                } catch (Throwable ignored) {
                    // 未安装
                }
            }
            MAIN.post(() -> {
                if (!isAdded()) {
                    return;
                }
                for (TargetApp app : allApps) {
                    Object[] data = resolved.get(app.packageName);
                    if (data == null) {
                        app.installed = false;
                        continue;
                    }
                    app.installed = true;
                    app.resolvedLabel = (String) data[0];
                    app.icon = (Drawable) data[1];
                    app.versionName = (String) data[2];
                }
                applyFilter();
            });
        }, "app-info-loader").start();
    }
}
