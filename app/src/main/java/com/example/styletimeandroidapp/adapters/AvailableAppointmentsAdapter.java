package com.example.styletimeandroidapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Appointment;

import java.util.List;

public class AvailableAppointmentsAdapter extends RecyclerView.Adapter<AvailableAppointmentsAdapter.AppointmentViewHolder> {

    private List<Appointment> availableAppointments;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public AvailableAppointmentsAdapter(List<Appointment> availableAppointments, OnAppointmentClickListener listener) {
        this.availableAppointments = availableAppointments;
        this.listener = listener;
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText;
        CardView cardView;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.appointmentDate);
            timeText = itemView.findViewById(R.id.appointmentTime);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Appointment appointment) {
            dateText.setText(appointment.getStartTime().split(" ")[0]); // Extract date
            timeText.setText(appointment.getStartTime().split(" ")[1]); // Extract time

            if (!appointment.isAvailable()) {
                cardView.setCardBackgroundColor(itemView.getResources().getColor(R.color.gray)); // Mark as unavailable
                cardView.setEnabled(false);
            } else {
                cardView.setOnClickListener(v -> listener.onAppointmentClick(appointment));
            }
        }
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_card, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(availableAppointments.get(position));
    }

    @Override
    public int getItemCount() {
        return availableAppointments.size();
    }
}
