package com.example.styletimeandroidapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
        noAppointmentsMessage = view.findViewById(R.id.noAppointmentsMessage);

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AppointmentsAdapter(appointmentList, this::showDeleteConfirmationDialog);
        recyclerViewAppointments.setAdapter(adapter);

        loadUserName();
        listenToAppointments();

        view.findViewById(R.id.bookAppointmentButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_bookAppointmentFragment)
        );

        view.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(v).navigate(R.id.action_clientHomeFragment_to_loginFragment);
        });

        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                    }
                });

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("appointments")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isAvailable", 0)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    appointmentList.clear();
                    Date now = new Date(); // התאריך והשעה הנוכחיים

                    for (DocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);

                        // סינון רק לתורים עתידיים
                        if (appointment != null && appointment.getParsedDate() != null && appointment.getParsedDate().after(now)) {
                            appointmentList.add(appointment);
                        }
                    }

                    // מיון התורים לפי תאריך מהקרוב לרחוק
                    Collections.sort(appointmentList, (a1, a2) -> {
                        if (a1.getParsedDate() != null && a2.getParsedDate() != null) {
                            return a1.getParsedDate().compareTo(a2.getParsedDate());
                        } else {
                            return 0;
                        }
                    });

                    adapter.notifyDataSetChanged();

                    // הצגת הודעה אם אין תורים עתידיים
                    if (appointmentList.isEmpty()) {
                        noAppointmentsMessage.setVisibility(View.VISIBLE);
                        recyclerViewAppointments.setVisibility(View.GONE);
                    } else {
                        noAppointmentsMessage.setVisibility(View.GONE);
                        recyclerViewAppointments.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching appointments", e));
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
