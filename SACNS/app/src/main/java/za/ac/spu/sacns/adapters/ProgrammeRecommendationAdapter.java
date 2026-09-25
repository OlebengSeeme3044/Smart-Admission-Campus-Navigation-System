package za.ac.spu.sacns.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.models.Programme;

public class ProgrammeRecommendationAdapter extends RecyclerView.Adapter<ProgrammeRecommendationAdapter.ViewHolder> {

    public interface OnRecommendationClickListener {
        void onRecommendationClick(Programme programme, String status);
    }

    private final Context context;
    private List<Programme> programmeList;
    private int userAps;
    private final OnRecommendationClickListener listener;

    public ProgrammeRecommendationAdapter(Context context, List<Programme> programmeList, int userAps, OnRecommendationClickListener listener) {
        this.context = context;
        this.programmeList = programmeList != null ? programmeList : new ArrayList<>();
        this.userAps = userAps;
        this.listener = listener;
    }

    public void updateData(List<Programme> newProgrammes, int newUserAps) {
        this.programmeList = newProgrammes != null ? newProgrammes : new ArrayList<>();
        this.userAps = newUserAps;
        notifyDataSetChanged();
    }

    public void setUserAps(int newUserAps) {
        this.userAps = newUserAps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_programme_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Programme programme = programmeList.get(position);
        holder.tvName.setText(programme.getName());
        holder.tvDept.setText(programme.getDepartment() + " • " + programme.getDuration());
        holder.tvMinAps.setText("Min APS: " + programme.getMinAps());
        holder.tvYourAps.setText("Your APS: " + userAps);

        String status = programme.getEligibilityStatus(userAps);

        if ("qualified".equals(status)) {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
            holder.tvStatusBadge.setText("QUALIFIED");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#DCFCE7"));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#166534"));
        } else if ("borderline".equals(status)) {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
            holder.tvStatusBadge.setText("BORDERLINE");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#92400E"));
        } else {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FEF2F2"));
            holder.tvStatusBadge.setText("NOT ELIGIBLE");
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvStatusBadge.setTextColor(Color.parseColor("#991B1B"));
        }

        holder.cardRoot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecommendationClick(programme, status);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programmeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardRoot;
        TextView tvName;
        TextView tvDept;
        TextView tvMinAps;
        TextView tvYourAps;
        TextView tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardProgrammeRec);
            tvName = itemView.findViewById(R.id.tvRecProgName);
            tvDept = itemView.findViewById(R.id.tvRecProgDept);
            tvMinAps = itemView.findViewById(R.id.tvRecMinAps);
            tvYourAps = itemView.findViewById(R.id.tvRecYourAps);
            tvStatusBadge = itemView.findViewById(R.id.tvRecStatusBadge);
        }
    }
}
