package com.example.styletimeandroidapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AvailableAppointmentsAdapter;
import com.example.styletimeandroidapp.models.Treatment;
import com.example.styletimeandroidapp.models.Appointment;
import com.example.styletimeandroidapp.utils.DisableSaturdaysDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookAppointmentFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView availableAppointmentsRecyclerView;
    private Spinner treatmentSpinner;
    private Button confirmAppointmentButton;
    private TextView availableTimesTitle;
    private FirebaseFirestore db;
    private List<String> availableAppointments;
    private List<Treatment> treatments;
    private AvailableAppointmentsAdapter appointmentAdapter;
    private Treatment selectedTreatment;
    private String selectedAppointment;
    private ArrayAdapter<String> treatmentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        // Initialize Firestore and UI elements
        db = FirebaseFirestore.getInstance();
        calendarView = view.findViewById(R.id.calendarView);
        availableAppointmentsRecyclerView = view.findViewById(R.id.availableAppointmentsRecyclerView);
        treatmentSpinner = view.findViewById(R.id.treatmentSpinner);
        confirmAppointmentButton = view.findViewById(R.id.confirmAppointmentButton);
        availableTimesTitle = view.findViewById(R.id.availableTimesTitle);

        availableAppointmentsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        availableAppointments = new ArrayList<>();
        treatments = new ArrayList<>();

        // Set up Treatment Spinner
        List<String> treatmentNames = new ArrayList<>();
        treatmentNames.add("Select Treatment...");
        treatmentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, treatmentNames);
        treatmentSpinner.setAdapter(treatmentAdapter);

        // Set up Appointments Adapter
        appointmentAdapter = new AvailableAppointmentsAdapter(availableAppointments, appointment -> {
            selectedAppointment = appointment;
            confirmAppointmentButton.setVisibility(View.VISIBLE);
        });
        availableAppointmentsRecyclerView.setAdapter(appointmentAdapter);

        setupCalendar();
        loadTreatments();

        // Confirm Appointment Button
        confirmAppointmentButton.setOnClickListener(v -> confirmAppointment());

        // Treatment Selection Listener
        treatmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTreatment = null;
                    calendarView.setVisibility(View.GONE);
                } else {
                    selectedTreatment = treatments.get(position - 1);
                    calendarView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return view;
    }

    /**
     * Sets up the calendar view and its behavior.
     */
    private void setupCalendar() {
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.today())
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.addDecorator(new DisableSaturdaysDecorator());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selectedTreatment != null) {
                loadAvailableAppointments(date);
            } else {
                Toast.makeText(getContext(), "Please select a treatment first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads treatments from Firestore and populates the Spinner.
     */
    private void loadTreatments() {
        db.collection("treatments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    treatments.clear();
                    List<String> treatmentNames = new ArrayList<>();
                    treatmentNames.add("Select Treatment...");

                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        Long durationLong = doc.getLong("duration");

                        if (name != null && durationLong != null) {
                            int duration = durationLong.intValue();
                            treatments.add(new Treatment(name, duration));
                            treatmentNames.add(name);
                        } else {
                            Log.e("Firebase", "Missing or invalid fields in treatment document: " + doc.getId());
                        }
                    }

                    treatmentAdapter.clear();
                    treatmentAdapter.addAll(treatmentNames);
                    treatmentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to load treatments", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Loads available appointments for a selected date.
     */
    private void loadAvailableAppointments(CalendarDay date) {
        availableAppointments.clear();

        Calendar calendar = date.getCalendar();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // ✅ Saturday: Show "Closed" Message
        if (dayOfWeek == Calendar.SATURDAY) {
            availableTimesTitle.setText("Closed on Saturdays");
            availableTimesTitle.setVisibility(View.VISIBLE);
            availableAppointmentsRecyclerView.setVisibility(View.GONE);
            confirmAppointmentButton.setVisibility(View.GONE);
            return;
        } else {
            // ✅ Reset Message When Selecting Other Days
            availableTimesTitle.setText("Select an Available Time:");
            availableTimesTitle.setVisibility(View.VISIBLE);
            availableAppointmentsRecyclerView.setVisibility(View.VISIBLE);
            confirmAppointmentButton.setVisibility(View.GONE); // Hide until a slot is selected
        }

        // ✅ Friday: Different Hours
        int startHour = (dayOfWeek == Calendar.FRIDAY) ? 8 : 10;
        int endHour = (dayOfWeek == Calendar.FRIDAY) ? 15 : 22;

        db.collection("appointments")
                .whereEqualTo("date", formatDateToFirestore(date.getDate().toString()))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> bookedTimes = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String bookedTime = doc.getString("time");
                        if (bookedTime != null) {
                            bookedTimes.add(bookedTime);
                        }
                    }

                    for (int hour = startHour; hour < endHour; hour++) {
                        String timeSlot = String.format("%02d:00", hour);
                        if (!bookedTimes.contains(timeSlot)) {
                            availableAppointments.add(timeSlot);
                        }
                    }

                    appointmentAdapter.notifyDataSetChanged();

                    if (availableAppointments.isEmpty()) {
                        availableTimesTitle.setText("No Available Appointments");
                        availableAppointmentsRecyclerView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to load booked times", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Handles appointment confirmation and saves it to Firestore.
     */
    private void confirmAppointment() {
        if (selectedTreatment == null) {
            Toast.makeText(getContext(), "Please select a treatment!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAppointment == null) {
            Toast.makeText(getContext(), "Please select a time!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the selected date to DD/MM/YYYY
        String formattedDate = formatDate(calendarView.getSelectedDate().getDate().toString());

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Appointment")
                .setMessage("Confirm booking for " + selectedTreatment.getName() + " on " + formattedDate + " at " + selectedAppointment + "?")
                .setPositiveButton("Yes", (dialog, which) -> saveAppointmentToFirestore())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    // Date Formatter Method (DD/MM/YYYY)
    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("DateFormatError", "Error formatting date", e);
            return rawDate; // Fallback to raw date
        }
    }


    /**
     * Formats date to include the day of the week in DD/MM/YYYY format.
     */
    private String formatDateWithDay(String originalDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat desiredFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

            Date date = originalFormat.parse(originalDate);
            return desiredFormat.format(date); // Example: "Tuesday, 20/02/2025"
        } catch (ParseException e) {
            Log.e("DateFormatError", "Error formatting date", e);
            return originalDate;
        }
    }

    private String formatDateToFirestore(String originalDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat desiredFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = originalFormat.parse(originalDate);
            return desiredFormat.format(date);
        } catch (ParseException e) {
            Log.e("DateFormatError", "Error formatting date for Firestore", e);
            return originalDate;
        }
    }


    /**
     * Saves the confirmed appointment to Firestore.
     */
    private void saveAppointmentToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String appointmentId = db.collection("appointments").document().getId();

        // Format date before saving
        String formattedDate = formatDateToFirestore(calendarView.getSelectedDate().getDate().toString());

        db.collection("appointments").document(appointmentId)
                .set(new Appointment(
                        appointmentId,
                        userId,
                        selectedTreatment.getName(),
                        formattedDate,
                        selectedAppointment,
                        1 // 1 means the appointment is booked
                ))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to book appointment!", Toast.LENGTH_SHORT).show()
                );
    }

}
