package com.example.styletimeandroidapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.styletimeandroidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private NavController navController;

    private EditText nameEditText, emailEditText, phoneEditText, passwordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        view.findViewById(R.id.registerButton).setOnClickListener(v -> registerUser());
        view.findViewById(R.id.go_to_login).setOnClickListener(v -> navController.navigate(R.id.loginFragment));
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showErrorDialog("Error", "All fields are required.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), name, email, phone);
                        }
                    } else {
                        Log.e("RegisterFragment", "Registration failed", task.getException());
                        showErrorDialog("Registration Failed", "Error creating account. Please try again.");
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", "client"); // Default role

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RegisterFragment", "User registered successfully!");

                    // Sign out the user after registration
                    mAuth.signOut();

                    // Show success dialog
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e("RegisterFragment", "Failed to save user", e);
                    showErrorDialog("Error", "Failed to save user data.");
                });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Registration Successful")
                .setMessage("Your account has been created successfully! You can now log in.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    navController.navigate(R.id.loginFragment);
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
