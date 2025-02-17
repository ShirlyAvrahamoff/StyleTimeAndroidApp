package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.styletimeandroidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientHomeFragment extends Fragment {
    private TextView welcomeText, appointmentsText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = view.findViewById(R.id.welcomeText);
        appointmentsText = view.findViewById(R.id.appointmentsText);

        loadUserDetails();
        return view;
    }

    private void loadUserDetails() {
        if (auth.getCurrentUser() == null) {
            Log.e("ClientHomeFragment", "User is not logged in!");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            welcomeText.setText("Hello, " + name + "!");
                        } else {
                            Log.e("ClientHomeFragment", "Name field is missing in Firestore.");
                        }
                        loadAppointments(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ClientHomeFragment", "Error loading user data", e);
                    Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAppointments(String userId) {
        db.collection("appointments").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        StringBuilder appointments = new StringBuilder();
                        for (DocumentSnapshot doc : querySnapshot) {
                            String date = doc.getString("date");
                            String treatment = doc.getString("treatment");
                            appointments.append("• ").append(treatment).append(" - ").append(date).append("\n");
                        }
                        appointmentsText.setText(appointments.toString());
                    } else {
                        appointmentsText.setText("No upcoming appointments available."); // ✅ English text
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ClientHomeFragment", "Error loading appointments", e);
                    Toast.makeText(getActivity(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                });
    }
}
