package za.ac.spu.sacns.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;
import java.util.Locale;

import za.ac.spu.sacns.R;
import za.ac.spu.sacns.models.NavigationNode;
import za.ac.spu.sacns.navigation.navigation.CampusNavigationData;
import za.ac.spu.sacns.navigation.navigation.DijkstraAlgorithm;
import za.ac.spu.sacns.navigation.navigation.NavigationGraph;
import za.ac.spu.sacns.Views.CampusMapView;

public class CampusNavigationActivity extends AppCompatActivity {

    private com.google.android.material.floatingactionbutton.FloatingActionButton btnZoomIn;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnZoomOut;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnResetMap;

    private CampusMapView campusMapView;

    private Spinner spRouteFrom;
    private Spinner spRouteTo;

    private Button btnFindRoute;

    private TextView tvWalkingTime;
    private TextView tvDistance;
    private TextView tvSelectedVenueName;
    private TextView tvSelectedVenueDesc;

    private NavigationGraph navigationGraph;

    private TextToSpeech textToSpeech;
    private boolean voiceEnabled = true;
    private boolean textToSpeechReady = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private boolean locationUpdatesRunning = false;

    // === SPU Central Campus locations (must stay in the same order as locationIds) ===
    private final String[] locationNames = {
            "Main Gate",
            "William Pescod (WP)",
            "Teaching Practice (C008)",
            "Foundation Phase (C009)",
            "Moroka Residence (C001)",
            "Student Affairs (C002)",
            "Academic Building (C003)",
            "Library (C004)",
            "Applied Sciences (C005)",
            "Data Science Labs (C006)",
            "Science Lab (C007)",
            "Humanities Labs (C010)",
            "Agriculture (C011)",
            "Sports Pavilion (C017)",
            "Spectator Seating (C018)",
            "Sports Entrance (C019)"
    };

    private final String[] locationIds = {
            "gate",
            "wp",   "c008", "c009",
            "c001", "c002", "c003", "c004", "c005", "c006", "c007",
            "c010", "c011",
            "c017", "c018", "c019"
    };

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                        Boolean fineLocation = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);

                        Boolean coarseLocation = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);

                        if (Boolean.TRUE.equals(fineLocation)
                                || Boolean.TRUE.equals(coarseLocation)) {

                            startLocationUpdates();

                            Toast.makeText(
                                    this,
                                    "Location permission granted",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Location permission is required for GPS navigation",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_campus_navigation);

        initializeViews();

        navigationGraph = CampusNavigationData.createCampusGraph();

        setupTextToSpeech();

        setupLocationServices();

        setupSpinners();

        setupButtons();

        requestLocationPermissionIfNeeded();
    }

    private void initializeViews() {

        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnResetMap = findViewById(R.id.btnResetMap);

        campusMapView = findViewById(R.id.campusMapView);

        spRouteFrom = findViewById(R.id.spRouteFrom);
        spRouteTo = findViewById(R.id.spRouteTo);

        btnFindRoute = findViewById(R.id.btnFindRoute);

        tvWalkingTime = findViewById(R.id.tvWalkingTime);
        tvDistance = findViewById(R.id.tvDistance);

        tvSelectedVenueName = findViewById(R.id.tvSelectedVenueName);
        tvSelectedVenueDesc = findViewById(R.id.tvSelectedVenueDesc);
    }

    private void setupTextToSpeech() {

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int result = textToSpeech.setLanguage(Locale.UK);

                        if (result != TextToSpeech.LANG_MISSING_DATA
                                && result != TextToSpeech.LANG_NOT_SUPPORTED) {

                            textToSpeechReady = true;
                        }
                    }
                }
        );
    }

    private void setupLocationServices() {

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    handleNewLocation(location);
                }
            }
        };
    }

    private void setupSpinners() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locationNames
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spRouteFrom.setAdapter(adapter);
        spRouteTo.setAdapter(adapter);

        // Default: From = Main Gate (index 0), To = William Pescod (index 1)
        if (locationNames.length > 1) {
            spRouteFrom.setSelection(0);
            spRouteTo.setSelection(1);
        }
    }

    private void setupButtons() {

        // ✅ Find Route button — this was missing before
        btnFindRoute.setOnClickListener(v -> calculateRoute());

        // Zoom In
        btnZoomIn.setOnClickListener(v -> {
            if (campusMapView != null) campusMapView.zoomIn();
        });

        // Zoom Out
        btnZoomOut.setOnClickListener(v -> {
            if (campusMapView != null) campusMapView.zoomOut();
        });

        // Reset map (pan + zoom)
        btnResetMap.setOnClickListener(v -> {
            if (campusMapView != null) campusMapView.resetMap();
        });
    }

    private void calculateRoute() {

        int fromPosition = spRouteFrom.getSelectedItemPosition();
        int toPosition = spRouteTo.getSelectedItemPosition();

        if (fromPosition < 0 || toPosition < 0) {

            Toast.makeText(
                    this,
                    "Please select a starting point and destination",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (fromPosition == toPosition) {

            Toast.makeText(
                    this,
                    "Starting point and destination cannot be the same",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String startId = locationIds[fromPosition];
        String destinationId = locationIds[toPosition];

        DijkstraAlgorithm.RouteResult routeResult =
                DijkstraAlgorithm.findShortestPath(
                        navigationGraph,
                        startId,
                        destinationId
                );

        if (routeResult == null
                || routeResult.getPath() == null
                || routeResult.getPath().isEmpty()) {

            Toast.makeText(
                    this,
                    "No route could be found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        List<NavigationNode> path = routeResult.getPath();
        double distance = routeResult.getDistance();
        double walkingMinutes = calculateWalkingTime(distance);

        // Draw the route + mark the destination with the correct ID
        if (campusMapView != null) {
            campusMapView.setRoute(path);
            campusMapView.setSelectedDestination(locationIds[toPosition]); // ✅ ID, not name
        }

        tvDistance.setText(String.format(Locale.getDefault(), "%.0f m", distance));
        tvWalkingTime.setText(String.format(Locale.getDefault(), "%.0f min", walkingMinutes));

        tvSelectedVenueName.setText(locationNames[toPosition]);

        tvSelectedVenueDesc.setText(
                "Route from "
                        + locationNames[fromPosition]
                        + " to "
                        + locationNames[toPosition]
        );

        announceRoute(path, locationNames[toPosition]);
    }

    private double calculateWalkingTime(double distanceMetres) {

        double walkingSpeedMetresPerMinute = 80.0;

        return distanceMetres / walkingSpeedMetresPerMinute;
    }

    private void announceRoute(
            List<NavigationNode> path,
            String destination
    ) {

        if (!voiceEnabled || !textToSpeechReady) {
            return;
        }

        if (path == null || path.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();

        message.append("Route to ")
                .append(destination)
                .append(". ");

        message.append("Follow this route: ");

        for (int i = 0; i < path.size(); i++) {

            message.append(path.get(i).getName());

            if (i < path.size() - 1) {
                message.append(", then ");
            }
        }

        textToSpeech.speak(
                message.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "routeAnnouncement"
        );
    }

    private void requestLocationPermissionIfNeeded() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {

            startLocationUpdates();

        } else {

            locationPermissionLauncher.launch(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }
            );
        }
    }

    private void startLocationUpdates() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            return;
        }

        LocationRequest locationRequest =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        3000
                )
                        .setMinUpdateIntervalMillis(2000)
                        .build();

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        locationUpdatesRunning = true;
    }

    private void stopLocationUpdates() {

        if (fusedLocationClient == null
                || locationCallback == null) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback);

        locationUpdatesRunning = false;
    }

    private void handleNewLocation(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();

        if (accuracy > 100) {
            return;
        }

        String locationText = String.format(
                Locale.getDefault(),
                "GPS: %.6f, %.6f",
                latitude,
                longitude
        );

        tvSelectedVenueDesc.setText(locationText);
    }

    public void setVoiceEnabled(boolean enabled) {

        voiceEnabled = enabled;

        if (!enabled && textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!locationUpdatesRunning) {
            requestLocationPermissionIfNeeded();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}