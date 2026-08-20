package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import za.ac.spu.sacns.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String selectedRole = "Prospective";

    private Button btnProspective;
    private Button btnCurrent;
    private Button btnAdmin;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        String roleFromLanding = getIntent().getStringExtra("USER_ROLE");

        if (roleFromLanding != null) {
            selectedRole = roleFromLanding;
        }

        initializeViews();
        setupRoleSelection();
        updateRoleButtons();
        setupLogin();
    }

    private void initializeViews() {

        btnProspective = findViewById(R.id.btnProspective);
        btnCurrent = findViewById(R.id.btnCurrent);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupRoleSelection() {

        btnProspective.setOnClickListener(v -> {
            selectedRole = "Prospective";
            updateRoleButtons();
        });

        btnCurrent.setOnClickListener(v -> {
            selectedRole = "Current";
            updateRoleButtons();
        });

        btnAdmin.setOnClickListener(v -> {
            selectedRole = "Admin";
            updateRoleButtons();
        });
    }

    private void updateRoleButtons() {

        // Reset all buttons
        btnProspective.setBackgroundTintList(
                getColorStateList(android.R.color.darker_gray)
        );

        btnCurrent.setBackgroundTintList(
                getColorStateList(android.R.color.darker_gray)
        );

        btnAdmin.setBackgroundTintList(
                getColorStateList(android.R.color.darker_gray)
        );

        btnProspective.setTextColor(
                getColor(android.R.color.black)
        );

        btnCurrent.setTextColor(
                getColor(android.R.color.black)
        );

        btnAdmin.setTextColor(
                getColor(android.R.color.black)
        );

        // Highlight selected role
        Button selectedButton;

        if (selectedRole.equals("Prospective")) {
            selectedButton = btnProspective;

        } else if (selectedRole.equals("Current")) {
            selectedButton = btnCurrent;

        } else {
            selectedButton = btnAdmin;
        }

        selectedButton.setBackgroundTintList(
                getColorStateList(R.color.sacns_blue)
        );

        selectedButton.setTextColor(
                getColor(android.R.color.white)
        );
    }

    private void setupLogin() {

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {

        com.google.android.material.textfield.TextInputEditText emailInput =
                findViewById(R.id.etEmail);

        com.google.android.material.textfield.TextInputEditText passwordInput =
                findViewById(R.id.etPassword);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Please enter your email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Please enter your password");
            passwordInput.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        // Step 1: Authenticate user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            // Step 2: Get the Firebase Authentication UID
                            String uid = user.getUid();

                            // Step 3: Find the matching user document in Firestore
                            getUserRole(uid);

                        } else {

                            btnLogin.setEnabled(true);

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Unable to get user information.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } else {

                        btnLogin.setEnabled(true);

                        String errorMessage = "Login failed.";

                        if (task.getException() != null) {
                            errorMessage = "Login failed: " +
                                    task.getException().getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void getUserRole(String uid) {

        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {

                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();

                        if (document != null && document.exists()) {

                            String firestoreRole = document.getString("role");

                            if (firestoreRole == null) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "User role has not been assigned.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // Compare selected role with the role stored in Firestore
                            if (!selectedRole.equalsIgnoreCase(firestoreRole)) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Access denied. Your account is registered as "
                                                + firestoreRole + ".",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // Role is valid
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful as " + firestoreRole,
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Navigate according to the verified Firestore role
                            navigateToDashboard(firestoreRole);

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "User profile not found in Firestore.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Failed to retrieve user role.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void navigateToDashboard(String role) {

        switch (role) {

            case "Prospective":

                // Dashboard will be connected here
                Toast.makeText(
                        this,
                        "Opening Prospective Student Dashboard",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case "Current":

                // Dashboard will be connected here
                Toast.makeText(
                        this,
                        "Opening Current Student Dashboard",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case "Admin":

                // Dashboard will be connected here
                Toast.makeText(
                        this,
                        "Opening Admin Dashboard",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            default:

                Toast.makeText(
                        this,
                        "Unknown user role.",
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }
}