package com.emsi.fittracker.utils;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.emsi.fittracker.interfaces.AuthCallback;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.BmiRecord;
import com.emsi.fittracker.models.User;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.models.WorkoutSession;

import java.util.ArrayList;
import java.util.Date;
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
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void signInUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException().getMessage());
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

    // Workout methods


    public void getUserWorkouts(String userId, DataCallback<List<Workout>> callback) {
        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Workout workout = document.toObject(Workout.class);
                        workouts.add(workout);
                    }
                    callback.onSuccess(workouts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workouts", e);
                    callback.onFailure(e.getMessage());
                });
    }



    public void deleteWorkout(String workoutId, DataCallback<Void> callback) {
        db.collection(WORKOUTS_COLLECTION)
                .document(workoutId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout deleted successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting workout", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void getWorkout(String workoutId, DataCallback<Workout> callback) {
        db.collection(WORKOUTS_COLLECTION)
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Workout workout = documentSnapshot.toObject(Workout.class);
                            if (workout != null) {
                                workout.setId(documentSnapshot.getId());
                                callback.onSuccess(workout);
                            } else {
                                callback.onFailure("Failed to parse workout data");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing workout", e);
                            callback.onFailure("Error parsing workout: " + e.getMessage());
                        }
                    } else {
                        callback.onFailure("Workout not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workout", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // Update the existing saveWorkout method to include userId
    public void saveWorkout(Workout workout, DataCallback<Workout> callback) {
        // Get current user ID
        String userId = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        DocumentReference docRef = db.collection(WORKOUTS_COLLECTION).document();
        workout.setId(docRef.getId());

        // Add userId and createdAt to the workout data
        Map<String, Object> workoutData = workout.toMap();
        workoutData.put("userId", userId);
        workoutData.put("createdAt", new Date());

        docRef.set(workoutData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout saved successfully");
                    callback.onSuccess(workout);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving workout", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // Update the existing updateWorkout method
    public void updateWorkout(Workout workout, DataCallback<Workout> callback) {
        if (workout.getId() == null) {
            callback.onFailure("Workout ID is required for update");
            return;
        }

        // Add updatedAt to the workout data
        Map<String, Object> workoutData = workout.toMap();
        workoutData.put("updatedAt", new Date());

        db.collection(WORKOUTS_COLLECTION)
                .document(workout.getId())
                .set(workoutData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout updated successfully");
                    callback.onSuccess(workout);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating workout", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // Workout Session methods
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
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WorkoutSession> sessions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WorkoutSession session = document.toObject(WorkoutSession.class);
                        sessions.add(session);
                    }
                    callback.onSuccess(sessions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting workout sessions", e);
                    callback.onFailure(e.getMessage());
                });
    }


    // BMI Record methods
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

    public void updateBmiRecord(BmiRecord bmiRecord, DataCallback<BmiRecord> callback) {
        if (bmiRecord.getId() == null || bmiRecord.getId().isEmpty()) {
            callback.onFailure("BMI record ID is required for update");
            return;
        }

        db.collection(BMI_RECORDS_COLLECTION)
                .document(bmiRecord.getId())
                .set(bmiRecord.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "BMI record updated successfully");
                    callback.onSuccess(bmiRecord);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating BMI record", e);
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

    // Get BMI statistics for a user
    public void getBmiStatistics(String userId, DataCallback<BmiStatistics> callback) {
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

                    // Calculate statistics
                    BmiStatistics stats = calculateBmiStatistics(records);
                    callback.onSuccess(stats);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting BMI statistics", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // Helper method to calculate BMI statistics
    private BmiStatistics calculateBmiStatistics(List<BmiRecord> records) {
        if (records.isEmpty()) {
            return new BmiStatistics(0, 0.0, 0.0, 0.0, null, null);
        }

        double totalBmi = 0.0;
        double minBmi = Double.MAX_VALUE;
        double maxBmi = Double.MIN_VALUE;
        BmiRecord latest = records.get(0); // Already sorted by timestamp desc
        BmiRecord oldest = records.get(records.size() - 1);

        for (BmiRecord record : records) {
            double bmi = record.getBmi();
            totalBmi += bmi;
            minBmi = Math.min(minBmi, bmi);
            maxBmi = Math.max(maxBmi, bmi);
        }

        double averageBmi = totalBmi / records.size();

        return new BmiStatistics(records.size(), averageBmi, minBmi, maxBmi, latest, oldest);
    }

    // BMI Statistics inner class
    public static class BmiStatistics {
        private int totalRecords;
        private double averageBmi;
        private double minBmi;
        private double maxBmi;
        private BmiRecord latestRecord;
        private BmiRecord oldestRecord;

        public BmiStatistics(int totalRecords, double averageBmi, double minBmi, double maxBmi,
                             BmiRecord latestRecord, BmiRecord oldestRecord) {
            this.totalRecords = totalRecords;
            this.averageBmi = averageBmi;
            this.minBmi = minBmi;
            this.maxBmi = maxBmi;
            this.latestRecord = latestRecord;
            this.oldestRecord = oldestRecord;
        }

        // Getters
        public int getTotalRecords() { return totalRecords; }
        public double getAverageBmi() { return averageBmi; }
        public double getMinBmi() { return minBmi; }
        public double getMaxBmi() { return maxBmi; }
        public BmiRecord getLatestRecord() { return latestRecord; }
        public BmiRecord getOldestRecord() { return oldestRecord; }

        public String getAverageBmiCategory() {
            return ValidationUtils.getBmiCategory(averageBmi);
        }
    }
}