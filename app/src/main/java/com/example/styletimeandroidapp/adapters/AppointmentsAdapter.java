package com.example.styletimeandroidapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying booked appointments in a RecyclerView.
 */
public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private final List<Appointment> appointmentList;
    private final OnCancelClickListener cancelListener;

    /**
     * Interface to handle appointment cancellation.
     */
    public interface OnCancelClickListener {
        void onCancel(Appointment appointment);
    }

    /**
     * Constructor for AppointmentsAdapter.
     *
     * @param appointmentList List of appointments.
     * @param cancelListener  Listener for handling cancellations.
     */
    public AppointmentsAdapter(List<Appointment> appointmentList, OnCancelClickListener cancelListener) {
        this.appointmentList = appointmentList;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to each item in the RecyclerView.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Display the treatment name
        holder.treatmentText.setText(appointment.getTreatment());

        // Format and display the date (DD/MM/YYYY)
        String formattedDate = formatDate(appointment.getDate());
        holder.dateText.setText("Date: " + formattedDate);

        // Display the time directly
        holder.timeText.setText("Time: " + appointment.getTime());

        // Cancel button functionality
        holder.cancelButton.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancel(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    /**
     * ViewHolder class for each appointment item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView treatmentText, dateText, timeText;
        Button cancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treatmentText = itemView.findViewById(R.id.treatmentText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }

    /**
     * Formats Firestore's raw date to "dd/MM/yyyy".
     *
     * @param rawDate The raw date string from Firestore.
     * @return Formatted date or raw date if parsing fails.
     */
    private String formatDate(String rawDate) {
        try {
            // Example Firestore date: "Mon Feb 24 00:00:00 GMT+02:00 2025"
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return outputFormat.format(inputFormat.parse(rawDate));
        } catch (ParseException e) {
            Log.e("AppointmentsAdapter", "Error formatting date: " + e.getMessage());
            return rawDate; // Fallback if parsing fails
        }
    }
}
