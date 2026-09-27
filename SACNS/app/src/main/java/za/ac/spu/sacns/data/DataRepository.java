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

        // If local data is empty or outdated from older mocks, populate with official SPU 2026 seed data
        if (programmes.size() < 22) {
            programmes.clear();
            loadDefaultProgrammes();
            saveProgrammesLocally();
        }

        if (buildings.size() < 8 || !buildings.get(0).getName().contains("Luka Jantjie")) {
            buildings.clear();
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
       Default Seed Data (Sol Plaatje University 2026 Prospectus)
       ========================================================== */
    private void loadDefaultProgrammes() {
        // FACULTY OF EDUCATION (Prospectus Pages 5 - 11)
        programmes.add(new Programme(101,
                "Bachelor of Education in Foundation Phase Teaching (Grade R-3) [EDU720]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 (50%) or English FAL Level 5 (60%). Afrikaans/Setswana/isiXhosa HL/FAL Level 4. Mathematics Level 3 (40%) or Mathematical Literacy Level 4 (50%). (Or APS 25 + SAQA accredited ECD Level 5).",
                "NQF Level 7", "119582", "Bachelor's Degree",
                "Foundation Phase Teacher (Grade R-3), Early Childhood Development Practitioner, Primary Education Specialist."));

        programmes.add(new Programme(102,
                "Bachelor of Education in Intermediate Phase Teaching (Maths, Sciences & Tech) [EDU723]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Afrikaans/Setswana/isiXhosa Level 4. Compulsory: Mathematics Level 4, Physical Sciences Level 4, Life Sciences Level 4.",
                "NQF Level 7", "99722", "Bachelor's Degree",
                "Intermediate Phase Teacher (Grades 4-6) in Languages, Mathematics, Natural Sciences and Technology."));

        programmes.add(new Programme(103,
                "Bachelor of Education in Intermediate Phase Teaching (Social Sciences & Life Skills) [EDU724]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Afrikaans/Setswana/isiXhosa Level 4. Any one of: Geography Level 4 OR History Level 4.",
                "NQF Level 7", "99722", "Bachelor's Degree",
                "Intermediate Phase Teacher (Grades 4-6) in Languages, Social Sciences and Life Skills, Educational Advisor."));

        programmes.add(new Programme(104,
                "Bachelor of Education in Senior & FET Phase (Life Sciences, Natural Sciences & Maths) [EDU740]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Compulsory: Mathematics Level 4 AND Life Sciences Level 4.",
                "NQF Level 7", "96406", "Bachelor's Degree",
                "High School (Grades 7-12) Teacher in Mathematics, Natural Sciences, Life Sciences, STEM Education Specialist."));

        programmes.add(new Programme(105,
                "Bachelor of Education in Senior & FET Phase (Languages OR Language & History) [EDU741]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Afrikaans HL/FAL Level 4 OR Setswana HL/FAL Level 4. History Level 4 if History is selected.",
                "NQF Level 7", "96406", "Bachelor's Degree",
                "High School (Grades 7-12) Teacher in Languages (English, Afrikaans, Setswana) and History."));

        programmes.add(new Programme(106,
                "Bachelor of Education in Senior & FET Phase (History, Social Sciences & Language) [EDU742]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Afrikaans/Setswana Level 4. Compulsory: Geography Level 4 AND History Level 4.",
                "NQF Level 7", "96406", "Bachelor's Degree",
                "High School (Grades 7-12) Teacher in Geography, History, Social Sciences and Languages."));

        programmes.add(new Programme(107,
                "Bachelor of Education in Senior & FET Phase (Accounting, Economics & Business Studies) [EDU743]",
                "Faculty of Education",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Any two of: Accounting Level 4, Business Studies Level 4, or Economics Level 4.",
                "NQF Level 7", "96406", "Bachelor's Degree",
                "High School (Grades 7-12) Commercial Educator in Accounting, Economics, Business Studies and EMS."));

        programmes.add(new Programme(108,
                "Postgraduate Certificate in Education (PGCE)",
                "Faculty of Education",
                30,
                "1 year",
                "Approved Bachelor's degree (NQF Level 7) or 360-credit Diploma (NQF Level 6) with two recognised school subjects.",
                "NQF Level 7", "119074", "Postgraduate Certificate",
                "Accredited Secondary School Educator, School Subject Specialist, Education Official."));

        // FACULTY OF ECONOMIC AND MANAGEMENT SCIENCES (Prospectus Pages 12 - 16)
        programmes.add(new Programme(201,
                "Bachelor of Commerce in Accounting",
                "Faculty of Economic and Management Sciences",
                30,
                "3 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 5 (60%) OR (Mathematics Level 4 AND Accounting Level 3).",
                "NQF Level 7", "118404", "Bachelor's Degree",
                "Financial Accountant, Management Accountant, Financial Manager, Tax Practitioner, Internal Auditor, Finance Director, CFO, Consultant."));

        programmes.add(new Programme(202,
                "Bachelor of Commerce in Economics",
                "Faculty of Economic and Management Sciences",
                30,
                "3 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 5 (60%) OR (Mathematics Level 4 AND Economics/Business Studies Level 3).",
                "NQF Level 7", "118906", "Bachelor's Degree",
                "Economist, Economic Researcher, Financial Risk Analyst, Market Forecaster, Investment Analyst, Economic Programmer."));

        programmes.add(new Programme(203,
                "Diploma in Retail Business Management",
                "Faculty of Economic and Management Sciences",
                25,
                "3 years",
                "NSC Diploma endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 3 OR Mathematical Literacy Level 5. At least one of: Accounting, Business Studies or Economics at Level 4.",
                "NQF Level 6", "93648", "Diploma",
                "Retail Trainee Manager, Merchandiser, Buyer, Stock Controller, Store Manager, Supply Chain Coordinator."));

        programmes.add(new Programme(204,
                "Advanced Diploma in Management",
                "Faculty of Economic and Management Sciences",
                25,
                "1 year",
                "Relevant Diploma at NQF Level 6 or Bachelor's degree with minimum 60% average in management-related subjects.",
                "NQF Level 7", "108875", "Advanced Diploma",
                "Strategic Manager, Project Manager, Operations Manager, Human Resources Manager, Managerial Finance Specialist."));

        programmes.add(new Programme(205,
                "Higher Certificate in Entrepreneurship",
                "Faculty of Economic and Management Sciences",
                25,
                "1 year",
                "NSC Higher Certificate endorsement. English HL/LOLT Level 4 or English FAL Level 5. Mathematics Level 3 OR Mathematical Literacy Level 4. At least one of Accounting, Business Studies or Economics at Level 3.",
                "NQF Level 5", "123433", "Higher Certificate",
                "Small Business Owner, Start-Up Entrepreneur, Business Consultant, Franchise Manager, Business Administrator, Junior Management."));

        // FACULTY OF HUMANITIES (Prospectus Pages 17 - 20)
        programmes.add(new Programme(301,
                "Bachelor of Arts (B.A.)",
                "Faculty of Humanities",
                30,
                "3 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 2 OR Mathematical Literacy Level 3. (Geography Level 4 required if majoring in Geography).",
                "NQF Level 7", "98922", "Bachelor's Degree",
                "Cultural Heritage Officer, Communications Specialist, Sociological Researcher, Public Relations, Policy Analyst, Journalist."));

        programmes.add(new Programme(302,
                "Higher Certificate in Heritage Studies",
                "Faculty of Humanities",
                25,
                "1 year",
                "NSC Higher Certificate endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 2 OR Mathematical Literacy Level 3.",
                "NQF Level 5", "94804", "Higher Certificate",
                "Museum Administrator, Archival Assistant, Heritage Site Officer, Tourism Officer, Cultural Resource Assistant."));

        programmes.add(new Programme(303,
                "Higher Certificate in Court Interpreting",
                "Faculty of Humanities",
                25,
                "1 year",
                "NSC Higher Certificate endorsement. English HL Level 4 or English FAL Level 5. At least one other African language at HL Level 4 OR FAL Level 5.",
                "NQF Level 5", "115460", "Higher Certificate",
                "Court & Legal Interpreter, Community Legal Liaison, Parliamentary/Government Interpreter, Health & Medical Interpreter."));

        // FACULTY OF NATURAL AND APPLIED SCIENCES (Prospectus Pages 21 - 28)
        programmes.add(new Programme(401,
                "Bachelor of Science (B.Sc.)",
                "Faculty of Natural and Applied Sciences",
                30,
                "3 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 4 (Mathematical Literacy not acceptable). Physical Sciences Level 4. Life Sciences Level 4.",
                "NQF Level 7", "97908", "Bachelor's Degree",
                "Biologist, Conservationist, Molecular Biologist, Statistician, Computer Programmer, Cyber Security Analyst, Physicist, Chemical Analyst, GIS Analyst."));

        programmes.add(new Programme(402,
                "Bachelor of Science in Data Science",
                "Faculty of Natural and Applied Sciences",
                30,
                "3 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 5 (60%) (Mathematical Literacy not acceptable).",
                "NQF Level 7", "96105", "Bachelor's Degree",
                "Data Scientist, Data Architect, Data Analyst, Analytics Manager, Data Engineer, Intelligence Analyst, Machine Learning Specialist."));

        programmes.add(new Programme(403,
                "Bachelor of Environmental Science",
                "Faculty of Natural and Applied Sciences",
                30,
                "4 years",
                "NSC Bachelor's endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 4 (Mathematical Literacy not acceptable). Physical Sciences Level 4. Life Sciences Level 4.",
                "NQF Level 8", "123429", "Bachelor's Degree",
                "Environmental Consultant, Mining Environmental Specialist, Sustainability Manager, Climate Resilience Officer, Urban Planner Collaborator."));

        programmes.add(new Programme(404,
                "Diploma in Information and Communication Technology (ICT) in Applications Development",
                "Faculty of Natural and Applied Sciences",
                25,
                "3 years",
                "NSC Diploma endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 3 OR Mathematical Literacy Level 5. (CAT or IT highly recommended).",
                "NQF Level 6", "93728", "Diploma",
                "Software Application Developer, Web Developer, Systems Administrator, Solution Architect, Network Analyst, Software Analyst."));

        programmes.add(new Programme(405,
                "Diploma in Agriculture",
                "Faculty of Natural and Applied Sciences",
                25,
                "3 years",
                "NSC Diploma endorsement. English HL Level 4 or English FAL Level 5. Mathematics Level 3 OR Mathematical Literacy Level 5. Physical Science Level 3. Life Sciences Level 3 OR Agricultural Sciences Level 3.",
                "NQF Level 6", "120923", "Diploma",
                "Agricultural Entrepreneur, Agricultural Extension Officer, Farm Manager, Agricultural Advisor, Research Technician."));

        programmes.add(new Programme(406,
                "Advanced Diploma in ICT in Applications Development",
                "Faculty of Natural and Applied Sciences",
                25,
                "1 year",
                "3-year Diploma in ICT (NQF Level 6) or equivalent with at least 60% in third-year exit modules.",
                "NQF Level 7", "111254", "Advanced Diploma",
                "Senior Software Engineer, Solutions Architect, IT Systems Analyst, Applications Development Specialist."));
    }

    private void loadDefaultBuildings() {
        // Sol Plaatje University Buildings explicitly required:
        buildings.add(new Building(1, "Sol Plaatje University - North Campus: Luka Jantjie House", "admin",
                "North Campus administrative headquarters, Vice-Chancellor's office, Council chambers, and Executive Management", 265, 125));

        buildings.add(new Building(2, "Sol Plaatje University Central Campus", "academic",
                "Central Campus core academic precinct, modern lecture halls, and student assembly square", 510, 125));

        buildings.add(new Building(3, "Sol Plaatje University Library & Student Resource Centre", "facility",
                "Iconic Sol Plaatje Memorial Library, multi-level learning commons, research hub, and digital study pods", 510, 375));

        buildings.add(new Building(4, "Moroka building", "facility",
                "Historic Moroka residence hall, academic student support services, and communal learning zones", 265, 375));

        buildings.add(new Building(5, "ems building", "academic",
                "Faculty of Economic and Management Sciences, specialized accounting labs, lecture auditoriums, and seminar rooms", 755, 125));

        buildings.add(new Building(6, "nas building", "academic",
                "Faculty of Natural and Applied Sciences, advanced chemistry, physics, biology labs, and computing facilities", 755, 375));

        buildings.add(new Building(7, "education building", "academic",
                "Faculty of Education, teacher training lecture theatres, micro-teaching simulation suites, and pedagogical labs", 380, 250));

        buildings.add(new Building(8, "humanities building", "academic",
                "Faculty of Humanities, languages and communication studios, heritage studies seminar rooms, and staff offices", 630, 250));
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
