package com.example.styletimeandroidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;

import java.util.List;

public class AvailableAppointmentsAdapter extends RecyclerView.Adapter<AvailableAppointmentsAdapter.ViewHolder> {

    private final List<String> appointments;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(String appointment);
    }

    public AvailableAppointmentsAdapter(List<String> appointments, OnItemClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String appointment = appointments.get(position);
        holder.timeText.setText(appointment);

        // שינוי צבע הכפתור לפי בחירה
        holder.itemView.setBackgroundResource(position == selectedPosition ?
                R.drawable.selected_time_background : R.drawable.default_time_background);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // לעדכן רק את הכפתור הישן והחדש כדי לשפר ביצועים
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            listener.onItemClick(appointment);
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointmentTimeText);
        }
    }
}
