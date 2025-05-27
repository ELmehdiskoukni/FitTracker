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
import java.util.List;

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
    public void saveWorkout(Workout workout, DataCallback<Workout> callback) {
        DocumentReference docRef = db.collection(WORKOUTS_COLLECTION).document();
        workout.setId(docRef.getId());

        docRef.set(workout.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout saved successfully");
                    callback.onSuccess(workout);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving workout", e);
                    callback.onFailure(e.getMessage());
                });
    }

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

    public void updateWorkout(Workout workout, DataCallback<Workout> callback) {
        db.collection(WORKOUTS_COLLECTION)
                .document(workout.getId())
                .set(workout.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Workout updated successfully");
                    callback.onSuccess(workout);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating workout", e);
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
                        BmiRecord record = document.toObject(BmiRecord.class);
                        records.add(record);
                    }
                    callback.onSuccess(records);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting BMI records", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteBmiRecord(String recordId, DataCallback<Void> callback) {
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


}