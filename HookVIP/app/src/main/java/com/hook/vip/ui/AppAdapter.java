package com.hook.vip.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.hook.vip.R;
import com.hook.vip.data.TargetApp;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用列表：图标 + 名称 + 包名/适配信息 + 开关（开关即 LSPosed 作用域）。
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    public interface OnToggleListener {
        void onToggle(TargetApp app, boolean checked);
    }

    private final List<TargetApp> items = new ArrayList<>();
    private final OnToggleListener toggleListener;

    public AppAdapter(OnToggleListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void submit(List<TargetApp> apps) {
        items.clear();
        items.addAll(apps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(items.get(position), toggleListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView title;
        private final TextView subtitle;
        private final TextView badgeInstalled;
        private final MaterialSwitch switchView;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_icon);
            title = itemView.findViewById(R.id.item_title);
            subtitle = itemView.findViewById(R.id.item_subtitle);
            badgeInstalled = itemView.findViewById(R.id.item_badge_installed);
            switchView = itemView.findViewById(R.id.item_switch);
        }

        void bind(TargetApp app, OnToggleListener listener) {
            Context context = itemView.getContext();

            title.setText(app.resolvedLabel != null ? app.resolvedLabel : app.displayName);

            StringBuilder sub = new StringBuilder(app.packageName);
            if (app.note != null && !app.note.isEmpty()) {
                sub.append(" · ").append(app.note);
            } else if (app.testedVersion != null && !app.testedVersion.isEmpty()) {
                sub.append(" · ").append(context.getString(R.string.app_tested, app.testedVersion));
            }
            subtitle.setText(sub.toString());

            if (app.icon != null) {
                icon.setImageDrawable(app.icon);
            } else {
                icon.setImageResource(R.drawable.ic_tab_apps);
            }

            if (app.installed) {
                badgeInstalled.setText(app.versionName != null && !app.versionName.isEmpty()
                        ? context.getString(R.string.app_version, app.versionName)
                        : context.getString(R.string.app_installed));
                badgeInstalled.setTextColor(MaterialColors.getColor(itemView,
                        com.google.android.material.R.attr.colorOnSurfaceVariant));
            } else {
                badgeInstalled.setText(R.string.app_not_installed);
                badgeInstalled.setTextColor(ContextCompat.getColor(context, R.color.state_warn));
            }

            // 绑定期间不要触发监听，否则会把状态又写回去
            switchView.setOnCheckedChangeListener(null);
            switchView.setChecked(app.inScope);
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(app, isChecked);
                }
            });

            itemView.setOnClickListener(v -> launch(context, app));
        }

        private void launch(Context context, TargetApp app) {
            if (!app.installed) {
                return;
            }
            try {
                PackageManager pm = context.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(app.packageName);
                if (intent == null) {
                    Toast.makeText(context,
                            context.getString(R.string.app_launch_failed, app.displayName),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Throwable t) {
                Toast.makeText(context,
                        context.getString(R.string.app_launch_failed, app.displayName),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
