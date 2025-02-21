package com.example.styletimeandroidapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.styletimeandroidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private NavController navController;

    private EditText emailEditText, passwordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize EditText fields
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        // Login Button Listener
        view.findViewById(R.id.login_button).setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate Input Fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);  // ✅ Pass the required arguments
            }
        });

        // Navigate to Register Fragment
        view.findViewById(R.id.go_to_register).setOnClickListener(v ->
                navController.navigate(R.id.registerFragment)
        );
    }

    /**
     * Logs in the user using Firebase Authentication.
     */
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());  // ✅ Check if user is admin or client
                        }
                    } else {
                        Toast.makeText(getContext(), "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Checks if the logged-in user is an admin or a client.
     */
    private void checkUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (isAdmin != null && isAdmin) {
                        navController.navigate(R.id.action_loginFragment_to_adminHomeFragment);  // Admin Home
                    } else {
                        navController.navigate(R.id.action_loginFragment_to_clientHomeFragment);  // Client Home
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginFragment", "Error fetching user role", e);
                    showErrorDialog("Error", "Unable to retrieve user information.");
                });
    }

    /**
     * Displays an error dialog.
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
