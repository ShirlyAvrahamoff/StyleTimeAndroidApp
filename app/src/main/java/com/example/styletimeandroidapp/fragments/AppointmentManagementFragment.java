package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AppointmentManagementAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentManagementFragment extends Fragment {
    private static final String TAG = "AppointmentManagement";

    private RecyclerView appointmentsRecyclerView;
    private TextView emptyStateText;
    private AppointmentManagementAdapter adapter;
    private FirebaseFirestore db;

    private RadioGroup radioGroupFilter;
    private RadioButton radioAll, radioClient, radioTreatment;
    private Spinner spinnerClient, spinnerTreatment;
    private Button btnViewPassedAppointments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_management, container, false);

        db = FirebaseFirestore.getInstance();
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        radioGroupFilter = view.findViewById(R.id.radioGroupFilter);
        radioAll = view.findViewById(R.id.radioAll);
        radioClient = view.findViewById(R.id.radioClient);
        radioTreatment = view.findViewById(R.id.radioTreatment);
        spinnerClient = view.findViewById(R.id.spinnerClient);
        spinnerTreatment = view.findViewById(R.id.spinnerTreatment);
        btnViewPassedAppointments = view.findViewById(R.id.btnViewPassedAppointments);

        btnViewPassedAppointments.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_appointmentManagementFragment_to_passedAppointmentFragment);
        });

        setupRecyclerView();
        setupFilterOptions();
        loadCurrentAndFutureAppointments();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AppointmentManagementAdapter();
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecyclerView.setAdapter(adapter);
    }

    private void setupFilterOptions() {
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            adapter.updateAppointments(new ArrayList<>());
            appointmentsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);

            if (checkedId == R.id.radioAll) {
                spinnerClient.setVisibility(View.GONE);
                spinnerTreatment.setVisibility(View.GONE);
                loadCurrentAndFutureAppointments();
            } else if (checkedId == R.id.radioClient) {
                spinnerClient.setVisibility(View.VISIBLE);
                spinnerTreatment.setVisibility(View.GONE);
                loadClientList();
            } else if (checkedId == R.id.radioTreatment) {
                spinnerClient.setVisibility(View.GONE);
                spinnerTreatment.setVisibility(View.VISIBLE);
                loadTreatmentList();
            }
        });
    }

    private void loadCurrentAndFutureAppointments() {
        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                    Date currentDateTime = new Date();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String userId = doc.getString("userId");
                        String treatment = doc.getString("treatment");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String clientName = doc.getString("clientName");
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = (isAvailableLong != null) ? isAvailableLong.intValue() : 0;

                        if (isAvailable == 0) {
                            try {
                                Date appointmentDate = dateTimeFormat.parse(date + " " + time);
                                if (appointmentDate != null && (appointmentDate.after(currentDateTime) || appointmentDate.equals(currentDateTime))) {
                                    Appointment appointment = new Appointment(id, userId, treatment, date, time, isAvailable);
                                    if (clientName != null) appointment.setClientName(clientName);
                                    appointment.setParsedDate(appointmentDate);
                                    appointments.add(appointment);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing appointment date", e);
                            }
                        }
                    }

                    Collections.sort(appointments, (a1, a2) -> a1.getParsedDate().compareTo(a2.getParsedDate()));

                    adapter.updateAppointments(appointments);

                    if (appointments.isEmpty()) {
                        appointmentsRecyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        appointmentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading appointments", e));
    }

    private void loadClientList() {
        db.collection("users")
                .whereEqualTo("role", "client")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> clientNames = new ArrayList<>();
                    clientNames.add("Select Client");

                    for (DocumentSnapshot doc : querySnapshot) {
                        String clientName = doc.getString("name");
                        if (clientName != null) {
                            clientNames.add(clientName);
                        }
                    }

                    ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, clientNames);
                    spinnerClient.setAdapter(clientAdapter);

                    spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position > 0) {
                                String selectedClient = clientNames.get(position);
                                loadAppointmentsForClient(selectedClient);
                            } else {
                                adapter.updateAppointments(new ArrayList<>());
                                appointmentsRecyclerView.setVisibility(View.GONE);
                                emptyStateText.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading clients", e));
    }

    private void loadAppointmentsForClient(String clientName) {
        db.collection("users")
                .whereEqualTo("name", clientName)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.isEmpty()) {
                        Log.e(TAG, "No user found with name: " + clientName);
                        return;
                    }

                    String userId = userSnapshot.getDocuments().get(0).getId();

                    db.collection("appointments")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Appointment> clientAppointments = new ArrayList<>();
                                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                                Date currentDateTime = new Date();

                                for (DocumentSnapshot doc : querySnapshot) {
                                    String id = doc.getId();
                                    String treatment = doc.getString("treatment");
                                    String date = doc.getString("date");
                                    String time = doc.getString("time");
                                    Long isAvailableLong = doc.getLong("isAvailable");
                                    int isAvailable = (isAvailableLong != null) ? isAvailableLong.intValue() : 0;

                                    if (isAvailable == 0) {
                                        try {
                                            Date appointmentDate = dateTimeFormat.parse(date + " " + time);
                                            if (appointmentDate != null && appointmentDate.after(currentDateTime)) {
                                                Appointment appointment = new Appointment(id, userId, clientName, treatment, date, time, isAvailable);
                                                clientAppointments.add(appointment);
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing appointment date", e);
                                        }
                                    }
                                }

                                Collections.sort(clientAppointments, (a1, a2) -> a1.getParsedDate().compareTo(a2.getParsedDate()));

                                adapter.updateAppointments(clientAppointments);

                                if (clientAppointments.isEmpty()) {
                                    appointmentsRecyclerView.setVisibility(View.GONE);
                                    emptyStateText.setVisibility(View.VISIBLE);
                                } else {
                                    appointmentsRecyclerView.setVisibility(View.VISIBLE);
                                    emptyStateText.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error loading client appointments", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user ID for client", e));
    }

    private void loadTreatmentList() {
        db.collection("treatments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> treatmentNames = new ArrayList<>();
                    treatmentNames.add("Select Treatment...");

                    for (DocumentSnapshot doc : querySnapshot) {
                        String treatment = doc.getString("name");
                        if (treatment != null) {
                            treatmentNames.add(treatment);
                        }
                    }

                    ArrayAdapter<String> treatmentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, treatmentNames);
                    spinnerTreatment.setAdapter(treatmentAdapter);

                    spinnerTreatment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position > 0) {
                                String selectedTreatment = treatmentNames.get(position);
                                applyFilter("treatment", selectedTreatment);
                            } else {
                                adapter.updateAppointments(new ArrayList<>());
                                appointmentsRecyclerView.setVisibility(View.GONE);
                                emptyStateText.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading treatments", e));
    }

    private void applyFilter(String field, String value) {
        db.collection("appointments")
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> filteredAppointments = new ArrayList<>();
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
                    Date currentDateTime = new Date();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String userId = doc.getString("userId");
                        String treatment = doc.getString("treatment");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String clientName = doc.getString("clientName");
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = (isAvailableLong != null) ? isAvailableLong.intValue() : 0;

                        if (isAvailable == 0) {
                            try {
                                Date appointmentDate = dateTimeFormat.parse(date + " " + time);
                                if (appointmentDate != null && appointmentDate.after(currentDateTime)) {
                                    Appointment appointment = new Appointment(id, userId, clientName, treatment, date, time, isAvailable);
                                    filteredAppointments.add(appointment);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing appointment date", e);
                            }
                        }
                    }

                    Collections.sort(filteredAppointments, (a1, a2) -> a1.getParsedDate().compareTo(a2.getParsedDate()));

                    adapter.updateAppointments(filteredAppointments);

                    if (filteredAppointments.isEmpty()) {
                        appointmentsRecyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        appointmentsRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error applying filter", e));
    }
}
