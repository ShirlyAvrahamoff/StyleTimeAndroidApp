package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointmentFragment extends Fragment {
    private Spinner treatmentSpinner;
    private RecyclerView availableSlotsRecyclerView;
    private Button confirmButton;
    private TextView selectedSlotText;
    private FirebaseFirestore db;
    private List<String> availableSlots;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        db = FirebaseFirestore.getInstance();
        treatmentSpinner = view.findViewById(R.id.treatmentSpinner);
        availableSlotsRecyclerView = view.findViewById(R.id.availableSlotsRecyclerView);
        confirmButton = view.findViewById(R.id.confirmButton);
        selectedSlotText = view.findViewById(R.id.selectedSlotText);

        availableSlotsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        availableSlots = new ArrayList<>();

        loadTreatments();
        loadAvailableSlots();

        confirmButton.setOnClickListener(v -> confirmAppointment());

        return view;
    }

    // Load treatments from Firestore
    private void loadTreatments() {
        db.collection("treatments").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> treatmentList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        treatmentList.add(doc.getString("name"));
                    }
                    // TODO: Set up Spinner adapter here
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to load treatments", Toast.LENGTH_SHORT).show());
    }

    // Load available slots for the upcoming week
    private void loadAvailableSlots() {
        availableSlots.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // Skip Saturday (Shabbat)
            if (dayOfWeek == Calendar.SATURDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            // Get working hours based on the day
            int startHour = (dayOfWeek == Calendar.FRIDAY) ? 9 : 10;
            int endHour = (dayOfWeek == Calendar.FRIDAY) ? 15 : 22;

            for (int hour = startHour; hour < endHour; hour++) {
                String slot = sdf.format(calendar.getTime()) + " - " + hour + ":00";
                availableSlots.add(slot);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // TODO: Set up RecyclerView adapter here
    }

    private void confirmAppointment() {
        String selectedSlot = selectedSlotText.getText().toString();
        if (selectedSlot.isEmpty()) {
            Toast.makeText(getActivity(), "Please select an available slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save the appointment to Firestore
        Toast.makeText(getActivity(), "Appointment booked: " + selectedSlot, Toast.LENGTH_SHORT).show();
    }
}
