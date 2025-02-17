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

public class AdminHomeFragment extends Fragment {
    private TextView welcomeText, allAppointmentsText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = view.findViewById(R.id.welcomeText);
        allAppointmentsText = view.findViewById(R.id.allAppointmentsText);

        if (welcomeText == null) {
            Log.e("AdminHomeFragment", " ERROR: welcomeText is NULL! Check fragment_admin_home.xml");
        }

        if (allAppointmentsText == null) {
            Log.e("AdminHomeFragment", " ERROR: allAppointmentsText is NULL! Check fragment_admin_home.xml");
        }

        loadAdminDetails();
        return view;
    }

    private void loadAdminDetails() {
        if (auth.getCurrentUser() == null) {
            Log.e("AdminHomeFragment", " ERROR: User is not logged in!");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");

                        if (name != null && !name.isEmpty()) {
                            if (welcomeText != null) {
                                welcomeText.setText("Hello, " + name + "!");
                            } else {
                                Log.e("AdminHomeFragment", " ERROR: welcomeText is NULL, cannot set text.");
                            }
                        } else {
                            Log.e("AdminHomeFragment", " ERROR: 'name' field is missing in Firestore.");
                        }

                        loadAllAppointments();
                    } else {
                        Log.e("AdminHomeFragment", " ERROR: User document does not exist in Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminHomeFragment", " ERROR: Failed to load admin data.", e);
                    Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAllAppointments() {
        db.collection("appointments").get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        StringBuilder appointments = new StringBuilder();
                        for (DocumentSnapshot doc : querySnapshot) {
                            String date = doc.getString("date");
                            String treatment = doc.getString("treatment");
                            String user = doc.getString("userName");

                            if (date != null && treatment != null && user != null) {
                                appointments.append("• ").append(user).append(" - ").append(treatment)
                                        .append(" on ").append(date).append("\n");
                            } else {
                                Log.e("AdminHomeFragment", " ERROR: Missing data in appointment document: " + doc.getId());
                            }
                        }

                        if (allAppointmentsText != null) {
                            allAppointmentsText.setText(appointments.length() > 0 ? appointments.toString() : "No upcoming appointments available.");
                        }
                    } else {
                        if (allAppointmentsText != null) {
                            allAppointmentsText.setText("No upcoming appointments available.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminHomeFragment", " ERROR: Failed to fetch appointments.", e);
                    Toast.makeText(getActivity(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                });
    }
}
