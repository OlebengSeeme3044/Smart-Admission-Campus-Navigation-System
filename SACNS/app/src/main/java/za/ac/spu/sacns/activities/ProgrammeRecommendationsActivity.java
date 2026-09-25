package za.ac.spu.sacns.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.adapters.ProgrammeRecommendationAdapter;
import za.ac.spu.sacns.data.DataRepository;
import za.ac.spu.sacns.models.Programme;

public class ProgrammeRecommendationsActivity extends AppCompatActivity implements DataRepository.DataChangeListener {

    private DataRepository repository;
    private int userAps = 35;
    private String currentFilter = "all"; // "all", "qualified", "borderline", "ineligible"

    // Views
    private ImageButton btnRecsBack;
    private Button btnRecsHelp;
    private TextView tvApsCaption;
    private SeekBar sbApsScore;
    private TextView tvSliderApsVal;

    // Status Summary Cards
    private LinearLayout cardQualified;
    private LinearLayout cardBorderline;
    private LinearLayout cardIneligible;
    private TextView tvCountQualified;
    private TextView tvCountBorderline;
    private TextView tvCountIneligible;

    // Filter Chips
    private Button chipFilterAll;
    private Button chipFilterQualified;
    private Button chipFilterBorderline;
    private Button chipFilterIneligible;

    // Search & Recycler
    private EditText etSearchRecs;
    private RecyclerView rvProgrammeRecommendations;
    private ProgrammeRecommendationAdapter adapter;

    private List<Programme> allProgrammes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_recommendations);

        repository = DataRepository.getInstance(this);
        repository.addListener(this);

        int intentAps = getIntent().getIntExtra("USER_APS", -1);
        if (intentAps > 0) {
            userAps = intentAps;
            repository.setUserAps(userAps);
        } else {
            userAps = repository.getUserAps();
        }

        initializeViews();
        setupSlider();
        setupFilterClicks();
        setupSearchFilter();
        setupRecyclerView();
        recalculateAndRender();
    }

    private void initializeViews() {
        btnRecsBack = findViewById(R.id.btnRecsBack);
        btnRecsHelp = findViewById(R.id.btnRecsHelp);
        tvApsCaption = findViewById(R.id.tvApsCaption);
        sbApsScore = findViewById(R.id.sbApsScore);
        tvSliderApsVal = findViewById(R.id.tvSliderApsVal);

        cardQualified = findViewById(R.id.cardQualified);
        cardBorderline = findViewById(R.id.cardBorderline);
        cardIneligible = findViewById(R.id.cardIneligible);
        tvCountQualified = findViewById(R.id.tvCountQualified);
        tvCountBorderline = findViewById(R.id.tvCountBorderline);
        tvCountIneligible = findViewById(R.id.tvCountIneligible);

        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterQualified = findViewById(R.id.chipFilterQualified);
        chipFilterBorderline = findViewById(R.id.chipFilterBorderline);
        chipFilterIneligible = findViewById(R.id.chipFilterIneligible);

        etSearchRecs = findViewById(R.id.etSearchRecs);
        rvProgrammeRecommendations = findViewById(R.id.rvProgrammeRecommendations);

        btnRecsBack.setOnClickListener(v -> finish());
        btnRecsHelp.setOnClickListener(v -> showApsHelpGuideDialog());
    }

    private void setupSlider() {
        sbApsScore.setMax(50);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            sbApsScore.setMin(15);
        }
        sbApsScore.setProgress(userAps);
        tvSliderApsVal.setText(String.valueOf(userAps));
        tvApsCaption.setText("Based on your APS score of " + userAps + " points");

        sbApsScore.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int adjusted = Math.max(15, progress);
                userAps = adjusted;
                tvSliderApsVal.setText(String.valueOf(userAps));
                tvApsCaption.setText("Based on your APS score of " + userAps + " points");
                repository.setUserAps(userAps);
                recalculateAndRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupFilterClicks() {
        cardQualified.setOnClickListener(v -> setFilter("qualified"));
        cardBorderline.setOnClickListener(v -> setFilter("borderline"));
        cardIneligible.setOnClickListener(v -> setFilter("ineligible"));

        chipFilterAll.setOnClickListener(v -> setFilter("all"));
        chipFilterQualified.setOnClickListener(v -> setFilter("qualified"));
        chipFilterBorderline.setOnClickListener(v -> setFilter("borderline"));
        chipFilterIneligible.setOnClickListener(v -> setFilter("ineligible"));
    }

    private void setFilter(String filter) {
        this.currentFilter = filter;
        updateChipVisuals();
        filterAndDisplayList();
    }

    private void updateChipVisuals() {
        int navyColor = Color.parseColor("#0B324F");
        int grayBg = Color.parseColor("#F1F5F9");
        int whiteText = Color.WHITE;
        int grayText = Color.parseColor("#475569");

        chipFilterAll.setBackgroundTintList(ColorStateList.valueOf("all".equals(currentFilter) ? navyColor : grayBg));
        chipFilterAll.setTextColor("all".equals(currentFilter) ? whiteText : grayText);

        chipFilterQualified.setBackgroundTintList(ColorStateList.valueOf("qualified".equals(currentFilter) ? navyColor : grayBg));
        chipFilterQualified.setTextColor("qualified".equals(currentFilter) ? whiteText : grayText);

        chipFilterBorderline.setBackgroundTintList(ColorStateList.valueOf("borderline".equals(currentFilter) ? navyColor : grayBg));
        chipFilterBorderline.setTextColor("borderline".equals(currentFilter) ? whiteText : grayText);

        chipFilterIneligible.setBackgroundTintList(ColorStateList.valueOf("ineligible".equals(currentFilter) ? navyColor : grayBg));
        chipFilterIneligible.setTextColor("ineligible".equals(currentFilter) ? whiteText : grayText);
    }

    private void setupSearchFilter() {
        etSearchRecs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        rvProgrammeRecommendations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProgrammeRecommendationAdapter(this, new ArrayList<>(), userAps, (programme, status) -> {
            showProgrammeRequirementModal(programme, status);
        });
        rvProgrammeRecommendations.setAdapter(adapter);
    }

    private void recalculateAndRender() {
        allProgrammes = repository.getProgrammes();

        int qualified = 0;
        int borderline = 0;
        int ineligible = 0;

        for (Programme p : allProgrammes) {
            String status = p.getEligibilityStatus(userAps);
            if ("qualified".equals(status)) qualified++;
            else if ("borderline".equals(status)) borderline++;
            else ineligible++;
        }

        tvCountQualified.setText(String.valueOf(qualified));
        tvCountBorderline.setText(String.valueOf(borderline));
        tvCountIneligible.setText(String.valueOf(ineligible));

        chipFilterAll.setText("All Programmes (" + allProgrammes.size() + ")");
        chipFilterQualified.setText("Qualified (" + qualified + ")");
        chipFilterBorderline.setText("Borderline (" + borderline + ")");
        chipFilterIneligible.setText("Not Eligible (" + ineligible + ")");

        filterAndDisplayList();
    }

    private void filterAndDisplayList() {
        String searchQuery = etSearchRecs.getText().toString().toLowerCase().trim();
        List<Programme> filtered = new ArrayList<>();

        for (Programme p : allProgrammes) {
            String status = p.getEligibilityStatus(userAps);

            // Filter by category
            if (!"all".equals(currentFilter) && !status.equals(currentFilter)) {
                continue;
            }

            // Filter by search query
            if (!searchQuery.isEmpty()) {
                boolean matches = p.getName().toLowerCase().contains(searchQuery) ||
                        p.getDepartment().toLowerCase().contains(searchQuery) ||
                        p.getRequirements().toLowerCase().contains(searchQuery);
                if (!matches) continue;
            }

            filtered.add(p);
        }

        // Sort: Qualified (1), then Borderline (2), then Ineligible (3)
        Collections.sort(filtered, new Comparator<Programme>() {
            @Override
            public int compare(Programme p1, Programme p2) {
                int score1 = getStatusOrder(p1.getEligibilityStatus(userAps));
                int score2 = getStatusOrder(p2.getEligibilityStatus(userAps));
                return Integer.compare(score1, score2);
            }

            private int getStatusOrder(String status) {
                if ("qualified".equals(status)) return 1;
                if ("borderline".equals(status)) return 2;
                return 3;
            }
        });

        adapter.updateData(filtered, userAps);
    }

    private void showProgrammeRequirementModal(Programme programme, String status) {
        int diff = userAps - programme.getMinAps();
        String advice;
        String statusTitle;

        int iconRes;
        if ("qualified".equals(status)) {
            statusTitle = "QUALIFIED (Meets Minimum Requirements)";
            iconRes = R.drawable.ic_check_circle;
            advice = "Your APS score of " + userAps + " exceeds the minimum requirement of " + programme.getMinAps() + " by " + diff + " points. You are in a strong position to apply for this qualification.";
        } else if ("borderline".equals(status)) {
            statusTitle = "BORDERLINE (Within 2 Points)";
            iconRes = R.drawable.ic_warning_circle;
            advice = "You are only " + Math.abs(diff) + " point(s) below the minimum requirement (" + programme.getMinAps() + "). SPU may consider borderline applicants based on remaining capacity, or you may qualify for extended foundation programmes.";
        } else {
            statusTitle = "NOT ELIGIBLE (Below Requirement)";
            iconRes = R.drawable.ic_cancel_circle;
            advice = "Your APS score of " + userAps + " is " + Math.abs(diff) + " points below the minimum (" + programme.getMinAps() + "). We recommend considering related diploma courses, upgrading subject results, or exploring alternative humanities/management disciplines.";
        }

        new AlertDialog.Builder(this)
                .setTitle(programme.getName())
                .setIcon(iconRes)
                .setMessage(statusTitle + "\n\n"
                        + "• Faculty: " + programme.getDepartment() + "\n"
                        + "• Duration: " + programme.getDuration() + "\n"
                        + "• Required APS: " + programme.getMinAps() + " points\n"
                        + "• Your APS: " + userAps + " points\n\n"
                        + "Detailed Admission Requirements:\n" + programme.getRequirements() + "\n\n"
                        + "Admissions Advice:\n" + advice)
                .setPositiveButton("Save To My Choices", (dialog, which) -> {
                    Toast.makeText(this, "Saved \"" + programme.getName() + "\" to your selected choices!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showApsHelpGuideDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Understanding APS & Recommendations")
                .setIcon(R.drawable.ic_graduation_cap)
                .setMessage("Welcome to the Sol Plaatje University Admission Point Score (APS) Advisor!\n\n"
                        + "1. What is an APS Score?\n"
                        + "Your Admission Point Score is calculated from the National Senior Certificate (NSC) achievement levels of your school subjects.\n\n"
                        + "2. Qualification Categories:\n"
                        + "• QUALIFIED (Green): Your score meets or exceeds the required points.\n"
                        + "• BORDERLINE (Amber): You are within 1-2 points of qualifying.\n"
                        + "• NOT ELIGIBLE (Red): Your current score falls short.\n\n"
                        + "3. How to Use This Tool:\n"
                        + "• Drag the slider above to see what programmes open up if you increase your score!\n"
                        + "• Tap any status card (Qualified, Borderline, Not Eligible) to filter the list instantly.\n"
                        + "• Tap any programme card to view detailed entry requirements.")
                .setPositiveButton("Got It!", null)
                .show();
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(this::recalculateAndRender);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.removeListener(this);
    }
}
