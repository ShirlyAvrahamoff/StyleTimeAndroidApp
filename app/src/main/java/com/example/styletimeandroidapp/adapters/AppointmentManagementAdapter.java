package com.example.styletimeandroidapp.adapters;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentManagementAdapter extends RecyclerView.Adapter<AppointmentManagementAdapter.ViewHolder> {
    private List<Appointment> appointments = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AppointmentManagementAdapter() {}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_management_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.treatmentText.setText(appointment.getTreatment());
        holder.dateText.setText(formatDate(appointment.getDate()));
        holder.timeText.setText(appointment.getTime());

        db.collection("users").document(appointment.getUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    String clientName = doc.getString("name");
                    holder.clientNameText.setText(clientName != null ? clientName : "Unknown");
                })
                .addOnFailureListener(e -> {
                    holder.clientNameText.setText("Client ID: " + appointment.getUserId().substring(0, 6) + "...");
                });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Appointment")
                    .setMessage("Are you sure you want to delete this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("appointments").document(appointment.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    appointments.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, appointments.size());
                                    Toast.makeText(v.getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Failed to delete appointment", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments != null ? newAppointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return outputFormat.format(inputFormat.parse(rawDate));
        } catch (Exception e) {
            Log.e("AppointmentManagementAdapter", "Error formatting date: " + e.getMessage());
            return rawDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView treatmentText, dateText, timeText, clientNameText;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treatmentText = itemView.findViewById(R.id.treatmentText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}