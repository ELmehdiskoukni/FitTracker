package com.emsi.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.adapters.ActiveExerciseAdapter;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.Exercise;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.models.WorkoutSession;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkoutSessionActivity extends AppCompatActivity implements ActiveExerciseAdapter.OnExerciseCompletedListener {

    private static final String TAG = "WorkoutSessionActivity";
    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_WORKOUT_TITLE = "workout_title";

    // UI Components
    private Toolbar toolbar;
    private TextView tvWorkoutTitle;
    private Chronometer chronometer;
    private RecyclerView recyclerViewExercises;
    private Button btnFinishWorkout;
    private Button btnPauseResume;

    // Data
    private ActiveExerciseAdapter exerciseAdapter;
    private List<Exercise> exercises;
    private String workoutId;
    private String workoutTitle;
    private long startTime;
    private boolean isPaused = false;
    private long pausedTime = 0;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        firebaseHelper = FirebaseHelper.getInstance();

        // Get workout data from intent
        workoutId = getIntent().getStringExtra(EXTRA_WORKOUT_ID);
        workoutTitle = getIntent().getStringExtra(EXTRA_WORKOUT_TITLE);

        if (workoutTitle != null) {
            tvWorkoutTitle.setText(workoutTitle);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Session: " + workoutTitle);
            }
        }

        loadWorkoutExercises();
        startWorkoutSession();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        chronometer = findViewById(R.id.chronometer);
        recyclerViewExercises = findViewById(R.id.recyclerViewExercises);
        btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        btnPauseResume = findViewById(R.id.btnPauseResume);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Session d'entraînement");
        }
    }

    private void setupRecyclerView() {
        exercises = new ArrayList<>();
        exerciseAdapter = new ActiveExerciseAdapter(exercises, this);
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExercises.setAdapter(exerciseAdapter);
    }

    private void setupListeners() {
        btnFinishWorkout.setOnClickListener(v -> showFinishWorkoutDialog());
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
    }

    private void loadWorkoutExercises() {
        if (workoutId == null || workoutId.isEmpty()) {
            Toast.makeText(this, "ID d'entraînement manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper.getWorkout(workoutId, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout.getExercises() != null) {
                    exercises.clear();
                    exercises.addAll(workout.getExercises());
                    exerciseAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + exercises.size() + " exercises for session");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load workout exercises: " + error);
                Toast.makeText(WorkoutSessionActivity.this, "Erreur de chargement des exercices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startWorkoutSession() {
        startTime = SystemClock.elapsedRealtime();
        chronometer.setBase(startTime);
        chronometer.start();
        Log.d(TAG, "Workout session started");
    }

    private void togglePauseResume() {
        if (isPaused) {
            // Resume
            long pauseDuration = SystemClock.elapsedRealtime() - pausedTime;
            chronometer.setBase(chronometer.getBase() + pauseDuration);
            chronometer.start();
            btnPauseResume.setText("Pause");
            isPaused = false;
            Log.d(TAG, "Workout session resumed");
        } else {
            // Pause
            chronometer.stop();
            pausedTime = SystemClock.elapsedRealtime();
            btnPauseResume.setText("Reprendre");
            isPaused = true;
            Log.d(TAG, "Workout session paused");
        }
    }

    private void showFinishWorkoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_finish_workout, null);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        new AlertDialog.Builder(this)
                .setTitle("Terminer l'entraînement")
                .setMessage("Voulez-vous terminer cette session d'entraînement ?")
                .setView(dialogView)
                .setPositiveButton("Terminer", (dialog, which) -> {
                    String notes = etNotes.getText().toString().trim();
                    finishWorkoutSession(notes);
                })
                .setNegativeButton("Continuer", null)
                .show();
    }

    private void finishWorkoutSession(String notes) {
        chronometer.stop();

        // Calculate duration in minutes
        long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        long calculatedDuration = elapsedMillis / (1000 * 60);

        // Ensure minimum duration of 1 minute
        final long durationMinutes = Math.max(calculatedDuration, 1);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create workout session record
        WorkoutSession session = new WorkoutSession(new Date(), durationMinutes, notes);
        session.setWorkoutId(workoutId);
        session.setUserId(currentUser.getUid());
        session.setWorkoutName(workoutTitle);

        // Save to Firebase
        firebaseHelper.saveWorkoutSession(session, new DataCallback<WorkoutSession>() {
            @Override
            public void onSuccess(WorkoutSession result) {
                Log.d(TAG, "Workout session saved successfully");
                Toast.makeText(WorkoutSessionActivity.this,
                        "Session terminée ! Durée: " + durationMinutes + " minutes",
                        Toast.LENGTH_LONG).show();

                // Return to main activity and switch to progress tab
                Intent intent = new Intent(WorkoutSessionActivity.this, MainActivity.class);
                intent.putExtra("switch_to_progress", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to save workout session: " + error);
                Toast.makeText(WorkoutSessionActivity.this,
                        "Erreur lors de la sauvegarde: " + error,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onExerciseCompleted(Exercise exercise, int position) {
        // Handle exercise completion (optional visual feedback)
        Toast.makeText(this, exercise.getName() + " terminé !", Toast.LENGTH_SHORT).show();

        // Check if all exercises are completed
        boolean allCompleted = exerciseAdapter.areAllExercisesCompleted();
        if (allCompleted) {
            btnFinishWorkout.setText("Terminer l'entraînement ✓");
            btnFinishWorkout.setBackgroundTintList(getColorStateList(R.color.success_green));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showExitConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quitter la session")
                .setMessage("Êtes-vous sûr de vouloir quitter cette session d'entraînement ? Votre progression sera perdue.")
                .setPositiveButton("Quitter", (dialog, which) -> {
                    chronometer.stop();
                    finish();
                })
                .setNegativeButton("Continuer", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
            togglePauseResume(); // Auto-pause when activity goes to background
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-resume is handled by user interaction
    }
}