package com.emsi.fittracker.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private double height; // in cm
    private double weight; // in kg
    private int age;
    private String gender; // "male", "female", "other"
    private String activityLevel; // "sedentary", "light", "moderate", "active", "very_active"
    private long createdAt;
    private long updatedAt;

    // Default constructor (required for Firebase)
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor with basic info
    public User(String uid, String name, String email) {
        this();
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    // Constructor with all fields
    public User(String uid, String name, String email, double height, double weight,
                int age, String gender, String activityLevel) {
        this(uid, name, email);
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        updateTimestamp();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        updateTimestamp();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
        updateTimestamp();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        updateTimestamp();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        updateTimestamp();
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
        updateTimestamp();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isComplete() {
        return uid != null && !uid.isEmpty() &&
                name != null && !name.isEmpty() &&
                email != null && !email.isEmpty();
    }

    public boolean hasHealthData() {
        return height > 0 && weight > 0 && age > 0 &&
                gender != null && !gender.isEmpty() &&
                activityLevel != null && !activityLevel.isEmpty();
    }

    // BMI calculation
    public double calculateBMI() {
        if (height > 0 && weight > 0) {
            double heightInMeters = height / 100.0;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0.0;
    }

    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi == 0.0) return "Non calculé";
        if (bmi < 18.5) return "Sous-poids";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Surpoids";
        return "Obésité";
    }

    // Display name helper
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            return email.split("@")[0]; // Use part before @ as display name
        }
        return "Utilisateur";
    }

    // Age group helper
    public String getAgeGroup() {
        if (age <= 0) return "Non défini";
        if (age < 18) return "Moins de 18 ans";
        if (age < 25) return "18-24 ans";
        if (age < 35) return "25-34 ans";
        if (age < 45) return "35-44 ans";
        if (age < 55) return "45-54 ans";
        if (age < 65) return "55-64 ans";
        return "65+ ans";
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", activityLevel='" + activityLevel + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return uid != null ? uid.equals(user.uid) : user.uid == null;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }

    /**
     * Convert User object to Map for Firebase storage
     * @return Map representation of User object
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("height", height);
        result.put("weight", weight);
        result.put("age", age);
        result.put("gender", gender);
        result.put("activityLevel", activityLevel);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }

    /**
     * Create User object from Firebase Map
     * @param map Map from Firebase document
     * @return User object or null if invalid data
     */
    public static User fromMap(Map<String, Object> map) {
        if (map == null) return null;

        try {
            User user = new User();
            user.setUid((String) map.get("uid"));
            user.setName((String) map.get("name"));
            user.setEmail((String) map.get("email"));

            // Handle numeric fields safely
            Object heightObj = map.get("height");
            if (heightObj instanceof Number) {
                user.setHeight(((Number) heightObj).doubleValue());
            }

            Object weightObj = map.get("weight");
            if (weightObj instanceof Number) {
                user.setWeight(((Number) weightObj).doubleValue());
            }

            Object ageObj = map.get("age");
            if (ageObj instanceof Number) {
                user.setAge(((Number) ageObj).intValue());
            }

            user.setGender((String) map.get("gender"));
            user.setActivityLevel((String) map.get("activityLevel"));

            // Handle timestamp fields
            Object createdAtObj = map.get("createdAt");
            if (createdAtObj instanceof Number) {
                user.setCreatedAt(((Number) createdAtObj).longValue());
            }

            Object updatedAtObj = map.get("updatedAt");
            if (updatedAtObj instanceof Number) {
                user.setUpdatedAt(((Number) updatedAtObj).longValue());
            }

            return user;
        } catch (Exception e) {
            return null;
        }
    }
}