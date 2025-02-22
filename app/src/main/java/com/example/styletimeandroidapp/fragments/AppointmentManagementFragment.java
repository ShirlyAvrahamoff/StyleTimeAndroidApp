package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AppointmentManagementAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentManagementFragment extends Fragment {
    private static final String TAG = "AppointmentManagement";

    private RecyclerView appointmentsRecyclerView;
    private TextView emptyStateText;
    private AppointmentManagementAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_management, container, false);

        db = FirebaseFirestore.getInstance();
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        Button btnViewPassedAppointments = view.findViewById(R.id.btnViewPassedAppointments);
        btnViewPassedAppointments.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_appointmentManagementFragment_to_passedAppointmentFragment);
        });

        setupRecyclerView();
        loadCurrentAndFutureAppointments();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AppointmentManagementAdapter();
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecyclerView.setAdapter(adapter);
    }

    private void loadCurrentAndFutureAppointments() {
        appointmentsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);

        Date currentDateTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy HH:mm", Locale.ENGLISH);

        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = isAvailableLong != null ? isAvailableLong.intValue() : 0;

                        if (isAvailable == 1) {
                            String id = doc.getId();
                            String userId = doc.getString("userId");
                            String treatment = doc.getString("treatment");
                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            String clientName = doc.getString("clientName");

                            Appointment appointment = new Appointment(id, userId, treatment, date, time, isAvailable);
                            if (clientName != null) {
                                appointment.setClientName(clientName);
                            }

                            String appointmentDateTime = date + " " + time;
                            try {
                                Date appointmentDate = dateTimeFormat.parse(appointmentDateTime);
                                if (appointmentDate.after(currentDateTime) || appointmentDate.equals(currentDateTime)) {
                                    appointments.add(appointment);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing appointment date: " + e.getMessage());
                            }
                        }
                    }

                    Log.d(TAG, "Loaded " + appointments.size() + " current/future appointments");

                    appointments.sort((a1, a2) -> {
                        try {
                            String datetime1 = a1.getDate() + " " + a1.getTime();
                            String datetime2 = a2.getDate() + " " + a2.getTime();
                            return dateTimeFormat.parse(datetime1).compareTo(dateTimeFormat.parse(datetime2));
                        } catch (Exception e) {
                            Log.e(TAG, "Error sorting appointments: " + e.getMessage());
                            return 0;                         }
                    });

                    if (appointments.isEmpty()) {
                        appointmentsRecyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateAppointments(appointments);
                        appointmentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading appointments", e);
                    appointmentsRecyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                });
    }
}