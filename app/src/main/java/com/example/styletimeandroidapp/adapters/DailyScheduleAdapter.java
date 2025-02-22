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

import java.util.ArrayList;
import java.util.List;

public class DailyScheduleAdapter extends RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder> {
    private List<Appointment> appointments = new ArrayList<>();
    private final boolean isManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DailyScheduleAdapter(boolean isManager) {
        this.isManager = isManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.treatmentText.setText(appointment.getTreatment());
        holder.timeText.setText(appointment.getTime());

        if (isManager) {
            holder.clientNameText.setText("Loading...");

            // Fetch client name from Firestore using userId
            db.collection("users").document(appointment.getUserId())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            if (name != null && !name.isEmpty()) {
                                holder.clientNameText.setText("Client: " + name);
                            } else {
                                holder.clientNameText.setText("Client: Unknown");
                            }
                        } else {
                            holder.clientNameText.setText("Client: Not Found");
                        }
                    })
                    .addOnFailureListener(e -> holder.clientNameText.setText("Error loading client"));
        } else {
            holder.clientNameText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments != null ? newAppointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView treatmentText, timeText, clientNameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            treatmentText = itemView.findViewById(R.id.treatmentText);
            timeText = itemView.findViewById(R.id.timeText);
            clientNameText = itemView.findViewById(R.id.clientNameText);
        }
    }
}
