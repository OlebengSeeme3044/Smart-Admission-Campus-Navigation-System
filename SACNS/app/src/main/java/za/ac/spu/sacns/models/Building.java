package za.ac.spu.sacns.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Building implements Serializable {
    private long id;
    private String name;
    private String type; // admin, academic, facility, sports
    private String description;
    private int x;
    private int y;

    public Building() {
        // Default constructor
    }

    public Building(long id, String name, String type, String description, int x, int y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.x = x;
        this.y = y;
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

    public String getType() {
        return type != null ? type : "facility";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("description", description);
        map.put("x", x);
        map.put("y", y);
        return map;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", type);
        json.put("description", description);
        json.put("x", x);
        json.put("y", y);
        return json;
    }

    public static Building fromJsonObject(JSONObject json) {
        if (json == null) return null;
        return new Building(
                json.optLong("id", System.currentTimeMillis()),
                json.optString("name", ""),
                json.optString("type", "facility"),
                json.optString("description", ""),
                json.optInt("x", 250),
                json.optInt("y", 250)
        );
    }
}
