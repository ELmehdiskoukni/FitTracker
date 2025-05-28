package com.emsi.fittracker;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fittracker.R;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;

public class AddEditWorkoutActivity extends AppCompatActivity {

    private static final String TAG = "AddEditWorkoutActivity";

    private EditText etWorkoutTitle;
    private Button btnSaveWorkout;
    private Toolbar toolbar;

    private FirebaseHelper firebaseHelper;
    private String currentUserId;
    private String workoutId; // null for new workout
    private boolean isEditMode = false;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_workout);

        initViews();
        setupToolbar();
        setupListeners();

        firebaseHelper = FirebaseHelper.getInstance();

        // Check authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        // Check if we're editing an existing workout
        workoutId = getIntent().getStringExtra("workout_id");
        if (workoutId != null && !workoutId.isEmpty()) {
            isEditMode = true;
            String workoutTitle = getIntent().getStringExtra("workout_title");
            if (workoutTitle != null) {
                etWorkoutTitle.setText(workoutTitle);
            }
            toolbar.setTitle("Modifier l'entraînement");
            Log.d(TAG, "Edit mode - Workout ID: " + workoutId);
        } else {
            toolbar.setTitle("Nouvel entraînement");
            Log.d(TAG, "Create mode");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etWorkoutTitle = findViewById(R.id.etWorkoutTitle);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());
    }

    private void saveWorkout() {
        String title = etWorkoutTitle.getText().toString().trim();

        if (title.isEmpty()) {
            etWorkoutTitle.setError("Le titre est requis");
            etWorkoutTitle.requestFocus();
            return;
        }

        if (isSaving) {
            Log.d(TAG, "Already saving workout, ignoring request");
            return;
        }

        isSaving = true;
        btnSaveWorkout.setEnabled(false);
        btnSaveWorkout.setText("Sauvegarde...");

        Log.d(TAG, "Saving workout: " + title + ", Edit mode: " + isEditMode);

        if (isEditMode) {
            updateExistingWorkout(title);
        } else {
            createNewWorkout(title);
        }
    }

    private void createNewWorkout(String title) {
        Log.d(TAG, "Creating new workout: " + title);

        try {
            // Create new workout with empty exercises list
            Workout workout = new Workout(title, new Date());
            workout.setExercises(new ArrayList<>()); // Initialize empty exercises list

            Log.d(TAG, "Workout object created, calling FirebaseHelper");

            firebaseHelper.saveWorkout(workout, new DataCallback<Workout>() {
                @Override
                public void onSuccess(Workout result) {
                    Log.d(TAG, "Workout created successfully: " + result.getId());
                    runOnUiThread(() -> {
                        Toast.makeText(AddEditWorkoutActivity.this, "Entraînement créé avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to create workout: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(AddEditWorkoutActivity.this, "Erreur lors de la création: " + error, Toast.LENGTH_LONG).show();
                        resetSaveButton();
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception while creating workout", e);
            Toast.makeText(this, "Erreur lors de la préparation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetSaveButton();
        }
    }

    private void updateExistingWorkout(String title) {
        Log.d(TAG, "Updating existing workout: " + workoutId + " with title: " + title);

        // First get the existing workout to preserve exercises
        firebaseHelper.getWorkout(workoutId, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                Log.d(TAG, "Retrieved existing workout for update");

                // Update only the title, keep everything else
                workout.setTitle(title);

                firebaseHelper.updateWorkout(workout, new DataCallback<Workout>() {
                    @Override
                    public void onSuccess(Workout result) {
                        Log.d(TAG, "Workout updated successfully: " + result.getId());
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditWorkoutActivity.this, "Entraînement modifié avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to update workout: " + error);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditWorkoutActivity.this, "Erreur lors de la modification: " + error, Toast.LENGTH_LONG).show();
                            resetSaveButton();
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to retrieve workout for update: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, "Erreur lors de la récupération: " + error, Toast.LENGTH_LONG).show();
                    resetSaveButton();
                });
            }
        });
    }

    private void resetSaveButton() {
        isSaving = false;
        btnSaveWorkout.setEnabled(true);
        btnSaveWorkout.setText("Enregistrer");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reset saving state if activity is destroyed
        isSaving = false;
    }
}