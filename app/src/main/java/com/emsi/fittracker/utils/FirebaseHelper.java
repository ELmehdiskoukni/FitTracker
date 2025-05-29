package com.emsi.fittracker.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.emsi.fittracker.interfaces.AuthCallback;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.BmiRecord;
import com.emsi.fittracker.models.Exercise;
import com.emsi.fittracker.models.User;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.models.WorkoutSession;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String WORKOUTS_COLLECTION = "workouts";
    private static final String WORKOUT_SESSIONS_COLLECTION = "workout_sessions";
    private static final String BMI_RECORDS_COLLECTION = "bmi_records";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Singleton instance
    private static FirebaseHelper instance;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // Authentication methods
    public void signUpUser(String email, String password, String name, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), name, email);
                            saveUser(user, new DataCallback<User>() {
                                @Override
                                public void onSuccess(User result) {
                                    callback.onSuccess(firebaseUser);
                                }

                                @Override
                                public void onFailure(String error) {
                                    callback.onFailure(error);
                                }
                            });
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    public void signInUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // User methods
    public void saveUser(User user, DataCallback<User> callback) {
        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void getUser(String userId, DataCallback<User> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // FIXED WORKOUT METHODS - COMPLETE SOLUTION

    // Save a new workout
    public void saveWorkout(Workout workout, DataCallback<Workout> callback) {
        Log.d(TAG, "Attempting to save workout: " + workout.getTitle());

        // Get current user ID
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found");
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        try {
            // Create document reference
            DocumentReference docRef = db.collection(WORKOUTS_COLLECTION).document();
            String workoutId = docRef.getId();
            workout.setId(workoutId);

            // Prepare workout data
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("id", workoutId);
            workoutData.put("title", workout.getTitle());
            workoutData.put("date", workout.getDate());
            workoutData.put("userId", userId);
            workoutData.put("createdAt", new Date());
            workoutData.put("updatedAt", new Date());

            // Convert exercises to maps
            List<Map<String, Object>> exercisesList = new ArrayList<>();
            if (workout.getExercises() != null) {
                for (Exercise exercise : workout.getExercises()) {
                    if (exercise != null) {
                        Map<String, Object> exerciseMap = new HashMap<>();
                        exerciseMap.put("name", exercise.getName());
                        exerciseMap.put("sets", exercise.getSets());
                        exerciseMap.put("reps", exercise.getReps());
                        exerciseMap.put("weight", exercise.getWeight());
                        exercisesList.add(exerciseMap);
                    }
                }
            }
            workoutData.put("exercises", exercisesList);

            Log.d(TAG, "Saving workout data: " + workoutData.toString());

            // Save to Firestore
            docRef.set(workoutData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Workout saved successfully with ID: " + workoutId);
                        workout.setUserId(userId);
                        callback.onSuccess(workout);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving workout", e);
                        callback.onFailure("Failed to save workout: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception while saving workout", e);
            callback.onFailure("Error preparing workout data: " + e.getMessage());
        }
    }

    // Update an existing workout
    public void updateWorkout(Workout workout, DataCallback<Workout> callback) {
        Log.d(TAG, "Attempting to update workout: " + workout.getId());

        if (workout.getId() == null || workout.getId().isEmpty()) {
            Log.e(TAG, "Workout ID is null or empty");
            callback.onFailure("Workout ID is required for update");
            return;
        }

        try {
            // Prepare workout data
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("id", workout.getId());
            workoutData.put("title", workout.getTitle());
            workoutData.put("date", workout.getDate());
            workoutData.put("updatedAt", new Date());

            // Convert exercises to maps
            List<Map<String, Object>> exercisesList = new ArrayList<>();
            if (workout.getExercises() != null) {
                for (Exercise exercise : workout.getExercises()) {
                    if (exercise != null) {
                        Map<String, Object> exerciseMap = new HashMap<>();
                        exerciseMap.put("name", exercise.getName());
                        exerciseMap.put("sets", exercise.getSets());
                        exerciseMap.put("reps", exercise.getReps());
                        exerciseMap.put("weight", exercise.getWeight());
                        exercisesList.add(exerciseMap);
                    }
                }
            }
            workoutData.put("exercises", exercisesList);

            Log.d(TAG, "Updating workout with data: " + workoutData.toString());

            // Update in Firestore
            db.collection(WORKOUTS_COLLECTION)
                    .document(workout.getId())
                    .update(workoutData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Workout updated successfully: " + workout.getId());
                        callback.onSuccess(workout);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating workout", e);
                        callback.onFailure("Failed to update workout: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception while updating workout", e);
            callback.onFailure("Error preparing workout data: " + e.getMessage());
        }
    }

    // Get user's workouts
    public void getUserWorkouts(String userId, DataCallback<List<Workout>> callback) {
        Log.d(TAG, "Getting workouts for user: " + userId);

        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Workout workout = parseWorkoutFromDocument(document);
                            if (workout != null) {
                                workouts.add(workout);
                                Log.d(TAG, "Parsed workout: " + workout.getTitle() + " with " + workout.getExerciseCount() + " exercises");
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing workout: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Retrieved " + workouts.size() + " workouts");
                    callback.onSuccess(workouts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workouts", e);
                    callback.onFailure("Failed to load workouts: " + e.getMessage());
                });
    }

    // Get a specific workout by ID
    public void getWorkout(String workoutId, DataCallback<Workout> callback) {
        Log.d(TAG, "Getting workout: " + workoutId);

        db.collection(WORKOUTS_COLLECTION)
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Workout workout = parseWorkoutFromDocument(documentSnapshot);
                            if (workout != null) {
                                Log.d(TAG, "Retrieved workout: " + workout.getTitle() + " with " + workout.getExerciseCount() + " exercises");
                                callback.onSuccess(workout);
                            } else {
                                callback.onFailure("Failed to parse workout data");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing workout", e);
                            callback.onFailure("Error parsing workout: " + e.getMessage());
                        }
                    } else {
                        Log.w(TAG, "Workout not found: " + workoutId);
                        callback.onFailure("Workout not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workout", e);
                    callback.onFailure("Failed to load workout: " + e.getMessage());
                });
    }

    // Delete a workout
    public void deleteWorkout(String workoutId, DataCallback<Void> callback) {
        if (workoutId == null || workoutId.isEmpty()) {
            callback.onFailure("Workout ID is required for deletion");
            return;
        }

        Log.d(TAG, "Deleting workout: " + workoutId);

        db.collection(WORKOUTS_COLLECTION)
                .document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout deleted successfully: " + workoutId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting workout", e);
                    callback.onFailure("Failed to delete workout: " + e.getMessage());
                });
    }

    // Helper method to parse workout from Firestore document
    private Workout parseWorkoutFromDocument(DocumentSnapshot document) {
        try {
            String id = document.getId();
            String title = document.getString("title");
            Date date = document.getDate("date");
            String userId = document.getString("userId");
            Date createdAt = document.getDate("createdAt");
            Date updatedAt = document.getDate("updatedAt");

            // Parse exercises list
            List<Exercise> exercises = new ArrayList<>();
            Object exercisesObj = document.get("exercises");

            if (exercisesObj instanceof List) {
                List<?> exercisesList = (List<?>) exercisesObj;
                for (Object exerciseObj : exercisesList) {
                    if (exerciseObj instanceof Map) {
                        Map<?, ?> exerciseMap = (Map<?, ?>) exerciseObj;
                        Exercise exercise = parseExerciseFromMap(exerciseMap);
                        if (exercise != null) {
                            exercises.add(exercise);
                        }
                    }
                }
            }

            Workout workout = new Workout(id, title, date, exercises, userId, createdAt, updatedAt);
            return workout;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing workout from document: " + document.getId(), e);
            return null;
        }
    }

    // Helper method to parse exercise from map
    private Exercise parseExerciseFromMap(Map<?, ?> exerciseMap) {
        try {
            String name = (String) exerciseMap.get("name");

            // Handle different number types for sets, reps, and weight
            int sets = 0;
            int reps = 0;
            double weight = 0.0;

            Object setsObj = exerciseMap.get("sets");
            if (setsObj instanceof Number) {
                sets = ((Number) setsObj).intValue();
            }

            Object repsObj = exerciseMap.get("reps");
            if (repsObj instanceof Number) {
                reps = ((Number) repsObj).intValue();
            }

            Object weightObj = exerciseMap.get("weight");
            if (weightObj instanceof Number) {
                weight = ((Number) weightObj).doubleValue();
            }

            if (name != null && !name.trim().isEmpty() && sets > 0 && reps > 0) {
                return new Exercise(name, sets, reps, weight);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing exercise from map", e);
        }
        return null;
    }

    // BMI Record methods (keeping existing ones)
    public void saveBmiRecord(BmiRecord bmiRecord, DataCallback<BmiRecord> callback) {
        DocumentReference docRef = db.collection(BMI_RECORDS_COLLECTION).document();
        bmiRecord.setId(docRef.getId());

        docRef.set(bmiRecord.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "BMI record saved successfully");
                    callback.onSuccess(bmiRecord);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving BMI record", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void getUserBmiRecords(String userId, DataCallback<List<BmiRecord>> callback) {
        db.collection(BMI_RECORDS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BmiRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            BmiRecord record = document.toObject(BmiRecord.class);
                            if (record != null) {
                                records.add(record);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing BMI record: " + document.getId(), e);
                        }
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting BMI records", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteBmiRecord(String recordId, DataCallback<Void> callback) {
        if (recordId == null || recordId.isEmpty()) {
            callback.onFailure("BMI record ID is required for deletion");
            return;
        }

        db.collection(BMI_RECORDS_COLLECTION)
                .document(recordId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "BMI record deleted successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting BMI record", e);
                    callback.onFailure(e.getMessage());
                });
    }


// WorkoutSession methods

    public void saveWorkoutSession(WorkoutSession session, DataCallback<WorkoutSession> callback) {
        DocumentReference docRef = db.collection(WORKOUT_SESSIONS_COLLECTION).document();
        session.setId(docRef.getId());

        docRef.set(session.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout session saved successfully");
                    callback.onSuccess(session);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving workout session", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void getUserWorkoutSessions(String userId, DataCallback<List<WorkoutSession>> callback) {
        db.collection(WORKOUT_SESSIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WorkoutSession> sessions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WorkoutSession session = parseWorkoutSessionFromDocument(document);
                            if (session != null) {
                                sessions.add(session);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing workout session: " + document.getId(), e);
                        }
                    }
                    Log.d(TAG, "Retrieved " + sessions.size() + " workout sessions");
                    callback.onSuccess(sessions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workout sessions", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteWorkoutSession(String sessionId, DataCallback<Void> callback) {
        if (sessionId == null || sessionId.isEmpty()) {
            callback.onFailure("Session ID is required for deletion");
            return;
        }

        db.collection(WORKOUT_SESSIONS_COLLECTION)
                .document(sessionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout session deleted successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting workout session", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // Helper method to parse WorkoutSession from Firestore document
    private WorkoutSession parseWorkoutSessionFromDocument(DocumentSnapshot document) {
        try {
            String id = document.getId();
            Date date = document.getDate("date");
            Long duration = document.getLong("duration");
            String notes = document.getString("notes");
            String workoutId = document.getString("workoutId");
            String userId = document.getString("userId");
            String workoutName = document.getString("workoutName");

            WorkoutSession session = new WorkoutSession(id, date,
                    duration != null ? duration : 0, notes, workoutId);

            // Set additional fields if available
            if (userId != null) {
                session.setUserId(userId);
            }
            if (workoutName != null) {
                session.setWorkoutName(workoutName);
            }

            return session;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing workout session from document: " + document.getId(), e);
            return null;
        }
    }
}