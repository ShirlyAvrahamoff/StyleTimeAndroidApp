package com.example.styletimeandroidapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PassedAppointmentAdapter extends RecyclerView.Adapter<PassedAppointmentAdapter.ViewHolder> {
    private List<Appointment> appointments = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PassedAppointmentAdapter() {}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passed_appointment_card, parent, false);
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
            return rawDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView treatmentText, dateText, timeText, clientNameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treatmentText = itemView.findViewById(R.id.treatmentText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            clientNameText = itemView.findViewById(R.id.clientNameText);
        }
    }
}