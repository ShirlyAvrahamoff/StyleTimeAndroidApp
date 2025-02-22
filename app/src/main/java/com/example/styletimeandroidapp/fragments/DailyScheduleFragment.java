package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.DailyScheduleAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class DailyScheduleFragment extends Fragment {
    private static final String TAG = "DailyScheduleFragment";

    private CalendarView calendarView;
    private RecyclerView dailyScheduleRecyclerView;
    private TextView dateTitle;
    private TextView emptyStateText;
    private FloatingActionButton fabBook;
    private DailyScheduleAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isManager = false;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_schedule, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        initViews(view);
        checkUserRole();
        setupCalendarView();
        setupRecyclerView();

        loadAppointmentsForDate(new Date());
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        dailyScheduleRecyclerView = view.findViewById(R.id.dailyScheduleRecyclerView);
        dateTitle = view.findViewById(R.id.dateTitle);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        fabBook = view.findViewById(R.id.fabBook);
    }

    private void checkUserRole() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Please sign in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        isManager = "manager".equals(role) || "admin".equals(role);
                        Log.d(TAG, "User role: " + role + ", isManager: " + isManager);
                        setupRecyclerView();
                        setupFab();
                        loadAppointmentsForDate(new Date());
                    } else {
                        Log.e(TAG, "User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking role: " + e.getMessage(), e);
                    showEmptyState();
                });
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date selectedDate = calendar.getTime();
            Log.d(TAG, "Selected date from calendar UI: " + selectedDate);

            loadAppointmentsForDate(selectedDate);
        });
    }

    private void setupRecyclerView() {
        adapter = new DailyScheduleAdapter(isManager);
        dailyScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyScheduleRecyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        if (isManager) {
            fabBook.setVisibility(View.GONE);
        } else {
            fabBook.setVisibility(View.VISIBLE);
            fabBook.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Booking feature TBD", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadAppointmentsForDate(Date date) {
        String displayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date);
        dateTitle.setText("Selected Date: " + displayDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        List<String> possibleDateFormats = new ArrayList<>();

        SimpleDateFormat sdfGmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+00:00' yyyy", Locale.ENGLISH);
        sdfGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        possibleDateFormats.add(sdfGmt.format(calendar.getTime()));

        SimpleDateFormat sdfGmt2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+02:00' yyyy", Locale.ENGLISH);
        sdfGmt2.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        possibleDateFormats.add(sdfGmt2.format(calendar.getTime()));

        Calendar calGmt2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+02:00"));
        calGmt2.setTime(date);
        calGmt2.set(Calendar.HOUR_OF_DAY, 0);
        calGmt2.set(Calendar.MINUTE, 0);
        calGmt2.set(Calendar.SECOND, 0);
        calGmt2.set(Calendar.MILLISECOND, 0);
        possibleDateFormats.add(sdfGmt2.format(calGmt2.getTime()));

        Log.d(TAG, "Querying for dates: " + possibleDateFormats);

        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String docDate = doc.getString("date");

                        if (docDate != null && possibleDateFormats.contains(docDate)) {
                            Long isAvailableLong = doc.getLong("isAvailable");
                            int isAvailable = isAvailableLong != null ? isAvailableLong.intValue() : 0;

                            if (isAvailable == 1) {
                                String id = doc.getId();
                                String userId = doc.getString("userId");
                                String time = doc.getString("time");
                                String treatment = doc.getString("treatment");

                                Appointment appointment = new Appointment(id, userId, treatment, displayDate, time, isAvailable);
                                appointments.add(appointment);
                            }
                        }
                    }

                    Log.d(TAG, "Found " + appointments.size() + " appointments for date " + displayDate);

                    if (appointments.isEmpty()) {
                        showEmptyState();
                    } else {
                        showAppointments(appointments);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading appointments: " + e.getMessage(), e);
                    showEmptyState();
                });
    }
    private void processAppointments(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, String displayDate) {
        List<Appointment> appointments = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            String id = doc.getId();
            String userId = doc.getString("userId");
            String treatment = doc.getString("treatment");
            String time = doc.getString("time");
            Long isAvailableLong = doc.getLong("isAvailable");
            int isAvailable = isAvailableLong != null ? isAvailableLong.intValue() : 1;

            if (treatment != null && time != null && isAvailable == 1) {
                appointments.add(new Appointment(id, userId, treatment, displayDate, time, isAvailable));

                if (userId != null) {
                    userIds.add(userId);
                }
            }
        }

        Log.d(TAG, "Need to fetch names for " + userIds.size() + " users");

        if (appointments.isEmpty()) {
            showEmptyState();
        } else {
            showAppointments(appointments);

            fetchUserNames(userIds);
        }
    }

    private void fetchUserNames(Set<String> userIds) {
        for (String userId : userIds) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                Log.d(TAG, "Found name for user " + userId + ": " + name);
                                // Update the adapter with this user's name
                                adapter.updateUserName(userId, name);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user name for " + userId, e);
                    });
        }
    }

    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
        dailyScheduleRecyclerView.setVisibility(View.GONE);
    }

    private void showAppointments(List<Appointment> appointments) {
        emptyStateText.setVisibility(View.GONE);
        dailyScheduleRecyclerView.setVisibility(View.VISIBLE);
        adapter.updateAppointments(appointments);
    }
}