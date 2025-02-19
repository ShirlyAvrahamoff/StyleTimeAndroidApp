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

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        view.findViewById(R.id.login_button).setOnClickListener(v -> loginUser());
        view.findViewById(R.id.go_to_register).setOnClickListener(v -> navController.navigate(R.id.registerFragment));
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorDialog("Error", "Email and password cannot be empty.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginFragment", "Login successful, checking role...");
                        checkUserRole();
                    } else {
                        Log.e("LoginFragment", "Login failed", task.getException());
                        showErrorDialog("Login Failed", "Invalid email or password.");
                    }
                });
    }

    private void checkUserRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("LoginFragment", "No user found after login");
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                Log.d("LoginFragment", "User is admin, navigating to AdminHomeFragment");
                                navController.navigate(R.id.adminHomeFragment);
                            } else {
                                Log.d("LoginFragment", "User is client, navigating to ClientHomeFragment");
                                navController.navigate(R.id.clientHomeFragment);
                            }
                        } else {
                            Log.e("LoginFragment", "User role not found");
                            showErrorDialog("Error", "Failed to retrieve user role.");
                        }
                    } else {
                        Log.e("LoginFragment", "Failed to fetch user role", task.getException());
                        showErrorDialog("Error", "Failed to retrieve user data.");
                    }
                });
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
