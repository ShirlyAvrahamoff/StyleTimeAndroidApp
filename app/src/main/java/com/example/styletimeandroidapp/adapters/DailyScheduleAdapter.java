package com.example.styletimeandroidapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DailyScheduleAdapter extends RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder> {
    private List<Appointment> appointments = new ArrayList<>();
    private Map<String, String> userNames = new HashMap<>();

    private final boolean isManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DailyScheduleAdapter(boolean isManager) {
        this.isManager = isManager;
    }

    public void updateUserName(String userId, String name) {
        userNames.put(userId, name);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_schedule_card, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.treatmentText.setText(appointment.getTreatment());
        holder.dateText.setText(formatDate(appointment.getDate()));
        holder.timeText.setText(appointment.getTime());

        if (isManager) {
            String shortUserId = appointment.getUserId().substring(0,
                    Math.min(6, appointment.getUserId().length())) + "...";
            holder.clientNameText.setText("Loading client info...");
            holder.clientNameText.setVisibility(View.VISIBLE);

            FirebaseFirestore.getInstance().collection("users")
                    .document(appointment.getUserId())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            if (name != null && !name.isEmpty()) {
                                holder.clientNameText.setText("Client: " + name);
                            } else {
                                holder.clientNameText.setText("Client ID: " + shortUserId);
                            }
                        } else {
                            holder.clientNameText.setText("Client ID: " + shortUserId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.clientNameText.setText("Client ID: " + shortUserId);
                    });
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

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return outputFormat.format(inputFormat.parse(rawDate));
        } catch (Exception e) {
            Log.e("DailyScheduleAdapter", "Error formatting date: " + e.getMessage(), e);
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
            clientNameText = itemView.findViewById(R.id.clientNameText); // Ensure this ID exists in layout
        }
    }
}
