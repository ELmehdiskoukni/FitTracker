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
    private String userId; // Add userId field
    private Date createdAt; // Add createdAt field
    private Date updatedAt; // Add updatedAt field

    public Workout() {
        this.exercises = new ArrayList<>();
        this.date = new Date(); // Default to current date
    }

    public Workout(String title, Date date) {
        this.title = title;
        this.date = date != null ? date : new Date();
        this.exercises = new ArrayList<>();
    }

    public Workout(String id, String title, Date date, List<Exercise> exercises) {
        this.id = id;
        this.title = title;
        this.date = date != null ? date : new Date();
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Complete constructor with all fields
    public Workout(String id, String title, Date date, List<Exercise> exercises, String userId, Date createdAt, Date updatedAt) {
        this.id = id;
        this.title = title;
        this.date = date != null ? date : new Date();
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public Date getDate() { return date; }
    public List<Exercise> getExercises() { return exercises; }
    public String getUserId() { return userId; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(Date date) { this.date = date != null ? date : new Date(); }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Helper methods
    public void addExercise(Exercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        if (exercise != null) {
            exercises.add(exercise);
        }
    }

    public void removeExercise(Exercise exercise) {
        if (exercises != null && exercise != null) {
            exercises.remove(exercise);
        }
    }

    public void removeExerciseAt(int index) {
        if (exercises != null && index >= 0 && index < exercises.size()) {
            exercises.remove(index);
        }
    }

    public int getExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }

    public boolean hasExercises() {
        return exercises != null && !exercises.isEmpty();
    }

    // Calculate estimated workout duration based on exercise count
    public int getEstimatedDuration() {
        int exerciseCount = getExerciseCount();
        return exerciseCount * 3; // Assuming 3 minutes per exercise
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("date", date);
        result.put("userId", userId);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);

        List<Map<String, Object>> exercisesList = new ArrayList<>();
        if (exercises != null) {
            for (Exercise exercise : exercises) {
                if (exercise != null) {
                    exercisesList.add(exercise.toMap());
                }
            }
        }
        result.put("exercises", exercisesList);
        return result;
    }

    // Create from Firebase document map
    public static Workout fromMap(Map<String, Object> map) {
        Workout workout = new Workout();

        workout.setId((String) map.get("id"));
        workout.setTitle((String) map.get("title"));
        workout.setUserId((String) map.get("userId"));

        // Handle date fields
        Object dateObj = map.get("date");
        if (dateObj instanceof Date) {
            workout.setDate((Date) dateObj);
        }

        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof Date) {
            workout.setCreatedAt((Date) createdAtObj);
        }

        Object updatedAtObj = map.get("updatedAt");
        if (updatedAtObj instanceof Date) {
            workout.setUpdatedAt((Date) updatedAtObj);
        }

        // Handle exercises list
        Object exercisesObj = map.get("exercises");
        if (exercisesObj instanceof List) {
            List<?> exercisesList = (List<?>) exercisesObj;
            List<Exercise> exercises = new ArrayList<>();

            for (Object exerciseObj : exercisesList) {
                if (exerciseObj instanceof Map) {
                    Map<?, ?> exerciseMap = (Map<?, ?>) exerciseObj;
                    Exercise exercise = Exercise.fromMap(exerciseMap);
                    if (exercise != null) {
                        exercises.add(exercise);
                    }
                }
            }
            workout.setExercises(exercises);
        }

        return workout;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", exerciseCount=" + getExerciseCount() +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Workout workout = (Workout) obj;
        return id != null ? id.equals(workout.id) : workout.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}