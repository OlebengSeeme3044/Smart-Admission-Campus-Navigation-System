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
import za.ac.spu.sacns.models.Programme;

public class AdminProgrammeAdapter extends RecyclerView.Adapter<AdminProgrammeAdapter.ViewHolder> {

    public interface OnProgrammeActionListener {
        void onEdit(Programme programme);
        void onDelete(Programme programme);
    }

    private final Context context;
    private List<Programme> programmeList;
    private final OnProgrammeActionListener listener;

    public AdminProgrammeAdapter(Context context, List<Programme> programmeList, OnProgrammeActionListener listener) {
        this.context = context;
        this.programmeList = programmeList != null ? programmeList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Programme> newProgrammes) {
        this.programmeList = newProgrammes != null ? newProgrammes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_programme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Programme programme = programmeList.get(position);
        holder.tvName.setText(programme.getName());
        holder.tvDept.setText(programme.getDepartment());
        holder.tvMinAps.setText(String.valueOf(programme.getMinAps()));
        holder.tvDuration.setText(programme.getDuration());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(programme);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(programme);
        });
    }

    @Override
    public int getItemCount() {
        return programmeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDept;
        TextView tvMinAps;
        TextView tvDuration;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProgName);
            tvDept = itemView.findViewById(R.id.tvProgDept);
            tvMinAps = itemView.findViewById(R.id.tvProgMinAps);
            tvDuration = itemView.findViewById(R.id.tvProgDuration);
            btnEdit = itemView.findViewById(R.id.btnEditProg);
            btnDelete = itemView.findViewById(R.id.btnDeleteProg);
        }
    }
}
