package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.ac.spu.sacns.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String selectedRole = "Prospective";

    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private AutoCompleteTextView actvCountry;
    private TextInputEditText etSaId;
    private TextInputEditText etPassport;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private TextInputLayout saIdLayout;
    private TextInputLayout passportLayout;

    private Button btnNextStep;
    private Button btnBack;
    private Button btnRegister;
    private Button btnBackToLogin;
    private CheckBox cbTerms;

    private View step1Container;
    private View step2Container;
    private TextView tvStep1;
    private TextView tvStep2;
    private View viewStep1Indicator;
    private View viewStep2Indicator;

    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get role passed from LoginActivity
        String roleFromLogin = getIntent().getStringExtra("USER_ROLE");

        if (roleFromLogin != null) {
            selectedRole = roleFromLogin;
        }

        initializeViews();
        setupRegister();
        setupBackToLogin();
    }

    private void initializeViews() {

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        actvCountry = findViewById(R.id.actvCountry);
        etSaId = findViewById(R.id.etSaId);
        etPassport = findViewById(R.id.etPassport);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        saIdLayout = findViewById(R.id.saIdLayout);
        passportLayout = findViewById(R.id.passportLayout);

        btnNextStep = findViewById(R.id.btnNextStep);
        btnBack = findViewById(R.id.btnBack);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        cbTerms = findViewById(R.id.cbTerms);

        step1Container = findViewById(R.id.step1Container);
        step2Container = findViewById(R.id.step2Container);
        tvStep1 = findViewById(R.id.tvStep1);
        tvStep2 = findViewById(R.id.tvStep2);
        viewStep1Indicator = findViewById(R.id.viewStep1Indicator);
        viewStep2Indicator = findViewById(R.id.viewStep2Indicator);

        setupCountryDropdown();
        setupPassportTruncation();
    }

    private void setupRegister() {

        btnNextStep.setOnClickListener(v -> validateAndProceedToStep2());
        btnBack.setOnClickListener(v -> goToStep1());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void setupBackToLogin() {

        btnBackToLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            intent.putExtra("USER_ROLE", selectedRole);

            startActivity(intent);
            finish();
        });
    }

    private void setupCountryDropdown() {
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(new Country("South Africa", "ZA"));
        countries.add(new Country("United States", "US"));
        countries.add(new Country("United Kingdom", "GB"));
        countries.add(new Country("Canada", "CA"));
        countries.add(new Country("Australia", "AU"));
        countries.add(new Country("Germany", "DE"));
        countries.add(new Country("France", "FR"));
        countries.add(new Country("Netherlands", "NL"));
        countries.add(new Country("Italy", "IT"));
        countries.add(new Country("Spain", "ES"));
        countries.add(new Country("Japan", "JP"));
        countries.add(new Country("China", "CN"));
        countries.add(new Country("India", "IN"));
        countries.add(new Country("Brazil", "BR"));
        countries.add(new Country("Russia", "RU"));
        countries.add(new Country("Mexico", "MX"));
        countries.add(new Country("South Korea", "KR"));
        countries.add(new Country("Nigeria", "NG"));
        countries.add(new Country("Kenya", "KE"));
        countries.add(new Country("Ghana", "GH"));
        countries.add(new Country("Zimbabwe", "ZW"));
        countries.add(new Country("Zambia", "ZM"));
        countries.add(new Country("Botswana", "BW"));
        countries.add(new Country("Namibia", "NA"));
        countries.add(new Country("Mozambique", "MZ"));
        countries.add(new Country("Lesotho", "LS"));
        countries.add(new Country("Eswatini", "SZ"));

        ArrayAdapter<Country> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countries
        );

        actvCountry.setAdapter(adapter);
        actvCountry.setThreshold(1);

        actvCountry.setOnItemClickListener((parent, view, position, id) -> {
            Country selectedCountry = (Country) parent.getItemAtPosition(position);
            toggleIdPassportFields(selectedCountry.getCode());
        });

        actvCountry.setOnClickListener(v -> actvCountry.showDropDown());

        actvCountry.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && actvCountry.getText().toString().isEmpty()) {
                actvCountry.setError("Please select your country of origin");
            }
        });
    }

    private void toggleIdPassportFields(String countryCode) {
        if ("ZA".equals(countryCode)) {
            saIdLayout.setVisibility(View.VISIBLE);
            passportLayout.setVisibility(View.GONE);
            etSaId.setText("");
            etPassport.setText("");
        } else {
            saIdLayout.setVisibility(View.GONE);
            passportLayout.setVisibility(View.VISIBLE);
            etSaId.setText("");
            etPassport.setText("");
        }
    }

    private void setupPassportTruncation() {

        etPassport.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 9) {

                    String truncated = s.toString().substring(0, 9);

                    etPassport.setText(truncated);

                    // Keep cursor at the end
                    etPassport.setSelection(9);
                }
            }
        });
    }

    private static class Country {
        private String name;
        private String code;

        public Country(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void validateAndProceedToStep2() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String country = actvCountry.getText().toString().trim();

        // Validate first name
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("Please enter your first name");
            etFirstName.requestFocus();
            return;
        }

        // Validate last name
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Please enter your last name");
            etLastName.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email address");
            etEmail.requestFocus();
            return;
        }

        // Validate email according to selected role
        if (!isValidEmailForRole(email)) {
            etEmail.setError(getEmailErrorMessage());
            etEmail.requestFocus();
            return;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Please enter your phone number");
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return;
        }

        // Validate country selection
        if (TextUtils.isEmpty(country)) {
            actvCountry.setError("Please select your country of origin");
            actvCountry.requestFocus();
            return;
        }

        // Proceed to step 2
        goToStep2();
    }

    private void goToStep2() {

        currentStep = 2;
        step1Container.setVisibility(View.GONE);
        step2Container.setVisibility(View.VISIBLE);
        btnBackToLogin.setVisibility(View.GONE);

        // Update progress indicators
        tvStep1.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvStep1.setTypeface(null, android.graphics.Typeface.NORMAL);
        viewStep1Indicator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        tvStep2.setTextColor(getResources().getColor(R.color.sacns_blue));
        tvStep2.setTypeface(null, android.graphics.Typeface.BOLD);
        viewStep2Indicator.setBackgroundColor(getResources().getColor(R.color.sacns_blue));
    }

    private void goToStep1() {

        currentStep = 1;
        step2Container.setVisibility(View.GONE);
        step1Container.setVisibility(View.VISIBLE);
        btnBackToLogin.setVisibility(View.VISIBLE);

        // Update progress indicators
        tvStep2.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvStep2.setTypeface(null, android.graphics.Typeface.NORMAL);
        viewStep2Indicator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        tvStep1.setTextColor(getResources().getColor(R.color.sacns_blue));
        tvStep1.setTypeface(null, android.graphics.Typeface.BOLD);
        viewStep1Indicator.setBackgroundColor(getResources().getColor(R.color.sacns_blue));
    }

    private void registerUser() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String country = actvCountry.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        String idPassport;
        boolean isSouthAfrican = country.equals("South Africa");

        // Validate based on country
        if (isSouthAfrican) {
            String saId = etSaId.getText().toString().trim();
            
            if (TextUtils.isEmpty(saId)) {
                etSaId.setError("Please enter your SA ID number");
                etSaId.requestFocus();
                return;
            }

            if (saId.length() != 13) {
                etSaId.setError("SA ID must be exactly 13 digits");
                etSaId.requestFocus();
                return;
            }

            if (!saId.matches("\\d{13}")) {
                etSaId.setError("SA ID must contain only digits");
                etSaId.requestFocus();
                return;
            }

            idPassport = saId;
        } else {
            String passport = etPassport.getText().toString().trim();
            
            if (TextUtils.isEmpty(passport)) {
                etPassport.setError("Please enter your passport number");
                etPassport.requestFocus();
                return;
            }

            if (passport.length() < 6) {
                etPassport.setError("Passport number must be at least 6 characters");
                etPassport.requestFocus();
                return;
            }

            if (!passport.matches("[A-Za-z0-9]+")) {
                etPassport.setError("Passport number must be alphanumeric (letters and numbers only)");
                etPassport.requestFocus();
                return;
            }

            idPassport = passport;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email address");
            etEmail.requestFocus();
            return;
        }

        // Validate email according to selected role
        if (!isValidEmailForRole(email)) {
            etEmail.setError(getEmailErrorMessage());
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError(
                    "Password must be at least 8 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // Validate terms checkbox
        if (!cbTerms.isChecked()) {
            Toast.makeText(
                    RegisterActivity.this,
                    "Please agree to the Terms and Conditions",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Validate password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(
                    "Please confirm your password"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        // Disable button while registration is processing
        btnRegister.setEnabled(false);

        if (selectedRole.equals("Admin")) {

            String staffNumber =
                    email.substring(0, email.indexOf("@"));

            checkAdminAuthorization(
                    staffNumber,
                    firstName,
                    lastName,
                    email,
                    phone,
                    idPassport,
                    country,
                    password
            );

        } else {

            createFirebaseAccount(
                    firstName,
                    lastName,
                    email,
                    phone,
                    idPassport,
                    country,
                    password
            );
        }
    }

    private void checkAdminAuthorization(
            String staffNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String idPassport,
            String country,
            String password) {

        db.collection("admin_accounts")
                .document(staffNumber)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Unable to verify administrator authorization.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (!task.getResult().exists()) {

                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Administrator authorization failed. Your staff number is not approved.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Boolean approved =
                            task.getResult().getBoolean("approved");

                    if (approved == null || !approved) {

                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Administrator authorization failed. Your account has not been approved.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    // Admin is authorized.
                    createFirebaseAccount(
                            firstName,
                            lastName,
                            email,
                            phone,
                            idPassport,
                            country,
                            password
                    );
                });
    }

    private void createFirebaseAccount(
            String firstName,
            String lastName,
            String email,
            String phone,
            String idPassport,
            String country,
            String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {

                            btnRegister.setEnabled(true);

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration failed. User information unavailable.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        String uid = user.getUid();

                        user.sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {

                                    if (verificationTask.isSuccessful()) {

                                        createUserProfile(
                                                uid,
                                                firstName,
                                                lastName,
                                                email,
                                                phone,
                                                idPassport,
                                                country
                                        );

                                    } else {

                                        btnRegister.setEnabled(true);

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Account created, but verification email could not be sent.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });

                    } else {

                        btnRegister.setEnabled(true);

                        String errorMessage =
                                "Registration failed.";

                        if (task.getException() != null) {

                            errorMessage =
                                    "Registration failed: "
                                            + task.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private boolean isValidEmailForRole(String email) {

        String lowerCaseEmail = email.toLowerCase();

        switch (selectedRole) {

            case "Prospective":

                // Prospective students must use Gmail
                return lowerCaseEmail.matches(
                        "^[a-z0-9._%+-]+@gmail\\.com$"
                );

            case "Current":

                // Current students must use exactly
                // 9 digits + @spu.ac.za
                return lowerCaseEmail.matches(
                        "^[0-9]{9}@spu\\.ac\\.za$"
                );

            case "Admin":

                // Administrators must use exactly
                // 7 digits + @spu.ac.za
                return lowerCaseEmail.matches(
                        "^[0-9]{7}@spu\\.ac\\.za$"
                );

            default:

                return false;
        }
    }

    private String getEmailErrorMessage() {

        switch (selectedRole) {

            case "Prospective":

                return "Prospective students must use a @gmail.com email address.";

            case "Current":

                return "Current students must use a 9-digit student number followed by @spu.ac.za.";

            case "Admin":

                return "Administrators must use a 7-digit staff number followed by @spu.ac.za.";

            default:

                return "Invalid email address for the selected role.";
        }
    }

    private void createUserProfile(
            String uid,
            String firstName,
            String lastName,
            String email,
            String phone,
            String idPassport,
            String country) {

        Map<String, Object> user = new HashMap<>();

        user.put("uid", uid);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("name", firstName + " " + lastName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("idPassport", idPassport);
        user.put("country", country);
        user.put("role", selectedRole);

        user.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp()
        );

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnCompleteListener(task -> {

                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {

                        // Sign the user out until their email has been verified
                        mAuth.signOut();

                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created. Please check your email and verify your account before logging in.",
                                Toast.LENGTH_LONG
                        ).show();

                        // Return to Login
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class
                        );

                        intent.putExtra(
                                "USER_ROLE",
                                selectedRole
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String errorMessage = "Account created, but user profile could not be saved.";

                        if (task.getException() != null) {
                            errorMessage += "\n" + task.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                    }
                });
    }
}