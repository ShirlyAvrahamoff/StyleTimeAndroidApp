package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styletimeandroidapp.R;
import com.example.styletimeandroidapp.adapters.AppointmentsAdapter;
import com.example.styletimeandroidapp.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllAppointmentsFragment extends Fragment {
    private RecyclerView allAppointmentsRecyclerView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_appointments, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        allAppointmentsRecyclerView = view.findViewById(R.id.allAppointmentsRecyclerView);
        allAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AppointmentsAdapter(appointmentList);
        allAppointmentsRecyclerView.setAdapter(adapter);

        loadAllAppointments();
        return view;
    }

    private void loadAllAppointments() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "User is not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("appointments").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    appointmentList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Appointment appointment = doc.toObject(Appointment.class);
                        appointmentList.add(appointment);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to load appointments.", Toast.LENGTH_SHORT).show()
                );
    }
}
