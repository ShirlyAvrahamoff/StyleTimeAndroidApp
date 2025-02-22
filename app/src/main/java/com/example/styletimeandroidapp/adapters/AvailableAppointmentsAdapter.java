package com.example.styletimeandroidapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;

import java.util.List;

public class AvailableAppointmentsAdapter extends RecyclerView.Adapter<AvailableAppointmentsAdapter.ViewHolder> {

    private List<String> appointments;
    private List<String> bookedTimes;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(String appointmentTime);
    }

    public AvailableAppointmentsAdapter(List<String> appointments, List<String> bookedTimes, OnItemClickListener listener) {
        this.appointments = appointments;
        this.bookedTimes = bookedTimes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = appointments.get(position);

        holder.timeText.setText(time);

        if (bookedTimes.contains(time)) {
            holder.itemView.setBackgroundColor(Color.RED);
            holder.itemView.setEnabled(false);
        } else {
            holder.itemView.setBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);

            holder.itemView.setOnClickListener(v -> {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
                listener.onItemClick(time);
            });
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;

        ViewHolder(View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointmentTimeText);
        }
    }
}
