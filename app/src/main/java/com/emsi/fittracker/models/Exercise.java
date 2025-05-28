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
        this.name = name != null ? name.trim() : "";
        this.sets = Math.max(0, sets); // Ensure non-negative
        this.reps = Math.max(0, reps); // Ensure non-negative
        this.weight = Math.max(0.0, weight); // Ensure non-negative
    }

    // Getters
    public String getName() { return name; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public double getWeight() { return weight; }

    // Setters with validation
    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public void setSets(int sets) {
        this.sets = Math.max(0, sets);
    }

    public void setReps(int reps) {
        this.reps = Math.max(0, reps);
    }

    public void setWeight(double weight) {
        this.weight = Math.max(0.0, weight);
    }

    // Helper methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && sets > 0 && reps > 0;
    }

    public double getTotalVolume() {
        return sets * reps * weight;
    }

    public boolean isBodyweightExercise() {
        return weight == 0.0;
    }

    public String getFormattedWeight() {
        if (weight == 0.0) {
            return "Poids corporel";
        } else if (weight == (int) weight) {
            return String.format("%.0f kg", weight);
        } else {
            return String.format("%.1f kg", weight);
        }
    }

    public String getFormattedSetsReps() {
        return sets + " × " + reps;
    }

    public String getFormattedVolume() {
        double volume = getTotalVolume();
        if (volume == 0.0) {
            return "";
        } else if (volume == (int) volume) {
            return String.format("%.0f kg", volume);
        } else {
            return String.format("%.1f kg", volume);
        }
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("sets", sets);
        result.put("reps", reps);
        result.put("weight", weight);
        return result;
    }

    // Create from Firebase document map
    public static Exercise fromMap(Map<?, ?> map) {
        if (map == null) return null;

        try {
            String name = (String) map.get("name");

            // Handle different number types for sets, reps, and weight
            int sets = 0;
            int reps = 0;
            double weight = 0.0;

            Object setsObj = map.get("sets");
            if (setsObj instanceof Number) {
                sets = ((Number) setsObj).intValue();
            }

            Object repsObj = map.get("reps");
            if (repsObj instanceof Number) {
                reps = ((Number) repsObj).intValue();
            }

            Object weightObj = map.get("weight");
            if (weightObj instanceof Number) {
                weight = ((Number) weightObj).doubleValue();
            }

            if (name != null && !name.trim().isEmpty()) {
                return new Exercise(name, sets, reps, weight);
            }

        } catch (Exception e) {
            // Log error if needed
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "name='" + name + '\'' +
                ", sets=" + sets +
                ", reps=" + reps +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Exercise exercise = (Exercise) obj;
        return sets == exercise.sets &&
                reps == exercise.reps &&
                Double.compare(exercise.weight, weight) == 0 &&
                (name != null ? name.equals(exercise.name) : exercise.name == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + sets;
        result = 31 * result + reps;
        long weightBits = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (weightBits ^ (weightBits >>> 32));
        return result;
    }
}