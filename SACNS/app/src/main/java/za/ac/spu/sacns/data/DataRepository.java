package za.ac.spu.sacns.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.ac.spu.sacns.models.Building;
import za.ac.spu.sacns.models.Programme;

public class DataRepository {
    private static final String TAG = "DataRepository";
    private static final String PREF_NAME = "sacns_data_prefs";
    private static final String KEY_PROGRAMMES = "sacns_programmes_json";
    private static final String KEY_BUILDINGS = "sacns_buildings_json";
    private static final String KEY_USER_APS = "sacns_user_aps";
    private static final String KEY_USER_ROLE = "sacns_user_role";

    private static DataRepository instance;
    private final Context appContext;
    private final SharedPreferences prefs;
    private FirebaseFirestore firestore;

    private final List<Programme> programmes = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final List<DataChangeListener> listeners = new ArrayList<>();

    public interface DataChangeListener {
        void onDataChanged();
    }

    private DataRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        try {
            this.firestore = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.w(TAG, "Firestore initialization skipped: " + e.getMessage());
        }

        loadLocalData();

        // If local data is empty, populate with official SPU seed data
        if (programmes.isEmpty()) {
            loadDefaultProgrammes();
            saveProgrammesLocally();
        }

        if (buildings.isEmpty()) {
            loadDefaultBuildings();
            saveBuildingsLocally();
        }

        syncWithFirestore();
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context);
        }
        return instance;
    }

    public void addListener(DataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Listener notification failed: " + e.getMessage());
            }
        }
    }

    /* ==========================================================
       Programmes CRUD
       ========================================================== */
    public synchronized List<Programme> getProgrammes() {
        return new ArrayList<>(programmes);
    }

    public synchronized Programme getProgrammeById(long id) {
        for (Programme p : programmes) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public synchronized void addProgramme(Programme programme) {
        if (programme.getId() == 0) {
            programme.setId(System.currentTimeMillis());
        }
        programmes.add(programme);
        saveProgrammesLocally();
        syncProgrammeToFirestore(programme);
        notifyListeners();
    }

    public synchronized void updateProgramme(Programme programme) {
        for (int i = 0; i < programmes.size(); i++) {
            if (programmes.get(i).getId() == programme.getId()) {
                programmes.set(i, programme);
                saveProgrammesLocally();
                syncProgrammeToFirestore(programme);
                notifyListeners();
                return;
            }
        }
    }

    public synchronized void deleteProgramme(long id) {
        Programme target = null;
        for (Programme p : programmes) {
            if (p.getId() == id) {
                target = p;
                break;
            }
        }

        if (target != null) {
            programmes.remove(target);
            saveProgrammesLocally();
            deleteProgrammeFromFirestore(id);
            notifyListeners();
        }
    }

    /* ==========================================================
       Buildings CRUD
       ========================================================== */
    public synchronized List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }

    public synchronized Building getBuildingById(long id) {
        for (Building b : buildings) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public synchronized void addBuilding(Building building) {
        if (building.getId() == 0) {
            building.setId(System.currentTimeMillis());
        }
        buildings.add(building);
        saveBuildingsLocally();
        syncBuildingToFirestore(building);
        notifyListeners();
    }

    public synchronized void updateBuilding(Building building) {
        for (int i = 0; i < buildings.size(); i++) {
            if (buildings.get(i).getId() == building.getId()) {
                buildings.set(i, building);
                saveBuildingsLocally();
                syncBuildingToFirestore(building);
                notifyListeners();
                return;
            }
        }
    }

    public synchronized void deleteBuilding(long id) {
        Building target = null;
        for (Building b : buildings) {
            if (b.getId() == id) {
                target = b;
                break;
            }
        }

        if (target != null) {
            buildings.remove(target);
            saveBuildingsLocally();
            deleteBuildingFromFirestore(id);
            notifyListeners();
        }
    }

    /* ==========================================================
       User APS & Role Persistence
       ========================================================== */
    public int getUserAps() {
        return prefs.getInt(KEY_USER_APS, 35);
    }

    public void setUserAps(int aps) {
        prefs.edit().putInt(KEY_USER_APS, aps).apply();
        notifyListeners();
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "Prospective");
    }

    public void setUserRole(String role) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public synchronized void resetToDefaultSeedData() {
        programmes.clear();
        loadDefaultProgrammes();
        saveProgrammesLocally();

        buildings.clear();
        loadDefaultBuildings();
        saveBuildingsLocally();

        notifyListeners();
    }

    /* ==========================================================
       Default Seed Data (Sol Plaatje University)
       ========================================================== */
    private void loadDefaultProgrammes() {
        programmes.add(new Programme(1, "Diploma in ICT", "Faculty of Humanities", 28, "3 years",
                "NSC Diploma endorsement. English Level 4 (50%), Mathematics Level 4 (50%) or Mathematical Literacy Level 6 (70%)."));

        programmes.add(new Programme(2, "Bachelor of Commerce", "Faculty of Management Sciences", 30, "3 years",
                "NSC Degree endorsement. English Level 4 (50%), Mathematics Level 4 (50%) or Accounting Level 5 (60%)."));

        programmes.add(new Programme(3, "Bachelor of Education", "Faculty of Humanities", 26, "4 years",
                "NSC Degree endorsement. English Level 4 (50%), with relevant school teaching specialisation subjects."));

        programmes.add(new Programme(4, "BSc in Data Science", "Faculty of Natural & Applied Sciences", 34, "3 years",
                "NSC Degree endorsement. English Level 5 (60%), Mathematics Level 5 (60%), Physical Sciences Level 4 (50%)."));

        programmes.add(new Programme(5, "Bachelor of Arts", "Faculty of Humanities", 25, "3 years",
                "NSC Degree endorsement. English Level 4 (50%), plus two elective humanities disciplines at Level 4."));

        programmes.add(new Programme(6, "Diploma in Retail Business", "Faculty of Management Sciences", 36, "3 years",
                "NSC Diploma endorsement. English Level 4 (50%), Business Studies or Accounting Level 5 (60%)."));

        programmes.add(new Programme(7, "BSc in Mathematical Sciences", "Faculty of Natural & Applied Sciences", 37, "3 years",
                "NSC Degree endorsement. English Level 4 (50%), Mathematics Level 6 (70%), Physical Sciences Level 5 (60%)."));

        programmes.add(new Programme(8, "Bachelor of Science in Physics", "Faculty of Natural & Applied Sciences", 40, "3 years",
                "NSC Degree endorsement. English Level 5 (60%), Mathematics Level 6 (70%), Physical Sciences Level 6 (70%)."));
    }

    private void loadDefaultBuildings() {
        buildings.add(new Building(1, "Administration Building", "admin",
                "Main admin offices, Registrar, Admissions, and Student Finance", 265, 125));

        buildings.add(new Building(2, "ICT Building", "academic",
                "Computer labs 1-6, IT Support Centre, and multimedia lecture auditoriums", 510, 125));

        buildings.add(new Building(3, "Library", "facility",
                "Main Sol Plaatje Memorial Library, study cubicles, and digital resource hub", 510, 375));

        buildings.add(new Building(4, "Student Centre", "facility",
                "Cafeteria, Student Representative Council (SRC) & Student Support Services", 265, 375));

        buildings.add(new Building(5, "Science Labs Complex", "academic",
                "Chemistry, Biology, and Physics research & teaching laboratories", 755, 125));

        buildings.add(new Building(6, "Sports Complex", "sports",
                "Indoor sports arena, gym, athletics office, and fitness pavilion", 755, 375));
    }

    /* ==========================================================
       Local Persistence (JSON in SharedPreferences)
       ========================================================== */
    private void loadLocalData() {
        try {
            String progJson = prefs.getString(KEY_PROGRAMMES, null);
            if (progJson != null) {
                JSONArray arr = new JSONArray(progJson);
                programmes.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Programme p = Programme.fromJsonObject(obj);
                    if (p != null) programmes.add(p);
                }
            }

            String bldgJson = prefs.getString(KEY_BUILDINGS, null);
            if (bldgJson != null) {
                JSONArray arr = new JSONArray(bldgJson);
                buildings.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Building b = Building.fromJsonObject(obj);
                    if (b != null) buildings.add(b);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading local data: " + e.getMessage());
        }
    }

    private void saveProgrammesLocally() {
        try {
            JSONArray arr = new JSONArray();
            for (Programme p : programmes) {
                arr.put(p.toJsonObject());
            }
            prefs.edit().putString(KEY_PROGRAMMES, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving programmes locally: " + e.getMessage());
        }
    }

    private void saveBuildingsLocally() {
        try {
            JSONArray arr = new JSONArray();
            for (Building b : buildings) {
                arr.put(b.toJsonObject());
            }
            prefs.edit().putString(KEY_BUILDINGS, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving buildings locally: " + e.getMessage());
        }
    }

    /* ==========================================================
       Firestore Cloud Sync
       ========================================================== */
    private void syncWithFirestore() {
        if (firestore == null) return;

        firestore.collection("programmes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                List<Programme> remote = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    try {
                        long id = doc.contains("id") ? doc.getLong("id") : Long.parseLong(doc.getId());
                        String name = doc.getString("name");
                        String department = doc.getString("department");
                        int minAps = doc.contains("minAps") ? doc.getLong("minAps").intValue() : 25;
                        String duration = doc.getString("duration");
                        String requirements = doc.getString("requirements");

                        if (name != null) {
                            remote.add(new Programme(id, name, department, minAps, duration, requirements));
                        }
                    } catch (Exception ignored) {}
                }

                if (!remote.isEmpty()) {
                    synchronized (this) {
                        programmes.clear();
                        programmes.addAll(remote);
                        saveProgrammesLocally();
                    }
                    notifyListeners();
                }
            }
        }).addOnFailureListener(e -> Log.d(TAG, "Firestore sync programmes notice: " + e.getMessage()));

        firestore.collection("buildings").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                List<Building> remote = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    try {
                        long id = doc.contains("id") ? doc.getLong("id") : Long.parseLong(doc.getId());
                        String name = doc.getString("name");
                        String type = doc.getString("type");
                        String description = doc.getString("description");
                        int x = doc.contains("x") ? doc.getLong("x").intValue() : 250;
                        int y = doc.contains("y") ? doc.getLong("y").intValue() : 250;

                        if (name != null) {
                            remote.add(new Building(id, name, type, description, x, y));
                        }
                    } catch (Exception ignored) {}
                }

                if (!remote.isEmpty()) {
                    synchronized (this) {
                        buildings.clear();
                        buildings.addAll(remote);
                        saveBuildingsLocally();
                    }
                    notifyListeners();
                }
            }
        }).addOnFailureListener(e -> Log.d(TAG, "Firestore sync buildings notice: " + e.getMessage()));
    }

    private void syncProgrammeToFirestore(Programme p) {
        if (firestore == null) return;
        firestore.collection("programmes")
                .document(String.valueOf(p.getId()))
                .set(p.toMap(), SetOptions.merge())
                .addOnFailureListener(e -> Log.d(TAG, "Cloud sync notice: " + e.getMessage()));
    }

    private void deleteProgrammeFromFirestore(long id) {
        if (firestore == null) return;
        firestore.collection("programmes")
                .document(String.valueOf(id))
                .delete()
                .addOnFailureListener(e -> Log.d(TAG, "Cloud delete notice: " + e.getMessage()));
    }

    private void syncBuildingToFirestore(Building b) {
        if (firestore == null) return;
        firestore.collection("buildings")
                .document(String.valueOf(b.getId()))
                .set(b.toMap(), SetOptions.merge())
                .addOnFailureListener(e -> Log.d(TAG, "Cloud sync notice: " + e.getMessage()));
    }

    private void deleteBuildingFromFirestore(long id) {
        if (firestore == null) return;
        firestore.collection("buildings")
                .document(String.valueOf(id))
                .delete()
                .addOnFailureListener(e -> Log.d(TAG, "Cloud delete notice: " + e.getMessage()));
    }
}
