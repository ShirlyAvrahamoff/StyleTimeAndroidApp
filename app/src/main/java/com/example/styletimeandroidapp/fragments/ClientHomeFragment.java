package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AppointmentsAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClientHomeFragment extends Fragment {

    private TextView helloText, upcomingAppointmentsTitle;
    private RecyclerView recyclerViewAppointments;
    private CardView noAppointmentsCard;
    private Button bookAppointmentButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList;
    private boolean isAdmin = false;  // משתנה לבדוק האם המשתמש הוא מנהל

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_home, container, false);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // אתחול רכיבי UI
        helloText = view.findViewById(R.id.helloText);
        bookAppointmentButton = view.findViewById(R.id.bookAppointmentButton);
        recyclerViewAppointments = view.findViewById(R.id.recyclerViewAppointments);
        noAppointmentsCard = view.findViewById(R.id.noAppointmentsCard);
        upcomingAppointmentsTitle = view.findViewById(R.id.upcomingAppointmentsTitle);

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        appointmentList = new ArrayList<>();
        adapter = new AppointmentsAdapter(appointmentList);
        recyclerViewAppointments.setAdapter(adapter);

        // טעינת שם המשתמש + בדיקת האם הוא מנהל
        loadUserNameAndCheckAdmin();

        // טעינת התורים של המשתמש
        loadUserAppointments();

        // מעבר להזמנת תור חדש
        bookAppointmentButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_bookAppointmentFragment)
        );

        return view;
    }

    private void loadUserNameAndCheckAdmin() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            isAdmin = Boolean.TRUE.equals(document.getBoolean("isAdmin")); // בדיקה אם המשתמש הוא מנהל

                            if (name != null && !name.isEmpty()) {
                                helloText.setText("Hello, " + name + "!");
                            } else {
                                helloText.setText("Hello, User!");
                            }

                            // לאחר שאימתנו אם הוא מנהל, טוענים את התורים
                            loadUserAppointments();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ClientHomeFragment", "Failed to load user data", e));
        }
    }

    private void loadUserAppointments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointmentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String appointmentId = doc.getId();
                        String treatment = doc.getString("treatment");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        Long isAvailableLong = doc.getLong("isAvailable");
                        int isAvailable = (isAvailableLong != null) ? isAvailableLong.intValue() : 0; // ברירת מחדל 0

                        if (treatment != null && date != null && time != null) {
                            appointmentList.add(new Appointment(appointmentId, userId, treatment, date, time, isAvailable));
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (appointmentList.isEmpty()) {
                        recyclerViewAppointments.setVisibility(View.GONE);
                        noAppointmentsCard.setVisibility(View.VISIBLE);
                        upcomingAppointmentsTitle.setVisibility(View.GONE);
                    } else {
                        recyclerViewAppointments.setVisibility(View.VISIBLE);
                        noAppointmentsCard.setVisibility(View.GONE);
                        upcomingAppointmentsTitle.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.e("ClientHomeFragment", "Error loading appointments", e));
    }

    private void deleteAppointment(String appointmentId) {
        db.collection("appointments").document(appointmentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment canceled successfully!", Toast.LENGTH_SHORT).show();
                    loadUserAppointments();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                    Log.e("ClientHomeFragment", "Error deleting appointment", e);
                });
    }


}
