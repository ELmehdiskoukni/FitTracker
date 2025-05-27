package com.emsi.fittracker.models;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String name;
    private String email;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    // Getters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        return result;
    }
}