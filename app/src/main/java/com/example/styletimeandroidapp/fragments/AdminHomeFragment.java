package com.example.styletimeandroidapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
    private Button viewAllAppointmentsButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = view.findViewById(R.id.helloAdminText);
        dailyScheduleButton = view.findViewById(R.id.dailyScheduleButton);
        viewAllAppointmentsButton = view.findViewById(R.id.viewAllAppointmentsButton);

        if (welcomeText == null || dailyScheduleButton == null || viewAllAppointmentsButton == null) {
            Log.e(TAG, "UI components are missing");
            Toast.makeText(getContext(), "UI Initialization Error", Toast.LENGTH_SHORT).show();
            return view;
        }

        setupButtons();

        loadAdminDetails();

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(v).navigate(R.id.action_adminHomeFragment_to_loginFragment);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Do nothing to block back press
                    }
                });

        return view;
    }

    private void setupButtons() {
        dailyScheduleButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_adminHomeFragment_to_dailyScheduleFragment);
        });

        viewAllAppointmentsButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_adminHomeFragment_to_appointmentManagementFragment);
        });


    }

    private void loadAdminDetails() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "ERROR: User is not logged in!");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
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
                            welcomeText.setText("Hello, Admin!");
                        }
                    } else {
                        Log.e(TAG, "User document does not exist.");
                        Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user data", e);
                    Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
