package za.ac.spu.sacns.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.adapters.AdminBuildingAdapter;
import za.ac.spu.sacns.adapters.AdminProgrammeAdapter;
import za.ac.spu.sacns.data.DataRepository;
import za.ac.spu.sacns.models.Building;
import za.ac.spu.sacns.models.Programme;

public class AdminPanelActivity extends AppCompatActivity implements DataRepository.DataChangeListener {

    private static final int TAB_PROGRAMMES = 0;
    private static final int TAB_CAMPUS = 1;

    private DataRepository repository;
    private int currentTab = TAB_PROGRAMMES;

    private Programme editingProgramme = null;
    private Building editingBuilding = null;

    // Header & Stats
    private ImageButton btnAdminBack;
    private Button btnAdminResetData;
    private TextView tvAdminStatTotalProgs;
    private TextView tvAdminStatAvgAps;
    private TextView tvAdminStatTotalBuildings;

    // Tabs & Actions
    private TabLayout tabLayoutAdmin;
    private Button btnAdminAddAction;
    private EditText etAdminSearch;

    // Form
    private CardView cardAdminForm;
    private TextView tvAdminFormTitle;
    private TextView labelField1;
    private EditText etAdminField1;
    private TextView labelField2;
    private EditText etAdminField2;
    private TextView labelField3;
    private EditText etAdminField3;
    private TextView labelField4;
    private EditText etAdminField4;
    private LinearLayout layoutField3Wrapper;
    private LinearLayout layoutField4Wrapper;
    private Button btnAdminCancel;
    private Button btnAdminSave;

    // Table Headers
    private TextView tvAdminCol1;
    private TextView tvAdminCol2;
    private TextView tvAdminCol3;
    private TextView tvAdminCol4;
    private TextView tvAdminCol5;

    // Recycler & Adapters
    private RecyclerView rvAdminTable;
    private AdminProgrammeAdapter programmeAdapter;
    private AdminBuildingAdapter buildingAdapter;

    private List<Programme> currentProgrammes = new ArrayList<>();
    private List<Building> currentBuildings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        repository = DataRepository.getInstance(this);
        repository.addListener(this);

        initializeViews();
        setupTabLayout();
        setupFormHandlers();
        setupSearchFilter();
        setupAdapters();
        refreshAllData();
    }

    private void initializeViews() {
        btnAdminBack = findViewById(R.id.btnAdminBack);
        btnAdminResetData = findViewById(R.id.btnAdminResetData);
        tvAdminStatTotalProgs = findViewById(R.id.tvAdminStatTotalProgs);
        tvAdminStatAvgAps = findViewById(R.id.tvAdminStatAvgAps);
        tvAdminStatTotalBuildings = findViewById(R.id.tvAdminStatTotalBuildings);

        tabLayoutAdmin = findViewById(R.id.tabLayoutAdmin);
        btnAdminAddAction = findViewById(R.id.btnAdminAddAction);
        etAdminSearch = findViewById(R.id.etAdminSearch);

        cardAdminForm = findViewById(R.id.cardAdminForm);
        tvAdminFormTitle = findViewById(R.id.tvAdminFormTitle);
        labelField1 = findViewById(R.id.labelField1);
        etAdminField1 = findViewById(R.id.etAdminField1);
        labelField2 = findViewById(R.id.labelField2);
        etAdminField2 = findViewById(R.id.etAdminField2);
        labelField3 = findViewById(R.id.labelField3);
        etAdminField3 = findViewById(R.id.etAdminField3);
        labelField4 = findViewById(R.id.labelField4);
        etAdminField4 = findViewById(R.id.etAdminField4);
        layoutField3Wrapper = findViewById(R.id.layoutField3Wrapper);
        layoutField4Wrapper = findViewById(R.id.layoutField4Wrapper);
        btnAdminCancel = findViewById(R.id.btnAdminCancel);
        btnAdminSave = findViewById(R.id.btnAdminSave);

        tvAdminCol1 = findViewById(R.id.tvAdminCol1);
        tvAdminCol2 = findViewById(R.id.tvAdminCol2);
        tvAdminCol3 = findViewById(R.id.tvAdminCol3);
        tvAdminCol4 = findViewById(R.id.tvAdminCol4);
        tvAdminCol5 = findViewById(R.id.tvAdminCol5);

        rvAdminTable = findViewById(R.id.rvAdminTable);
        rvAdminTable.setLayoutManager(new LinearLayoutManager(this));

        btnAdminBack.setOnClickListener(v -> finish());

        btnAdminResetData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset SPU Data")
                    .setMessage("Are you sure you want to restore official Sol Plaatje University seed data? Any custom items will be replaced with defaults.")
                    .setPositiveButton("Restore Defaults", (dialog, which) -> {
                        repository.resetToDefaultSeedData();
                        Toast.makeText(this, "Default SPU data restored", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupTabLayout() {
        tabLayoutAdmin.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                closeForm();
                etAdminSearch.setText("");
                updateViewForTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateViewForTab() {
        if (currentTab == TAB_PROGRAMMES) {
            btnAdminAddAction.setText("+ Add New Programme");
            tvAdminCol1.setText("Programme Name");
            tvAdminCol2.setText("Department");
            tvAdminCol3.setVisibility(View.VISIBLE);
            tvAdminCol3.setText("Min APS");
            tvAdminCol4.setVisibility(View.VISIBLE);
            tvAdminCol4.setText("Duration");
            tvAdminCol5.setText("Actions");
            rvAdminTable.setAdapter(programmeAdapter);
        } else {
            btnAdminAddAction.setText("+ Add New Building");
            tvAdminCol1.setText("Building Name");
            tvAdminCol2.setText("Type");
            tvAdminCol3.setVisibility(View.GONE);
            tvAdminCol4.setVisibility(View.VISIBLE);
            tvAdminCol4.setText("Description");
            tvAdminCol5.setText("Actions");
            rvAdminTable.setAdapter(buildingAdapter);
        }
        applySearchFilter(etAdminSearch.getText().toString());
    }

    private void setupAdapters() {
        programmeAdapter = new AdminProgrammeAdapter(this, currentProgrammes, new AdminProgrammeAdapter.OnProgrammeActionListener() {
            @Override
            public void onEdit(Programme programme) {
                openEditProgramme(programme);
            }

            @Override
            public void onDelete(Programme programme) {
                confirmDeleteProgramme(programme);
            }
        });

        buildingAdapter = new AdminBuildingAdapter(this, currentBuildings, new AdminBuildingAdapter.OnBuildingActionListener() {
            @Override
            public void onEdit(Building building) {
                openEditBuilding(building);
            }

            @Override
            public void onDelete(Building building) {
                confirmDeleteBuilding(building);
            }
        });

        rvAdminTable.setAdapter(programmeAdapter);
    }

    private void setupFormHandlers() {
        btnAdminAddAction.setOnClickListener(v -> {
            if (currentTab == TAB_PROGRAMMES) {
                openAddProgramme();
            } else {
                openAddBuilding();
            }
        });

        btnAdminCancel.setOnClickListener(v -> closeForm());
        btnAdminSave.setOnClickListener(v -> handleSave());
    }

    private void setupSearchFilter() {
        etAdminSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applySearchFilter(String query) {
        String lower = query != null ? query.toLowerCase().trim() : "";

        if (currentTab == TAB_PROGRAMMES) {
            List<Programme> all = repository.getProgrammes();
            if (lower.isEmpty()) {
                currentProgrammes = all;
            } else {
                currentProgrammes = new ArrayList<>();
                for (Programme p : all) {
                    if (p.getName().toLowerCase().contains(lower) ||
                        p.getDepartment().toLowerCase().contains(lower)) {
                        currentProgrammes.add(p);
                    }
                }
            }
            programmeAdapter.updateData(currentProgrammes);
        } else {
            List<Building> all = repository.getBuildings();
            if (lower.isEmpty()) {
                currentBuildings = all;
            } else {
                currentBuildings = new ArrayList<>();
                for (Building b : all) {
                    if (b.getName().toLowerCase().contains(lower) ||
                        b.getType().toLowerCase().contains(lower) ||
                        b.getDescription().toLowerCase().contains(lower)) {
                        currentBuildings.add(b);
                    }
                }
            }
            buildingAdapter.updateData(currentBuildings);
        }
    }

    /* ==========================================================
       Add / Edit Form Operations
       ========================================================== */
    private void openAddProgramme() {
        editingProgramme = null;
        editingBuilding = null;

        tvAdminFormTitle.setText("Add New Programme");
        labelField1.setText("Programme Name");
        etAdminField1.setHint("e.g., Diploma in ICT");
        etAdminField1.setText("");

        labelField2.setText("Department / Faculty");
        etAdminField2.setHint("e.g., Faculty of Humanities");
        etAdminField2.setText("");

        layoutField3Wrapper.setVisibility(View.VISIBLE);
        labelField3.setText("Minimum APS");
        etAdminField3.setHint("e.g., 28");
        etAdminField3.setText("");

        layoutField4Wrapper.setVisibility(View.VISIBLE);
        labelField4.setText("Duration");
        etAdminField4.setHint("e.g., 3 years");
        etAdminField4.setText("3 years");

        cardAdminForm.setVisibility(View.VISIBLE);
        etAdminField1.requestFocus();
    }

    private void openEditProgramme(Programme programme) {
        editingProgramme = programme;
        editingBuilding = null;

        tvAdminFormTitle.setText("Edit Programme: " + programme.getName());
        labelField1.setText("Programme Name");
        etAdminField1.setText(programme.getName());

        labelField2.setText("Department / Faculty");
        etAdminField2.setText(programme.getDepartment());

        layoutField3Wrapper.setVisibility(View.VISIBLE);
        labelField3.setText("Minimum APS");
        etAdminField3.setText(String.valueOf(programme.getMinAps()));

        layoutField4Wrapper.setVisibility(View.VISIBLE);
        labelField4.setText("Duration");
        etAdminField4.setText(programme.getDuration());

        cardAdminForm.setVisibility(View.VISIBLE);
        etAdminField1.requestFocus();
    }

    private void openAddBuilding() {
        editingProgramme = null;
        editingBuilding = null;

        tvAdminFormTitle.setText("Add New Campus Building");
        labelField1.setText("Building Name");
        etAdminField1.setHint("e.g., Library Complex");
        etAdminField1.setText("");

        labelField2.setText("Building Type (academic, admin, facility, sports)");
        etAdminField2.setHint("e.g., academic");
        etAdminField2.setText("academic");

        layoutField3Wrapper.setVisibility(View.GONE);

        layoutField4Wrapper.setVisibility(View.VISIBLE);
        labelField4.setText("Description");
        etAdminField4.setHint("e.g., Main campus auditorium and study labs");
        etAdminField4.setText("");

        cardAdminForm.setVisibility(View.VISIBLE);
        etAdminField1.requestFocus();
    }

    private void openEditBuilding(Building building) {
        editingProgramme = null;
        editingBuilding = building;

        tvAdminFormTitle.setText("Edit Building: " + building.getName());
        labelField1.setText("Building Name");
        etAdminField1.setText(building.getName());

        labelField2.setText("Building Type (academic, admin, facility, sports)");
        etAdminField2.setText(building.getType());

        layoutField3Wrapper.setVisibility(View.GONE);

        layoutField4Wrapper.setVisibility(View.VISIBLE);
        labelField4.setText("Description");
        etAdminField4.setText(building.getDescription());

        cardAdminForm.setVisibility(View.VISIBLE);
        etAdminField1.requestFocus();
    }

    private void closeForm() {
        cardAdminForm.setVisibility(View.GONE);
        editingProgramme = null;
        editingBuilding = null;
        etAdminField1.setText("");
        etAdminField2.setText("");
        etAdminField3.setText("");
        etAdminField4.setText("");
    }

    private void handleSave() {
        String field1 = etAdminField1.getText().toString().trim();
        String field2 = etAdminField2.getText().toString().trim();

        if (TextUtils.isEmpty(field1)) {
            etAdminField1.setError("Please enter a name");
            etAdminField1.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(field2)) {
            etAdminField2.setError("This field cannot be empty");
            etAdminField2.requestFocus();
            return;
        }

        if (currentTab == TAB_PROGRAMMES) {
            String apsStr = etAdminField3.getText().toString().trim();
            String duration = etAdminField4.getText().toString().trim();

            if (TextUtils.isEmpty(apsStr)) {
                etAdminField3.setError("Please specify min APS");
                etAdminField3.requestFocus();
                return;
            }

            int minAps;
            try {
                minAps = Integer.parseInt(apsStr);
            } catch (NumberFormatException e) {
                etAdminField3.setError("Please enter a valid number");
                return;
            }

            if (TextUtils.isEmpty(duration)) {
                duration = "3 years";
            }

            if (editingProgramme != null) {
                editingProgramme.setName(field1);
                editingProgramme.setDepartment(field2);
                editingProgramme.setMinAps(minAps);
                editingProgramme.setDuration(duration);
                repository.updateProgramme(editingProgramme);
                Toast.makeText(this, "Updated: " + field1, Toast.LENGTH_SHORT).show();
            } else {
                Programme newProg = new Programme(
                        System.currentTimeMillis(),
                        field1,
                        field2,
                        minAps,
                        duration,
                        "NSC Admission endorsement. Minimum APS score: " + minAps + " points."
                );
                repository.addProgramme(newProg);
                Toast.makeText(this, "Created: " + field1, Toast.LENGTH_SHORT).show();
            }
        } else {
            String desc = etAdminField4.getText().toString().trim();
            if (TextUtils.isEmpty(desc)) {
                desc = "Campus venue and lecture facilities";
            }

            if (editingBuilding != null) {
                editingBuilding.setName(field1);
                editingBuilding.setType(field2);
                editingBuilding.setDescription(desc);
                repository.updateBuilding(editingBuilding);
                Toast.makeText(this, "Updated building: " + field1, Toast.LENGTH_SHORT).show();
            } else {
                int randX = 200 + (int)(Math.random() * 500);
                int randY = 100 + (int)(Math.random() * 250);
                Building newBuilding = new Building(
                        System.currentTimeMillis(),
                        field1,
                        field2,
                        desc,
                        randX,
                        randY
                );
                repository.addBuilding(newBuilding);
                Toast.makeText(this, "Created building: " + field1, Toast.LENGTH_SHORT).show();
            }
        }

        closeForm();
        refreshAllData();
    }

    private void confirmDeleteProgramme(Programme programme) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Programme")
                .setMessage("Are you sure you want to permanently delete \"" + programme.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteProgramme(programme.getId());
                    Toast.makeText(this, "Deleted programme: " + programme.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteBuilding(Building building) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Building")
                .setMessage("Are you sure you want to delete \"" + building.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteBuilding(building.getId());
                    Toast.makeText(this, "Deleted building: " + building.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshAllData() {
        List<Programme> progs = repository.getProgrammes();
        List<Building> bldgs = repository.getBuildings();

        // Update Stats
        tvAdminStatTotalProgs.setText(String.valueOf(progs.size()));
        tvAdminStatTotalBuildings.setText(String.valueOf(bldgs.size()));

        if (!progs.isEmpty()) {
            int sumAps = 0;
            for (Programme p : progs) sumAps += p.getMinAps();
            int avgAps = Math.round((float) sumAps / progs.size());
            tvAdminStatAvgAps.setText(String.valueOf(avgAps));
        } else {
            tvAdminStatAvgAps.setText("0");
        }

        applySearchFilter(etAdminSearch.getText().toString());
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(this::refreshAllData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.removeListener(this);
    }
}