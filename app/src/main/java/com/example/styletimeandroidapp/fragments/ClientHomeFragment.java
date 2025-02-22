package com.example.styletimeandroidapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientHomeFragment extends Fragment {

    private RecyclerView recyclerViewAppointments;
    private TextView helloText;
    private TextView noAppointmentsMessage;
    private CardView noAppointmentsCard;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewAppointments = view.findViewById(R.id.recyclerViewAppointments);
        helloText = view.findViewById(R.id.helloText);
        noAppointmentsCard = view.findViewById(R.id.noAppointmentsCard);
        noAppointmentsMessage = view.findViewById(R.id.noAppointmentsMessage);

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AppointmentsAdapter(appointmentList, this::showDeleteConfirmationDialog);
        recyclerViewAppointments.setAdapter(adapter);

        loadUserName();
        listenToAppointments();

        view.findViewById(R.id.bookAppointmentButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_bookAppointmentFragment)
        );

        return view;
    }

    private void loadUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("name");
                        helloText.setText("Hello, " + name + "!");
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch user name", e));
        }
    }

    private void listenToAppointments() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isAvailable", 1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Listen failed.", e);
                        return;
                    }

                    appointmentList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        appointmentList.add(appointment);
                    }

                    Collections.sort(appointmentList, (a1, a2) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            Date date1 = sdf.parse(a1.getDate() + " " + a1.getTime());
                            Date date2 = sdf.parse(a2.getDate() + " " + a2.getTime());
                            return date1.compareTo(date2);
                        } catch (ParseException ex) {
                            Log.e("SortingError", "Error while sorting appointments", ex);
                            return 0;
                        }
                    });
                    adapter.notifyDataSetChanged();


                    if (appointmentList.isEmpty()) {
                        noAppointmentsMessage.setVisibility(View.VISIBLE);
                    } else {
                        noAppointmentsMessage.setVisibility(View.GONE);
                    }
                });
    }

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

    private void showDeleteConfirmationDialog(Appointment appointment) {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> cancelAppointment(appointment))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment(Appointment appointment) {
        db.collection("appointments").document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Appointment canceled", Toast.LENGTH_SHORT).show();
                    listenToAppointments();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show());
    }
}
