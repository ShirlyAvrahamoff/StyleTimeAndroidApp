package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AppointmentManagementAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


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

        setupRecyclerView();
        loadAllAppointments();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AppointmentManagementAdapter();
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecyclerView.setAdapter(adapter);
    }

    private void loadAllAppointments() {
        // Show loading state
        appointmentsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);

        // Get ALL appointments and filter locally
        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        // Check if booked
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = isAvailableLong != null ? isAvailableLong.intValue() : 0;

                        if (isAvailable == 1) {
                            String id = doc.getId();
                            String userId = doc.getString("userId");
                            String treatment = doc.getString("treatment");
                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            String clientName = doc.getString("clientName"); // May be null

                            Appointment appointment = new Appointment(id, userId, treatment, date, time, isAvailable);
                            if (clientName != null) {
                                appointment.setClientName(clientName);
                            }

                            appointments.add(appointment);
                        }
                    }

                    Log.d(TAG, "Loaded " + appointments.size() + " appointments");

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