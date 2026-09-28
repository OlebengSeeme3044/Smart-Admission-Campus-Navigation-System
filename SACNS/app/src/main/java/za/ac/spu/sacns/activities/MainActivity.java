package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.adapters.DashboardModuleAdapter;
import za.ac.spu.sacns.adapters.FeaturedProgrammeAdapter;
import za.ac.spu.sacns.data.DataRepository;
import za.ac.spu.sacns.models.DashboardModule;
import za.ac.spu.sacns.models.Programme;

public class MainActivity extends AppCompatActivity implements DataRepository.DataChangeListener {

    private DataRepository repository;
    private String currentRole = "Prospective";

    private TextView tvWelcomeUser;
    private TextView tvUserRoleBadge;
    private TextView tvTeaserApsScore;
    private TextView tvTeaserQualifiedCount;
    private Button btnQuickViewRecs;
    private Button btnSwitchRole;
    private Button btnLogout;
    private EditText etSearchProgrammes;
    private TextView tvProgrammeCount;

    private RecyclerView rvDashboardModules;
    private RecyclerView rvFeaturedProgrammes;

    private DashboardModuleAdapter moduleAdapter;
    private FeaturedProgrammeAdapter programmeAdapter;
    private List<Programme> allProgrammes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = DataRepository.getInstance(this);
        repository.addListener(this);

        String roleFromIntent = getIntent().getStringExtra("USER_ROLE");
        if (roleFromIntent != null && !roleFromIntent.isEmpty()) {
            currentRole = roleFromIntent;
            repository.setUserRole(currentRole);
        } else {
            currentRole = repository.getUserRole();
        }

        initializeViews();
        setupDashboardModules();
        setupProgrammesList();
        setupSearchFilter();
        setupRoleAndActions();
        refreshDashboardStats();
    }

    private void initializeViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserRoleBadge = findViewById(R.id.tvUserRoleBadge);
        tvTeaserApsScore = findViewById(R.id.tvTeaserApsScore);
        tvTeaserQualifiedCount = findViewById(R.id.tvTeaserQualifiedCount);
        btnQuickViewRecs = findViewById(R.id.btnQuickViewRecs);
        btnSwitchRole = findViewById(R.id.btnSwitchRole);
        btnLogout = findViewById(R.id.btnLogout);
        etSearchProgrammes = findViewById(R.id.etSearchProgrammes);
        tvProgrammeCount = findViewById(R.id.tvProgrammeCount);
        rvDashboardModules = findViewById(R.id.rvDashboardModules);
        rvFeaturedProgrammes = findViewById(R.id.rvFeaturedProgrammes);

        tvUserRoleBadge.setText(currentRole);
        tvWelcomeUser.setText("Welcome, " + currentRole);
    }

    private void setupDashboardModules() {
        List<DashboardModule> modules = new ArrayList<>();

        modules.add(new DashboardModule(
                "calc",
                "APS Calculator",
                "Calculate your Admission Point Score from your high school marks",
                R.drawable.ic_calculator,
                "Prospective",
                R.color.sacns_blue,
                ApsCalculatorActivity.class
        ));

        modules.add(new DashboardModule(
                "recs",
                "Programme Recommendations",
                "Discover qualifying degree and diploma programmes based on your APS",
                R.drawable.ic_graduation_cap,
                "Admissions",
                R.color.green_base,
                ProgrammeRecommendationsActivity.class
        ));

        modules.add(new DashboardModule(
                "map",
                "Campus Navigation",
                "Find lecture halls, computer labs, library, and route walking times",
                R.drawable.ic_location_pin,
                "Current & Guests",
                R.color.spu_blue_accent,
                CampusNavigationActivity.class
        ));

        modules.add(new DashboardModule(
                "admin",
                "Admin Control Panel",
                "Manage academic programmes, APS thresholds, and campus venue coordinates",
                R.drawable.ic_admin_settings,
                "Administrator",
                R.color.spu_navy,
                AdminPanelActivity.class
        ));

        rvDashboardModules.setLayoutManager(new LinearLayoutManager(this));
        moduleAdapter = new DashboardModuleAdapter(this, modules, module -> {
            if ("admin".equals(module.getId()) && !"Admin".equalsIgnoreCase(currentRole)) {
                Toast.makeText(this, "Opening Admin Panel (Administrator View)", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(MainActivity.this, module.getTargetActivityClass());
            intent.putExtra("USER_ROLE", currentRole);
            startActivity(intent);
        });
        rvDashboardModules.setAdapter(moduleAdapter);
    }

    private void setupProgrammesList() {
        allProgrammes = repository.getProgrammes();
        rvFeaturedProgrammes.setLayoutManager(new LinearLayoutManager(this));

        programmeAdapter = new FeaturedProgrammeAdapter(this, allProgrammes, programme -> {
            showProgrammeDetailsDialog(programme);
        });
        rvFeaturedProgrammes.setAdapter(programmeAdapter);
        updateProgrammeCountText(allProgrammes.size());
    }

    private void setupSearchFilter() {
        etSearchProgrammes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProgrammes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProgrammes(String query) {
        if (query == null || query.trim().isEmpty()) {
            programmeAdapter.updateData(allProgrammes);
            updateProgrammeCountText(allProgrammes.size());
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Programme> filtered = new ArrayList<>();
        for (Programme p : allProgrammes) {
            if (p.getName().toLowerCase().contains(lowerQuery) ||
                p.getDepartment().toLowerCase().contains(lowerQuery)) {
                filtered.add(p);
            }
        }
        programmeAdapter.updateData(filtered);
        updateProgrammeCountText(filtered.size());
    }

    private void updateProgrammeCountText(int count) {
        tvProgrammeCount.setText(count + (count == 1 ? " Programme" : " Programmes"));
    }

    private void setupRoleAndActions() {
        btnQuickViewRecs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProgrammeRecommendationsActivity.class);
            intent.putExtra("USER_ROLE", currentRole);
            startActivity(intent);
        });

        btnSwitchRole.setOnClickListener(v -> showRoleSwitchDialog());

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showRoleSwitchDialog() {
        String[] roles = {"Prospective Student", "Current Student", "Administrator"};
        new AlertDialog.Builder(this)
                .setTitle("Switch Active Role")
                .setItems(roles, (dialog, which) -> {
                    String selected = roles[which];
                    if (selected.startsWith("Prospective")) currentRole = "Prospective";
                    else if (selected.startsWith("Current")) currentRole = "Current";
                    else currentRole = "Admin";

                    repository.setUserRole(currentRole);
                    tvUserRoleBadge.setText(currentRole);
                    tvWelcomeUser.setText("Welcome, " + currentRole);
                    Toast.makeText(this, "Switched role to " + currentRole, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProgrammeDetailsDialog(Programme programme) {
        int userAps = repository.getUserAps();
        String status = programme.getEligibilityStatus(userAps);
        String statusFormatted = status.toUpperCase();

        new AlertDialog.Builder(this)
                .setTitle(programme.getName())
                .setMessage("Faculty: " + programme.getDepartment() + "\n"
                        + "Duration: " + programme.getDuration() + "\n"
                        + "Minimum APS Required: " + programme.getMinAps() + " points\n"
                        + "Your Current APS: " + userAps + " points (" + statusFormatted + ")\n\n"
                        + "Admission Requirements:\n" + programme.getRequirements())
                .setPositiveButton("Check Recommendations", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, ProgrammeRecommendationsActivity.class);
                    intent.putExtra("USER_ROLE", currentRole);
                    startActivity(intent);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void refreshDashboardStats() {
        allProgrammes = repository.getProgrammes();
        int userAps = repository.getUserAps();
        tvTeaserApsScore.setText(String.valueOf(userAps));

        int qualified = 0;
        for (Programme p : allProgrammes) {
            if ("qualified".equals(p.getEligibilityStatus(userAps))) {
                qualified++;
            }
        }

        tvTeaserQualifiedCount.setText("You qualify for " + qualified + " out of " + allProgrammes.size() + " SPU programmes");
        if (programmeAdapter != null) {
            filterProgrammes(etSearchProgrammes.getText().toString());
        }
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(this::refreshDashboardStats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboardStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.removeListener(this);
    }
}