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

        loadAppointmentsForDate(new Date()); // טען את התאריך הנוכחי כשנכנסים

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
                        setupRecyclerView();
                        setupFab();
                        loadAppointmentsForDate(new Date());
                    } else {
                        Log.e(TAG, "User document not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking role", e));
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();
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
            fabBook.setOnClickListener(v -> Toast.makeText(getContext(), "Booking feature TBD", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadAppointmentsForDate(Date selectedDate) {
        String displayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(selectedDate);
        dateTitle.setText("Selected Date: " + displayDate);

        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Appointment> appointments = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String docDate = doc.getString("date");
                        String docTime = doc.getString("time");
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = isAvailableLong != null ? isAvailableLong.intValue() : 0;

                        if (docDate != null && docDate.equals(displayDate) && isAvailable == 0) {
                            String id = doc.getId();
                            String userId = doc.getString("userId");
                            String treatment = doc.getString("treatment");

                            Appointment appointment = new Appointment(id, userId, treatment, docDate, docTime, isAvailable);
                            appointments.add(appointment);
                        }
                    }

                    // ✅ מיון התורים לפי השעה
                    appointments.sort((a1, a2) -> {
                        try {
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                            Date time1 = timeFormat.parse(a1.getTime());
                            Date time2 = timeFormat.parse(a2.getTime());
                            return time1.compareTo(time2);
                        } catch (Exception e) {
                            Log.e(TAG, "Error sorting appointments by time: " + e.getMessage());
                            return 0;
                        }
                    });

                    if (appointments.isEmpty()) {
                        showEmptyState();
                    } else {
                        showAppointments(appointments);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading appointments for selected date", e);
                    showEmptyState();
                });
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
