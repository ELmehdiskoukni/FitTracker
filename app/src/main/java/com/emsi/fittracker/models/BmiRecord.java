package com.emsi.fittracker.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BmiRecord {
    private String id;
    private Date date;
    private double weight;
    private double height;
    private double bmi;

    public BmiRecord() {
        // Required empty constructor for Firebase
    }

    public BmiRecord(Date date, double weight, double height) {
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bmi = calculateBMI(weight, height);
    }

    public BmiRecord(String id, Date date, double weight, double height, double bmi) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
    }

    // Calculate BMI
    private double calculateBMI(double weight, double height) {
        if (height > 0) {
            return weight / (height * height);
        }
        return 0.0;
    }

    // Getters
    public String getId() { return id; }
    public Date getDate() { return date; }
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public double getBmi() { return bmi; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDate(Date date) { this.date = date; }
    public void setWeight(double weight) {
        this.weight = weight;
        this.bmi = calculateBMI(weight, height);
    }
    public void setHeight(double height) {
        this.height = height;
        this.bmi = calculateBMI(weight, height);
    }
    public void setBmi(double bmi) { this.bmi = bmi; }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("date", date);
        result.put("weight", weight);
        result.put("height", height);
        result.put("bmi", bmi);
        return result;
    }
}
