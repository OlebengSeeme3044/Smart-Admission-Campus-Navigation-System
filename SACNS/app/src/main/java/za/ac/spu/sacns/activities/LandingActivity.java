package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import za.ac.spu.sacns.R;

public class LandingActivity extends AppCompatActivity {

    private Button btnProspective;
    private Button btnCurrent;
    private Button btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);

        initializeViews();
        setupRoleButtons();
    }

    private void initializeViews() {

        btnProspective = findViewById(R.id.btnLandingProspective);
        btnCurrent = findViewById(R.id.btnLandingCurrent);
        btnAdmin = findViewById(R.id.btnLandingAdmin);
    }

    private void setupRoleButtons() {

        btnProspective.setOnClickListener(v ->
                openLogin("Prospective")
        );

        btnCurrent.setOnClickListener(v ->
                openLogin("Current")
        );

        btnAdmin.setOnClickListener(v ->
                openLogin("Admin")
        );
    }

    private void openLogin(String role) {

        Intent intent = new Intent(
                LandingActivity.this,
                LoginActivity.class
        );

        intent.putExtra("USER_ROLE", role);

        startActivity(intent);
    }
}
