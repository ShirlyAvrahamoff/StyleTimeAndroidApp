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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.styletimeandroidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeFragment extends Fragment {
    private static final String TAG = "AdminHomeFragment";

    private TextView welcomeText;
    private Button dailyScheduleButton;
    private Button appointmentManagementButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // אתחול הרכיבים
        welcomeText = view.findViewById(R.id.welcomeText);
        dailyScheduleButton = view.findViewById(R.id.dailyScheduleButton);
        appointmentManagementButton = view.findViewById(R.id.appointmentManagementButton);

        // בדיקה שהרכיבים אותחלו בהצלחה
        if (welcomeText == null) {
            Log.e(TAG, "ERROR: welcomeText is NULL! Check fragment_admin_home.xml");
        }

        // הגדרת מאזיני לחיצה לכפתורים
        setupButtons();

        // טעינת פרטי המנהל
        loadAdminDetails();

        return view;
    }

    private void setupButtons() {
        if (dailyScheduleButton != null) {
            dailyScheduleButton.setOnClickListener(v -> {
                // ניווט למסך לוז יומי
                Navigation.findNavController(v).navigate(R.id.action_adminHomeFragment_to_dailyScheduleFragment);
            });
        } else {
            Log.e(TAG, "ERROR: dailyScheduleButton is NULL!");
        }

        if (appointmentManagementButton != null) {
            appointmentManagementButton.setOnClickListener(v -> {
                // ניווט למסך ניהול תורים
                Navigation.findNavController(v).navigate(R.id.action_adminHomeFragment_to_appointmentManagementFragment);
            });
        } else {
            Log.e(TAG, "ERROR: appointmentManagementButton is NULL!");
        }
    }

    private void loadAdminDetails() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "ERROR: User is not logged in!");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");

                        if (name != null && !name.isEmpty()) {
                            if (welcomeText != null) {
                                welcomeText.setText("Hello, " + name);
                            } else {
                                Log.e(TAG, "ERROR: welcomeText is NULL, cannot set text.");
                            }
                        } else {
                            Log.e(TAG, "ERROR: 'name' field is missing in Firestore.");
                        }
                    } else {
                        Log.e(TAG, "ERROR: User document does not exist in Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "ERROR: Failed to load admin data.", e);
                    Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    Log.d("RoleCheck", "Current user role: " + role);
                });
    }
}