package za.ac.spu.sacns.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.models.DashboardModule;

public class DashboardModuleAdapter extends RecyclerView.Adapter<DashboardModuleAdapter.ViewHolder> {

    public interface OnModuleClickListener {
        void onModuleClick(DashboardModule module);
    }

    private final Context context;
    private final List<DashboardModule> moduleList;
    private final OnModuleClickListener listener;

    public DashboardModuleAdapter(Context context, List<DashboardModule> moduleList, OnModuleClickListener listener) {
        this.context = context;
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardModule module = moduleList.get(position);
        if (module.getIconResId() != 0) {
            holder.ivIcon.setImageResource(module.getIconResId());
        }
        holder.tvTitle.setText(module.getTitle());
        holder.tvDesc.setText(module.getDescription());
        holder.tvBadge.setText(module.getBadgeText());

        holder.cardModule.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModuleClick(module);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleList != null ? moduleList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardModule;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardModule = itemView.findViewById(R.id.cardDashboardModule);
            ivIcon = itemView.findViewById(R.id.ivModuleIcon);
            tvTitle = itemView.findViewById(R.id.tvModuleTitle);
            tvDesc = itemView.findViewById(R.id.tvModuleDesc);
            tvBadge = itemView.findViewById(R.id.tvModuleBadge);
        }
    }
}
