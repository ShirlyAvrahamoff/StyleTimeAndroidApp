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
import com.example.styletimeandroidapp.adapters.PassedAppointmentAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PassedAppointmentFragment extends Fragment {
    private static final String TAG = "PassedAppointment";
    private RecyclerView passedAppointmentsRecyclerView;
    private TextView emptyStateText;
    private PassedAppointmentAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passed_appointment, container, false);

        db = FirebaseFirestore.getInstance();
        passedAppointmentsRecyclerView = view.findViewById(R.id.passedAppointmentsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        setupRecyclerView();
        loadPastAppointments();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new PassedAppointmentAdapter();
        passedAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        passedAppointmentsRecyclerView.setAdapter(adapter);
    }

    private void loadPastAppointments() {
        passedAppointmentsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);

        Date currentDateTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy HH:mm", Locale.ENGLISH);

        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> pastAppointments = new ArrayList<>();

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
                                if (appointmentDate.before(currentDateTime)) {
                                    pastAppointments.add(appointment);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing appointment date: " + e.getMessage());
                            }
                        }
                    }

                    pastAppointments.sort((a1, a2) -> {
                        try {
                            String datetime1 = a1.getDate() + " " + a1.getTime();
                            String datetime2 = a2.getDate() + " " + a2.getTime();
                            return dateTimeFormat.parse(datetime2).compareTo(dateTimeFormat.parse(datetime1));
                        } catch (Exception e) {
                            Log.e(TAG, "Error sorting past appointments: " + e.getMessage());
                            return 0;
                        }
                    });

                    if (pastAppointments.isEmpty()) {
                        passedAppointmentsRecyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateAppointments(pastAppointments);
                        passedAppointmentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading past appointments", e);
                    passedAppointmentsRecyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                });
    }
}
