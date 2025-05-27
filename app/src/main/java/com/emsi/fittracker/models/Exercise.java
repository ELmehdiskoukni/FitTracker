package com.emsi.fittracker.models;

import java.util.HashMap;
import java.util.Map;

public class Exercise {
    private String name;
    private int sets;
    private int reps;
    private double weight;

    public Exercise() {
        // Required empty constructor for Firebase
    }

    public Exercise(String name, int sets, int reps, double weight) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    // Getters
    public String getName() { return name; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeight() { return weight; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(int reps) { this.reps = reps; }
    public void setWeight(double weight) { this.weight = weight; }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("sets", sets);
        result.put("reps", reps);
        result.put("weight", weight);
        return result;
    }
}

