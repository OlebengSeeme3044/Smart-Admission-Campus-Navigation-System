package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.data.DataRepository;

public class ApsCalculatorActivity extends AppCompatActivity {

    private EditText[] etSubjects = new EditText[6];
    private Spinner[] spLevels = new Spinner[6];

    private Button btnCalculateAPS;
    private LinearLayout layoutApsResult;
    private TextView tvTotalApsScore;
    private Button btnViewRecommendations;

    private int calculatedAps = 35;
    private DataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aps_calculator);

        repository = DataRepository.getInstance(this);

        initializeViews();
        setupSpinners();
        setupActions();
    }

    private void initializeViews() {
        etSubjects[0] = findViewById(R.id.etSubject1);
        etSubjects[1] = findViewById(R.id.etSubject2);
        etSubjects[2] = findViewById(R.id.etSubject3);
        etSubjects[3] = findViewById(R.id.etSubject4);
        etSubjects[4] = findViewById(R.id.etSubject5);
        etSubjects[5] = findViewById(R.id.etSubject6);

        spLevels[0] = findViewById(R.id.spLevel1);
        spLevels[1] = findViewById(R.id.spLevel2);
        spLevels[2] = findViewById(R.id.spLevel3);
        spLevels[3] = findViewById(R.id.spLevel4);
        spLevels[4] = findViewById(R.id.spLevel5);
        spLevels[5] = findViewById(R.id.spLevel6);

        btnCalculateAPS = findViewById(R.id.btnCalculateAPS);
        layoutApsResult = findViewById(R.id.layoutApsResult);
        tvTotalApsScore = findViewById(R.id.tvTotalApsScore);
        btnViewRecommendations = findViewById(R.id.btnViewRecommendations);
    }

    private void setupSpinners() {
        String[] levels = {
                "Level 7 (80-100%)",
                "Level 6 (70-79%)",
                "Level 5 (60-69%)",
                "Level 4 (50-59%)",
                "Level 3 (40-49%)",
                "Level 2 (30-39%)",
                "Level 1 (0-29%)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levels);

        for (int i = 0; i < spLevels.length; i++) {
            if (spLevels[i] != null) {
                spLevels[i].setAdapter(adapter);
                // Pre-select levels that give 35 total points default (Level 7 = index 0)
                spLevels[i].setSelection(0);
            }
        }
    }

    private void setupActions() {
        btnCalculateAPS.setOnClickListener(v -> calculateAps());

        btnViewRecommendations.setOnClickListener(v -> {
            Intent intent = new Intent(ApsCalculatorActivity.this, ProgrammeRecommendationsActivity.class);
            intent.putExtra("USER_APS", calculatedAps);
            startActivity(intent);
        });
    }

    private void calculateAps() {
        List<Integer> scores = new ArrayList<>();

        for (Spinner sp : spLevels) {
            if (sp != null) {
                int selectedIndex = sp.getSelectedItemPosition();
                int score = 7 - selectedIndex; // Index 0 is Level 7, Index 6 is Level 1
                scores.add(score);
            }
        }

        // Sort descending and sum top 5 subjects
        Collections.sort(scores, Collections.reverseOrder());
        int sum = 0;
        int count = Math.min(5, scores.size());
        for (int i = 0; i < count; i++) {
            sum += scores.get(i);
        }

        calculatedAps = sum;
        repository.setUserAps(calculatedAps);

        tvTotalApsScore.setText(String.valueOf(calculatedAps));
        layoutApsResult.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Calculated Total APS: " + calculatedAps + " points!", Toast.LENGTH_SHORT).show();
    }
}
