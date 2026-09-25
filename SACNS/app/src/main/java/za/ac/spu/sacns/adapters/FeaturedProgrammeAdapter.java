package za.ac.spu.sacns.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.models.Programme;

public class FeaturedProgrammeAdapter extends RecyclerView.Adapter<FeaturedProgrammeAdapter.ViewHolder> {

    public interface OnProgrammeClickListener {
        void onProgrammeClick(Programme programme);
    }

    private final Context context;
    private List<Programme> programmes;
    private final OnProgrammeClickListener listener;

    public FeaturedProgrammeAdapter(Context context, List<Programme> programmes, OnProgrammeClickListener listener) {
        this.context = context;
        this.programmes = programmes != null ? programmes : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Programme> newProgrammes) {
        this.programmes = newProgrammes != null ? newProgrammes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_programme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Programme programme = programmes.get(position);
        holder.tvName.setText(programme.getName());
        holder.tvDept.setText(programme.getDepartment() + " • " + programme.getDuration());
        holder.tvMinAps.setText("Min APS: " + programme.getMinAps());
        holder.tvReqSnippet.setText(programme.getRequirements());

        holder.cardItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProgrammeClick(programme);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programmes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardItem;
        TextView tvName;
        TextView tvDept;
        TextView tvMinAps;
        TextView tvReqSnippet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardFeaturedProgramme);
            tvName = itemView.findViewById(R.id.tvFeaturedProgName);
            tvDept = itemView.findViewById(R.id.tvFeaturedDept);
            tvMinAps = itemView.findViewById(R.id.tvFeaturedMinAps);
            tvReqSnippet = itemView.findViewById(R.id.tvFeaturedReqSnippet);
        }
    }
}
