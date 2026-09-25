package za.ac.spu.sacns.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.data.DataRepository;
import za.ac.spu.sacns.models.Building;

public class CampusNavigationActivity extends AppCompatActivity {

    private DataRepository repository;
    private Spinner spRouteFrom;
    private Spinner spRouteTo;
    private Button btnFindRoute;
    private TextView tvWalkingTime;
    private TextView tvDistance;
    private TextView tvSelectedVenueName;
    private TextView tvSelectedVenueDesc;

    private List<Building> buildings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_navigation);

        repository = DataRepository.getInstance(this);
        buildings = repository.getBuildings();

        initializeViews();
        setupSpinners();
        setupActions();
        calculateRoute();
    }

    private void initializeViews() {
        spRouteFrom = findViewById(R.id.spRouteFrom);
        spRouteTo = findViewById(R.id.spRouteTo);
        btnFindRoute = findViewById(R.id.btnFindRoute);
        tvWalkingTime = findViewById(R.id.tvWalkingTime);
        tvDistance = findViewById(R.id.tvDistance);
        tvSelectedVenueName = findViewById(R.id.tvSelectedVenueName);
        tvSelectedVenueDesc = findViewById(R.id.tvSelectedVenueDesc);
    }

    private void setupSpinners() {
        List<String> fromList = new ArrayList<>();
        fromList.add("Main Campus Gate (Entrance)");
        for (Building b : buildings) {
            fromList.add(b.getName() + " (" + b.getType() + ")");
        }

        List<String> toList = new ArrayList<>();
        for (Building b : buildings) {
            toList.add(b.getName() + " (" + b.getType() + ")");
        }

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fromList);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, toList);

        spRouteFrom.setAdapter(fromAdapter);
        spRouteTo.setAdapter(toAdapter);

        // Pre-select ICT Building as default destination (index 1 if ICT Building is second)
        if (toList.size() > 1) {
            spRouteTo.setSelection(1);
        }

        spRouteTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < buildings.size()) {
                    Building b = buildings.get(position);
                    tvSelectedVenueName.setText(b.getName());
                    tvSelectedVenueDesc.setText(b.getDescription());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupActions() {
        btnFindRoute.setOnClickListener(v -> calculateRoute());
    }

    private void calculateRoute() {
        int toPos = spRouteTo.getSelectedItemPosition();
        if (toPos < 0 || toPos >= buildings.size()) return;

        Building toBuilding = buildings.get(toPos);
        tvSelectedVenueName.setText(toBuilding.getName());
        tvSelectedVenueDesc.setText(toBuilding.getDescription());

        int fromPos = spRouteFrom.getSelectedItemPosition();
        int fromX = 100, fromY = 250; // Gate coords
        if (fromPos > 0 && fromPos - 1 < buildings.size()) {
            Building fromBuilding = buildings.get(fromPos - 1);
            fromX = fromBuilding.getX();
            fromY = fromBuilding.getY();
        }

        int toX = toBuilding.getX();
        int toY = toBuilding.getY();

        int distMeters = (int) Math.round(Math.abs(toX - fromX) * 0.8 + Math.abs(toY - fromY) * 0.9);
        if (distMeters < 50) distMeters = 60;
        int walkingMins = Math.max(1, (int) Math.ceil(distMeters / 70.0));

        tvDistance.setText(distMeters + " meters");
        tvWalkingTime.setText(walkingMins + " mins");

        Toast.makeText(this, "Route calculated: " + distMeters + "m (" + walkingMins + " mins walk)", Toast.LENGTH_SHORT).show();
    }
}
