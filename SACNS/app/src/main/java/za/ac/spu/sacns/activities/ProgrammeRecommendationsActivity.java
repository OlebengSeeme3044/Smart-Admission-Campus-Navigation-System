package za.ac.spu.sacns.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.adapters.ProgrammeRecommendationAdapter;
import za.ac.spu.sacns.data.DataRepository;
import za.ac.spu.sacns.models.Programme;
import za.ac.spu.sacns.utils.ApsProspectusCalculator;

/**
 * Sol Plaatje University - Prospective Student Programme Recommendations
 * Implements the 2026 Undergraduate Prospectus APS Calculator and recommendations across all faculties.
 */
public class ProgrammeRecommendationsActivity extends AppCompatActivity implements DataRepository.DataChangeListener {

    private DataRepository repository;
    private int userCalculatedAps = 35;
    private String selectedFacultyFilter = "ALL";
    private String selectedStatusFilter = "ALL";
    private String searchQuery = "";

    // Header Views
    private ImageButton btnRecsBack;
    private Button btnRecsHelp;

    // Calculator Views
    private TextView tvToggleCalcView;
    private LinearLayout layoutSubjectInputs;
    private Button btnPresetStem;
    private Button btnPresetCommerce;
    private Button btnPresetEducation;
    private Button btnPresetDiploma;

    private Spinner spinnerEnglishType;
    private EditText etMarkEnglish;
    private TextView tvPtsEnglish;

    private Spinner spinnerMathsType;
    private EditText etMarkMaths;
    private TextView tvPtsMaths;

    private EditText etMarkLO;
    private TextView tvPtsLO;

    private Spinner spinnerSub4;
    private EditText etMarkSub4;
    private TextView tvPtsSub4;

    private Spinner spinnerSub5;
    private EditText etMarkSub5;
    private TextView tvPtsSub5;

    private Spinner spinnerSub6;
    private EditText etMarkSub6;
    private TextView tvPtsSub6;

    private Spinner spinnerSub7;
    private EditText etMarkSub7;
    private TextView tvPtsSub7;

    private Button btnRecalculateAps;

    // APS Result Badges
    private TextView tvTotalCalculatedAps;
    private TextView tvBasePointsBadge;
    private TextView tvMathBonusBadge;
    private TextView tvLangBonusBadge;
    private TextView tvLoPointsBadge;
    private TextView tvEnglishRequirementStatus;

    // Recommendations & Filters
    private LinearLayout cardQualified;
    private LinearLayout cardBorderline;
    private LinearLayout cardIneligible;
    private TextView tvCountQualified;
    private TextView tvCountBorderline;
    private TextView tvCountIneligible;

    private Button chipFacultyAll;
    private Button chipFacultyEducation;
    private Button chipFacultyEMS;
    private Button chipFacultyHumanities;
    private Button chipFacultyNAS;

    private EditText etSearchRecs;
    private RecyclerView rvProgrammeRecommendations;
    private ProgrammeRecommendationAdapter adapter;

    private List<Programme> allProgrammes = new ArrayList<>();
    private final String[] ELECTIVE_SUBJECTS = {
            "Physical Sciences",
            "Life Sciences",
            "Accounting",
            "Business Studies",
            "Economics",
            "Geography",
            "History",
            "Agricultural Sciences",
            "Information Technology",
            "Computer Applications Tech (CAT)",
            "Afrikaans (Home Language)",
            "Afrikaans (First Additional)",
            "Setswana (Home Language)",
            "Setswana (First Additional)",
            "isiXhosa (Home Language)",
            "isiXhosa (First Additional)",
            "Engineering Graphics & Design (EGD)",
            "Tourism",
            "Consumer Studies",
            "Hospitality Studies",
            "Dramatic Arts"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_recommendations);

        repository = DataRepository.getInstance(this);
        repository.addListener(this);

        initializeViews();
        setupSpinners();
        setupPresetButtons();
        setupCalculatorListeners();
        setupFacultyFilterButtons();
        setupStatusFilterClicks();
        setupSearchFilter();
        setupRecyclerView();

        // Calculate initial score and display recommendations
        calculateApsScoreAndFilter();
    }

    private void initializeViews() {
        btnRecsBack = findViewById(R.id.btnRecsBack);
        btnRecsHelp = findViewById(R.id.btnRecsHelp);

        tvToggleCalcView = findViewById(R.id.tvToggleCalcView);
        layoutSubjectInputs = findViewById(R.id.layoutSubjectInputs);

        btnPresetStem = findViewById(R.id.btnPresetStem);
        btnPresetCommerce = findViewById(R.id.btnPresetCommerce);
        btnPresetEducation = findViewById(R.id.btnPresetEducation);
        btnPresetDiploma = findViewById(R.id.btnPresetDiploma);

        spinnerEnglishType = findViewById(R.id.spinnerEnglishType);
        etMarkEnglish = findViewById(R.id.etMarkEnglish);
        tvPtsEnglish = findViewById(R.id.tvPtsEnglish);

        spinnerMathsType = findViewById(R.id.spinnerMathsType);
        etMarkMaths = findViewById(R.id.etMarkMaths);
        tvPtsMaths = findViewById(R.id.tvPtsMaths);

        etMarkLO = findViewById(R.id.etMarkLO);
        tvPtsLO = findViewById(R.id.tvPtsLO);

        spinnerSub4 = findViewById(R.id.spinnerSub4);
        etMarkSub4 = findViewById(R.id.etMarkSub4);
        tvPtsSub4 = findViewById(R.id.tvPtsSub4);

        spinnerSub5 = findViewById(R.id.spinnerSub5);
        etMarkSub5 = findViewById(R.id.etMarkSub5);
        tvPtsSub5 = findViewById(R.id.tvPtsSub5);

        spinnerSub6 = findViewById(R.id.spinnerSub6);
        etMarkSub6 = findViewById(R.id.etMarkSub6);
        tvPtsSub6 = findViewById(R.id.tvPtsSub6);

        spinnerSub7 = findViewById(R.id.spinnerSub7);
        etMarkSub7 = findViewById(R.id.etMarkSub7);
        tvPtsSub7 = findViewById(R.id.tvPtsSub7);

        btnRecalculateAps = findViewById(R.id.btnRecalculateAps);

        tvTotalCalculatedAps = findViewById(R.id.tvTotalCalculatedAps);
        tvBasePointsBadge = findViewById(R.id.tvBasePointsBadge);
        tvMathBonusBadge = findViewById(R.id.tvMathBonusBadge);
        tvLangBonusBadge = findViewById(R.id.tvLangBonusBadge);
        tvLoPointsBadge = findViewById(R.id.tvLoPointsBadge);
        tvEnglishRequirementStatus = findViewById(R.id.tvEnglishRequirementStatus);

        cardQualified = findViewById(R.id.cardQualified);
        cardBorderline = findViewById(R.id.cardBorderline);
        cardIneligible = findViewById(R.id.cardIneligible);
        tvCountQualified = findViewById(R.id.tvCountQualified);
        tvCountBorderline = findViewById(R.id.tvCountBorderline);
        tvCountIneligible = findViewById(R.id.tvCountIneligible);

        chipFacultyAll = findViewById(R.id.chipFacultyAll);
        chipFacultyEducation = findViewById(R.id.chipFacultyEducation);
        chipFacultyEMS = findViewById(R.id.chipFacultyEMS);
        chipFacultyHumanities = findViewById(R.id.chipFacultyHumanities);
        chipFacultyNAS = findViewById(R.id.chipFacultyNAS);

        etSearchRecs = findViewById(R.id.etSearchRecs);
        rvProgrammeRecommendations = findViewById(R.id.rvProgrammeRecommendations);

        btnRecsBack.setOnClickListener(v -> finish());
        btnRecsHelp.setOnClickListener(v -> showProspectusRulesDialog());

        tvToggleCalcView.setOnClickListener(v -> {
            if (layoutSubjectInputs.getVisibility() == View.VISIBLE) {
                layoutSubjectInputs.setVisibility(View.GONE);
                tvToggleCalcView.setText("Adjust Marks ▶");
            } else {
                layoutSubjectInputs.setVisibility(View.VISIBLE);
                tvToggleCalcView.setText("Adjust Marks ▼");
            }
        });
    }

    private void setupSpinners() {
        String[] englishTypes = {"English Home Language (HL)", "English 1st Additional (FAL)"};
        ArrayAdapter<String> engAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, englishTypes);
        spinnerEnglishType.setAdapter(engAdapter);

        String[] mathsTypes = {"Mathematics (Pure)", "Mathematical Literacy"};
        ArrayAdapter<String> mathAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mathsTypes);
        spinnerMathsType.setAdapter(mathAdapter);

        ArrayAdapter<String> electivesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ELECTIVE_SUBJECTS);
        spinnerSub4.setAdapter(electivesAdapter);
        spinnerSub5.setAdapter(electivesAdapter);
        spinnerSub6.setAdapter(electivesAdapter);
        spinnerSub7.setAdapter(electivesAdapter);

        spinnerSub4.setSelection(0); // Physical Sciences
        spinnerSub5.setSelection(1); // Life Sciences
        spinnerSub6.setSelection(8); // IT
        spinnerSub7.setSelection(5); // Geography
    }

    private void setupPresetButtons() {
        btnPresetStem.setOnClickListener(v -> {
            spinnerEnglishType.setSelection(1); // FAL
            etMarkEnglish.setText("65");
            spinnerMathsType.setSelection(0); // Pure Maths
            etMarkMaths.setText("74");
            etMarkLO.setText("76");
            spinnerSub4.setSelection(0); // Physical Sciences
            etMarkSub4.setText("70");
            spinnerSub5.setSelection(1); // Life Sciences
            etMarkSub5.setText("68");
            spinnerSub6.setSelection(8); // IT
            etMarkSub6.setText("75");
            spinnerSub7.setSelection(5); // Geography
            etMarkSub7.setText("64");
            calculateApsScoreAndFilter();
            Toast.makeText(this, "Loaded STEM Stream Preset", Toast.LENGTH_SHORT).show();
        });

        btnPresetCommerce.setOnClickListener(v -> {
            spinnerEnglishType.setSelection(0); // HL
            etMarkEnglish.setText("70");
            spinnerMathsType.setSelection(0); // Pure Maths
            etMarkMaths.setText("66");
            etMarkLO.setText("74");
            spinnerSub4.setSelection(2); // Accounting
            etMarkSub4.setText("72");
            spinnerSub5.setSelection(4); // Economics
            etMarkSub5.setText("65");
            spinnerSub6.setSelection(3); // Business Studies
            etMarkSub6.setText("68");
            spinnerSub7.setSelection(12); // Setswana HL
            etMarkSub7.setText("62");
            calculateApsScoreAndFilter();
            Toast.makeText(this, "Loaded Commerce Stream Preset", Toast.LENGTH_SHORT).show();
        });

        btnPresetEducation.setOnClickListener(v -> {
            spinnerEnglishType.setSelection(0); // HL
            etMarkEnglish.setText("66");
            spinnerMathsType.setSelection(1); // Maths Lit
            etMarkMaths.setText("65");
            etMarkLO.setText("75");
            spinnerSub4.setSelection(6); // History
            etMarkSub4.setText("72");
            spinnerSub5.setSelection(5); // Geography
            etMarkSub5.setText("68");
            spinnerSub6.setSelection(12); // Setswana HL
            etMarkSub6.setText("64");
            spinnerSub7.setSelection(1); // Life Sciences
            etMarkSub7.setText("58");
            calculateApsScoreAndFilter();
            Toast.makeText(this, "Loaded Education Stream Preset", Toast.LENGTH_SHORT).show();
        });

        btnPresetDiploma.setOnClickListener(v -> {
            spinnerEnglishType.setSelection(1); // FAL
            etMarkEnglish.setText("60");
            spinnerMathsType.setSelection(1); // Maths Lit
            etMarkMaths.setText("58");
            etMarkLO.setText("68");
            spinnerSub4.setSelection(3); // Business Studies
            etMarkSub4.setText("62");
            spinnerSub5.setSelection(2); // Accounting
            etMarkSub5.setText("52");
            spinnerSub6.setSelection(17); // Tourism
            etMarkSub6.setText("64");
            spinnerSub7.setSelection(4); // Economics
            etMarkSub7.setText("54");
            calculateApsScoreAndFilter();
            Toast.makeText(this, "Loaded Diploma Preset", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCalculatorListeners() {
        btnRecalculateAps.setOnClickListener(v -> calculateApsScoreAndFilter());

        AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateApsScoreAndFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerEnglishType.setOnItemSelectedListener(spinListener);
        spinnerMathsType.setOnItemSelectedListener(spinListener);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateApsScoreAndFilter();
            }
        };

        etMarkEnglish.addTextChangedListener(watcher);
        etMarkMaths.addTextChangedListener(watcher);
        etMarkLO.addTextChangedListener(watcher);
        etMarkSub4.addTextChangedListener(watcher);
        etMarkSub5.addTextChangedListener(watcher);
        etMarkSub6.addTextChangedListener(watcher);
        etMarkSub7.addTextChangedListener(watcher);
    }

    private int parseMark(EditText et, int defaultVal) {
        try {
            String txt = et.getText().toString().trim();
            if (txt.isEmpty()) return defaultVal;
            int v = Integer.parseInt(txt);
            return Math.max(0, Math.min(100, v));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private void calculateApsScoreAndFilter() {
        boolean isEngHL = (spinnerEnglishType.getSelectedItemPosition() == 0);
        int engMark = parseMark(etMarkEnglish, 68);

        boolean isPureMaths = (spinnerMathsType.getSelectedItemPosition() == 0);
        int mathMark = parseMark(etMarkMaths, 72);

        int loMark = parseMark(etMarkLO, 75);
        int sub4Mark = parseMark(etMarkSub4, 65);
        int sub5Mark = parseMark(etMarkSub5, 62);
        int sub6Mark = parseMark(etMarkSub6, 60);
        int sub7Mark = parseMark(etMarkSub7, 58);

        String sub4Name = spinnerSub4.getSelectedItem() != null ? spinnerSub4.getSelectedItem().toString() : "Subject 4";
        String sub5Name = spinnerSub5.getSelectedItem() != null ? spinnerSub5.getSelectedItem().toString() : "Subject 5";
        String sub6Name = spinnerSub6.getSelectedItem() != null ? spinnerSub6.getSelectedItem().toString() : "Subject 6";
        String sub7Name = spinnerSub7.getSelectedItem() != null ? spinnerSub7.getSelectedItem().toString() : "Subject 7";

        boolean sub4IsHL = sub4Name.contains("(Home Language)");
        boolean sub5IsHL = sub5Name.contains("(Home Language)");
        boolean sub6IsHL = sub6Name.contains("(Home Language)");
        boolean sub7IsHL = sub7Name.contains("(Home Language)");

        List<ApsProspectusCalculator.SubjectEntry> subjects = new ArrayList<>();
        subjects.add(new ApsProspectusCalculator.SubjectEntry("English", engMark, isEngHL, false, false));
        subjects.add(new ApsProspectusCalculator.SubjectEntry("Mathematics", mathMark, false, isPureMaths, false));
        subjects.add(new ApsProspectusCalculator.SubjectEntry("Life Orientation", loMark, false, false, true));
        subjects.add(new ApsProspectusCalculator.SubjectEntry(sub4Name, sub4Mark, sub4IsHL, false, false));
        subjects.add(new ApsProspectusCalculator.SubjectEntry(sub5Name, sub5Mark, sub5IsHL, false, false));
        subjects.add(new ApsProspectusCalculator.SubjectEntry(sub6Name, sub6Mark, sub6IsHL, false, false));
        subjects.add(new ApsProspectusCalculator.SubjectEntry(sub7Name, sub7Mark, sub7IsHL, false, false));

        ApsProspectusCalculator.ApsResult result = ApsProspectusCalculator.calculate(subjects, isEngHL, engMark);

        userCalculatedAps = result.totalAps;
        repository.setUserAps(userCalculatedAps);

        // Update individual subject point displays
        tvPtsEnglish.setText(result.subjects.get(0).points + " pts");
        tvPtsMaths.setText(result.subjects.get(1).points + " pts");
        tvPtsLO.setText(result.subjects.get(2).points + " pts");
        tvPtsSub4.setText(result.subjects.get(3).points + " pts");
        tvPtsSub5.setText(result.subjects.get(4).points + " pts");
        tvPtsSub6.setText(result.subjects.get(5).points + " pts");
        tvPtsSub7.setText(result.subjects.get(6).points + " pts");

        // Update badges
        tvTotalCalculatedAps.setText(userCalculatedAps + " POINTS");
        tvBasePointsBadge.setText("Base: " + result.basePoints + " pts");
        tvMathBonusBadge.setText("Maths +" + result.mathBonusPoints);
        tvLangBonusBadge.setText("HL Lang +" + result.languageBonusPoints);
        tvLoPointsBadge.setText("LO: " + result.lifeOrientationPoints + " pts");

        if (result.englishRequirementMet) {
            tvEnglishRequirementStatus.setText("✓ " + result.englishFeedback);
            tvEnglishRequirementStatus.setTextColor(Color.parseColor("#166534"));
        } else {
            tvEnglishRequirementStatus.setText("⚠️ " + result.englishFeedback);
            tvEnglishRequirementStatus.setTextColor(Color.parseColor("#B91C1C"));
        }

        renderFilteredRecommendations();
    }

    private void setupFacultyFilterButtons() {
        View.OnClickListener clickListener = v -> {
            resetFacultyChipStyles();
            Button b = (Button) v;
            b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sacns_blue)));
            b.setTextColor(Color.WHITE);

            int id = v.getId();
            if (id == R.id.chipFacultyEducation) {
                selectedFacultyFilter = "EDUCATION";
            } else if (id == R.id.chipFacultyEMS) {
                selectedFacultyFilter = "EMS";
            } else if (id == R.id.chipFacultyHumanities) {
                selectedFacultyFilter = "HUMANITIES";
            } else if (id == R.id.chipFacultyNAS) {
                selectedFacultyFilter = "NAS";
            } else {
                selectedFacultyFilter = "ALL";
            }

            renderFilteredRecommendations();
        };

        chipFacultyAll.setOnClickListener(clickListener);
        chipFacultyEducation.setOnClickListener(clickListener);
        chipFacultyEMS.setOnClickListener(clickListener);
        chipFacultyHumanities.setOnClickListener(clickListener);
        chipFacultyNAS.setOnClickListener(clickListener);
    }

    private void resetFacultyChipStyles() {
        int defaultBg = Color.parseColor("#F1F5F9");
        int defaultText = Color.parseColor("#0F172A");

        chipFacultyAll.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
        chipFacultyAll.setTextColor(defaultText);

        chipFacultyEducation.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
        chipFacultyEducation.setTextColor(defaultText);

        chipFacultyEMS.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
        chipFacultyEMS.setTextColor(defaultText);

        chipFacultyHumanities.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
        chipFacultyHumanities.setTextColor(defaultText);

        chipFacultyNAS.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
        chipFacultyNAS.setTextColor(defaultText);
    }

    private void setupStatusFilterClicks() {
        cardQualified.setOnClickListener(v -> {
            selectedStatusFilter = "qualified".equalsIgnoreCase(selectedStatusFilter) ? "ALL" : "qualified";
            renderFilteredRecommendations();
        });

        cardBorderline.setOnClickListener(v -> {
            selectedStatusFilter = "borderline".equalsIgnoreCase(selectedStatusFilter) ? "ALL" : "borderline";
            renderFilteredRecommendations();
        });

        cardIneligible.setOnClickListener(v -> {
            selectedStatusFilter = "ineligible".equalsIgnoreCase(selectedStatusFilter) ? "ALL" : "ineligible";
            renderFilteredRecommendations();
        });
    }

    private void setupSearchFilter() {
        etSearchRecs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim() : "";
                renderFilteredRecommendations();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        rvProgrammeRecommendations.setLayoutManager(new LinearLayoutManager(this));
        allProgrammes = repository.getProgrammes();
        adapter = new ProgrammeRecommendationAdapter(this, new ArrayList<>(), userCalculatedAps, (programme, status) -> {
            showProgrammeDetailsDialog(programme, status);
        });
        rvProgrammeRecommendations.setAdapter(adapter);
    }

    private void renderFilteredRecommendations() {
        allProgrammes = repository.getProgrammes();
        List<Programme> filtered = new ArrayList<>();

        int qCount = 0;
        int bCount = 0;
        int iCount = 0;

        for (Programme p : allProgrammes) {
            String status = p.getEligibilityStatus(userCalculatedAps);
            if ("qualified".equals(status)) qCount++;
            else if ("borderline".equals(status)) bCount++;
            else iCount++;

            // Faculty check
            boolean matchesFaculty = true;
            if ("EDUCATION".equals(selectedFacultyFilter)) {
                matchesFaculty = p.getDepartment().toLowerCase().contains("education");
            } else if ("EMS".equals(selectedFacultyFilter)) {
                matchesFaculty = p.getDepartment().toLowerCase().contains("economic") || p.getDepartment().toLowerCase().contains("management");
            } else if ("HUMANITIES".equals(selectedFacultyFilter)) {
                matchesFaculty = p.getDepartment().toLowerCase().contains("humanities");
            } else if ("NAS".equals(selectedFacultyFilter)) {
                matchesFaculty = p.getDepartment().toLowerCase().contains("natural");
            }

            // Status filter check
            boolean matchesStatus = true;
            if (!"ALL".equalsIgnoreCase(selectedStatusFilter)) {
                matchesStatus = selectedStatusFilter.equalsIgnoreCase(status);
            }

            // Search query check
            boolean matchesSearch = true;
            if (!searchQuery.isEmpty()) {
                String q = searchQuery.toLowerCase();
                matchesSearch = p.getName().toLowerCase().contains(q) ||
                        p.getDepartment().toLowerCase().contains(q) ||
                        p.getCareerOpportunities().toLowerCase().contains(q) ||
                        p.getRequirements().toLowerCase().contains(q);
            }

            if (matchesFaculty && matchesStatus && matchesSearch) {
                filtered.add(p);
            }
        }

        tvCountQualified.setText(String.valueOf(qCount));
        tvCountBorderline.setText(String.valueOf(bCount));
        tvCountIneligible.setText(String.valueOf(iCount));

        adapter.updateData(filtered, userCalculatedAps);
    }

    private void showProgrammeDetailsDialog(Programme programme, String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(programme.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("🏛 Faculty: ").append(programme.getDepartment()).append("\n\n");
        sb.append("🎓 Qualification: ").append(programme.getQualificationType()).append(" (").append(programme.getNqfLevel()).append(")\n");
        if (programme.getSaqaId() != null && !programme.getSaqaId().isEmpty()) {
            sb.append("🆔 SAQA ID: ").append(programme.getSaqaId()).append("\n");
        }
        sb.append("⏱ Study Duration: ").append(programme.getDuration()).append("\n");
        sb.append("📊 Minimum APS Required: ").append(programme.getMinAps()).append(" points\n");
        sb.append("🎯 Your SPU APS Score: ").append(userCalculatedAps).append(" points\n\n");

        if ("qualified".equals(status)) {
            sb.append("✅ STATUS: QUALIFIED\nYou meet the minimum APS requirement for this programme!\n\n");
        } else if ("borderline".equals(status)) {
            sb.append("⚠️ STATUS: BORDERLINE\nYou are within 2 points of the minimum requirement. Admission may be subject to faculty selection committee review.\n\n");
        } else {
            sb.append("❌ STATUS: NOT CURRENTLY ELIGIBLE\nYour score is below the minimum APS threshold for this offering.\n\n");
        }

        sb.append("📋 Minimum Admission Requirements (2026 Prospectus):\n");
        sb.append(programme.getRequirements()).append("\n\n");

        if (programme.getCareerOpportunities() != null && !programme.getCareerOpportunities().isEmpty()) {
            sb.append("💼 Career Opportunities:\n");
            sb.append(programme.getCareerOpportunities()).append("\n");
        }

        builder.setMessage(sb.toString());
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showProspectusRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SPU 2026 Prospectus APS Rules");
        builder.setMessage(
                "How to Calculate SPU Admission Point Score (Prospectus Page 4):\n\n" +
                "• SPU Points Scale:\n" +
                "  Level 7 (90-100%): 8 points\n" +
                "  Level 7 (80-89%): 7 points\n" +
                "  Level 6 (70-79%): 6 points\n" +
                "  Level 5 (60-69%): 5 points\n" +
                "  Level 4 (50-59%): 4 points\n" +
                "  Level 3 (40-49%): 3 points\n" +
                "  Level 2 (30-39%): 2 points\n" +
                "  Level 1 (0-29%): 1 point\n\n" +
                "• Mathematics & Home Language Bonus:\n" +
                "  70-100%: +2 additional points\n" +
                "  40-69%: +1 additional point\n" +
                "  (Applies to Pure Mathematics and any official Home Language)\n\n" +
                "• Life Orientation Weighting:\n" +
                "  90-100%: 4 points\n" +
                "  80-89%: 3 points\n" +
                "  70-79%: 2 points\n" +
                "  60-69%: 1 point\n" +
                "  Below 60%: 0 points\n\n" +
                "• General English Minimum Requirement:\n" +
                "  English Home Language (HL): Level 4 (50%+) OR\n" +
                "  English First Additional Language (FAL): Level 5 (60%+)."
        );
        builder.setPositiveButton("Got It", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(this::renderFilteredRecommendations);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.removeListener(this);
        }
    }
}
