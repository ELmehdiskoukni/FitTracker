package com.emsi.fittracker.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WorkoutSession {
    private String id;
    private Date date;
    private long duration;
    private String notes;
    private String workoutId; // Reference to associated workout




    private String userId;
    private String workoutName;

    public WorkoutSession() {
        // Required empty constructor for Firebase
    }

    public WorkoutSession(Date date, long duration, String notes) {
        this.date = date;
        this.duration = duration;
        this.notes = notes;
    }

    public WorkoutSession(String id, Date date, long duration, String notes, String workoutId) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.notes = notes;
        this.workoutId = workoutId;
    }
    public WorkoutSession(String id, Date date, long duration, String notes, String workoutId, String userId, String workoutName) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.notes = notes;
        this.workoutId = workoutId;
        this.userId = userId;
        this.workoutName = workoutName;
    }

    // Getters
    public String getId() { return id; }
    public Date getDate() { return date; }
    public long getDuration() { return duration; }
    public String getNotes() { return notes; }
    public String getWorkoutId() { return workoutId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDate(Date date) { this.date = date; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }
    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("date", date);
        result.put("duration", duration);
        result.put("notes", notes);
        result.put("workoutId", workoutId);
        result.put("userId", userId);
        result.put("workoutName", workoutName);
        return result;
    }
}
