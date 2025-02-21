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

/**
 * Adapter for displaying available appointment time slots.
 */
public class AvailableAppointmentsAdapter extends RecyclerView.Adapter<AvailableAppointmentsAdapter.ViewHolder> {

    private List<String> appointments; // List of time slots
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Interface to handle time slot selection.
     */
    public interface OnItemClickListener {
        void onItemClick(String appointmentTime);
    }

    /**
     * Constructor for AvailableAppointmentsAdapter.
     *
     * @param appointments List of available time slots.
     * @param listener     Listener for handling selection.
     */
    public AvailableAppointmentsAdapter(List<String> appointments, OnItemClickListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_appointment, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds time slots to RecyclerView and manages selection highlighting.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = appointments.get(position);

        // Display the time
        holder.timeText.setText(time);

        // Highlight selected time slot
        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);

        // Click listener for time slot selection
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);
            listener.onItemClick(time); // Pass selected time to listener
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /**
     * ViewHolder for available time slots.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;

        ViewHolder(View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointmentTimeText);
        }
    }
}
