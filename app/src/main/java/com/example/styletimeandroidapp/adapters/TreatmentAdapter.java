package com.example.styletimeandroidapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Treatment;
import java.util.List;

public class TreatmentAdapter extends RecyclerView.Adapter<TreatmentAdapter.ViewHolder> {

    private final List<Treatment> treatments;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Treatment treatment);
    }

    public TreatmentAdapter(List<Treatment> treatments, OnItemClickListener listener) {
        this.treatments = treatments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treatment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Treatment treatment = treatments.get(position);
        holder.treatmentName.setText(treatment.getName());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(treatment));
    }

    @Override
    public int getItemCount() {
        return treatments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView treatmentName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treatmentName = itemView.findViewById(R.id.treatmentName);
        }
    }
}
