package com.emsi.fittracker.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BmiRecord {
    private String id;
    private String userId;
    private Date date;
    private double weight;
    private double height;
    private double bmi;
    private long timestamp;

    public BmiRecord() {
        // Required empty constructor for Firebase
    }

    public BmiRecord(Date date, double weight, double height) {
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bmi = calculateBMI(weight, height);
        this.timestamp = date.getTime();
    }

    public BmiRecord(String id, String userId, Date date, double weight, double height, double bmi) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.timestamp = date.getTime();
    }

    // Calculate BMI
    private double calculateBMI(double weight, double height) {
        if (height > 0) {
            double heightInMeters = height / 100.0; // Convert cm to meters
            return weight / (heightInMeters * heightInMeters);
        }
        return 0.0;
    }

    // Get BMI category as string
    public String getBmiCategory() {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi <= 24.9) {
            return "Normal Weight";
        } else if (bmi <= 29.9) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    // Get BMI status color resource
    public int getBmiCategoryColor() {
        if (bmi < 18.5) {
            return android.R.color.holo_blue_dark;
        } else if (bmi <= 24.9) {
            return android.R.color.holo_green_dark;
        } else if (bmi <= 29.9) {
            return android.R.color.holo_orange_dark;
        } else {
            return android.R.color.holo_red_dark;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public Date getDate() { return date; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public double getBmi() { return bmi; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDate(Date date) {
        this.date = date;
        this.timestamp = date != null ? date.getTime() : 0;
    }
    public void setWeight(double weight) {
        this.weight = weight;
        this.bmi = calculateBMI(weight, height);
    }
    public void setHeight(double height) {
        this.height = height;
        this.bmi = calculateBMI(weight, height);
    }
    public void setBmi(double bmi) { this.bmi = bmi; }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        if (timestamp > 0) {
            this.date = new Date(timestamp);
        }
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("date", date);
        result.put("weight", weight);
        result.put("height", height);
        result.put("bmi", bmi);
        result.put("timestamp", timestamp);
        return result;
    }

    // Create from Firebase document
    public static BmiRecord fromMap(Map<String, Object> map) {
        BmiRecord record = new BmiRecord();
        record.setId((String) map.get("id"));
        record.setUserId((String) map.get("userId"));

        Object dateObj = map.get("date");
        if (dateObj instanceof Date) {
            record.setDate((Date) dateObj);
        } else if (dateObj instanceof Long) {
            record.setDate(new Date((Long) dateObj));
        }

        Object weightObj = map.get("weight");
        if (weightObj instanceof Number) {
            record.setWeight(((Number) weightObj).doubleValue());
        }

        Object heightObj = map.get("height");
        if (heightObj instanceof Number) {
            record.setHeight(((Number) heightObj).doubleValue());
        }

        Object bmiObj = map.get("bmi");
        if (bmiObj instanceof Number) {
            record.setBmi(((Number) bmiObj).doubleValue());
        }

        Object timestampObj = map.get("timestamp");
        if (timestampObj instanceof Number) {
            record.setTimestamp(((Number) timestampObj).longValue());
        }

        return record;
    }

    @Override
    public String toString() {
        return "BmiRecord{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", date=" + date +
                ", weight=" + weight +
                ", height=" + height +
                ", bmi=" + bmi +
                ", category='" + getBmiCategory() + '\'' +
                '}';
    }
}