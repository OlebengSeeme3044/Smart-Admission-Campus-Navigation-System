package za.ac.spu.sacns.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.models.Building;

public class AdminBuildingAdapter extends RecyclerView.Adapter<AdminBuildingAdapter.ViewHolder> {

    public interface OnBuildingActionListener {
        void onEdit(Building building);
        void onDelete(Building building);
    }

    private final Context context;
    private List<Building> buildingList;
    private final OnBuildingActionListener listener;

    public AdminBuildingAdapter(Context context, List<Building> buildingList, OnBuildingActionListener listener) {
        this.context = context;
        this.buildingList = buildingList != null ? buildingList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Building> newBuildings) {
        this.buildingList = newBuildings != null ? newBuildings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_building, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Building building = buildingList.get(position);
        holder.tvName.setText(building.getName());
        holder.tvType.setText(building.getType());
        holder.tvDesc.setText(building.getDescription());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(building);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(building);
        });
    }

    @Override
    public int getItemCount() {
        return buildingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvType;
        TextView tvDesc;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBuildingName);
            tvType = itemView.findViewById(R.id.tvBuildingType);
            tvDesc = itemView.findViewById(R.id.tvBuildingDesc);
            btnEdit = itemView.findViewById(R.id.btnEditBuilding);
            btnDelete = itemView.findViewById(R.id.btnDeleteBuilding);
        }
    }
}
