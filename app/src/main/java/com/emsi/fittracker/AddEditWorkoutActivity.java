package com.emsi.fittracker;

import android.os.Bundle;
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

import java.util.Date;

public class AddEditWorkoutActivity extends AppCompatActivity {

    private EditText etWorkoutTitle;
    private Button btnSaveWorkout;
    private Toolbar toolbar;

    private FirebaseHelper firebaseHelper;
    private String currentUserId;
    private String workoutId; // null for new workout
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_workout);

        initViews();
        setupToolbar();
        setupListeners();

        firebaseHelper = FirebaseHelper.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if we're editing an existing workout
        workoutId = getIntent().getStringExtra("workout_id");
        if (workoutId != null) {
            isEditMode = true;
            String workoutTitle = getIntent().getStringExtra("workout_title");
            etWorkoutTitle.setText(workoutTitle);
            toolbar.setTitle("Modifier l'entraînement");
        } else {
            toolbar.setTitle("Nouvel entraînement");
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

        btnSaveWorkout.setEnabled(false);
        btnSaveWorkout.setText("Sauvegarde...");

        if (isEditMode) {
            updateExistingWorkout(title);
        } else {
            createNewWorkout(title);
        }
    }

    private void createNewWorkout(String title) {
        Workout workout = new Workout(title, new Date());

        firebaseHelper.saveWorkout(workout, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout result) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, "Entraînement créé", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    btnSaveWorkout.setEnabled(true);
                    btnSaveWorkout.setText("Enregistrer");
                });
            }
        });
    }

    private void updateExistingWorkout(String title) {
        firebaseHelper.getWorkout(workoutId, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                workout.setTitle(title);

                firebaseHelper.updateWorkout(workout, new DataCallback<Workout>() {
                    @Override
                    public void onSuccess(Workout result) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditWorkoutActivity.this, "Entraînement modifié", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditWorkoutActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                            btnSaveWorkout.setEnabled(true);
                            btnSaveWorkout.setText("Enregistrer");
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditWorkoutActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    btnSaveWorkout.setEnabled(true);
                    btnSaveWorkout.setText("Enregistrer");
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}