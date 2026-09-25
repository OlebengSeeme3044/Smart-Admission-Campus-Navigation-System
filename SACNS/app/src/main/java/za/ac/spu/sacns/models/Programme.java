package za.ac.spu.sacns.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Programme implements Serializable {
    private long id;
    private String name;
    private String department;
    private int minAps;
    private String duration;
    private String requirements;

    public Programme() {
        // Default constructor required for Firebase & serialization
    }

    public Programme(long id, String name, String department, int minAps, String duration, String requirements) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.minAps = minAps;
        this.duration = duration;
        this.requirements = requirements;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department != null ? department : "";
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getMinAps() {
        return minAps;
    }

    public void setMinAps(int minAps) {
        this.minAps = minAps;
    }

    public String getDuration() {
        return duration != null ? duration : "";
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRequirements() {
        return requirements != null ? requirements : "";
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /**
     * Helper to classify qualification status for a given APS score:
     * - "qualified" if userAps >= minAps
     * - "borderline" if minAps - userAps <= 2
     * - "ineligible" if minAps - userAps > 2
     */
    public String getEligibilityStatus(int userAps) {
        if (userAps >= minAps) {
            return "qualified";
        } else if (minAps - userAps <= 2) {
            return "borderline";
        } else {
            return "ineligible";
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("department", department);
        map.put("minAps", minAps);
        map.put("duration", duration);
        map.put("requirements", requirements);
        return map;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("department", department);
        json.put("minAps", minAps);
        json.put("duration", duration);
        json.put("requirements", requirements);
        return json;
    }

    public static Programme fromJsonObject(JSONObject json) {
        if (json == null) return null;
        return new Programme(
                json.optLong("id", System.currentTimeMillis()),
                json.optString("name", ""),
                json.optString("department", ""),
                json.optInt("minAps", 25),
                json.optString("duration", "3 years"),
                json.optString("requirements", "")
        );
    }
}
