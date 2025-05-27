package com.emsi.fittracker.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workout {
    private String id;
    private String title;
    private Date date;
    private List<Exercise> exercises;

    public Workout() {
        this.exercises = new ArrayList<>();
    }

    public Workout(String title, Date date) {
        this.title = title;
        this.date = date;
        this.exercises = new ArrayList<>();
    }

    public Workout(String id, String title, Date date, List<Exercise> exercises) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public Date getDate() { return date; }
    public List<Exercise> getExercises() { return exercises; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(Date date) { this.date = date; }
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Helper methods
    public void addExercise(Exercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    public void removeExercise(Exercise exercise) {
        if (exercises != null) {
            exercises.remove(exercise);
        }
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("date", date);

        List<Map<String, Object>> exercisesList = new ArrayList<>();
        if (exercises != null) {
            for (Exercise exercise : exercises) {
                exercisesList.add(exercise.toMap());
            }
        }
        result.put("exercises", exercisesList);
        return result;
    }
}
