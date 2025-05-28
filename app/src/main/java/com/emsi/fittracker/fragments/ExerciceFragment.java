package com.emsi.fittracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.adapters.ExerciseAdapter;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.Exercise;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciceFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {

    private static final String TAG = "ExerciceFragment";
    private static final String ARG_WORKOUT_ID = "workout_id";
    private static final String ARG_WORKOUT_TITLE = "workout_title";

    private String workoutId;
    private String workoutTitle;
    private Workout currentWorkout;

    private TextView tvWorkoutTitle;
    private RecyclerView recyclerViewExercises;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;
    private FloatingActionButton fabAddExercise;
    private View emptyStateView;
    private Button btnStartWorkout;

    private FirebaseHelper firebaseHelper;
    private boolean isLoading = false;
    private boolean isSaving = false;

    public static ExerciceFragment newInstance(String workoutId, String workoutTitle) {
        ExerciceFragment fragment = new ExerciceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORKOUT_ID, workoutId);
        args.putString(ARG_WORKOUT_TITLE, workoutTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workoutId = getArguments().getString(ARG_WORKOUT_ID);
            workoutTitle = getArguments().getString(ARG_WORKOUT_TITLE);
        }
        firebaseHelper = FirebaseHelper.getInstance();
        Log.d(TAG, "Fragment created for workout: " + workoutId + " - " + workoutTitle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        // Load workout details after views are initialized
        loadWorkoutDetails();
    }

    private void initViews(View view) {
        tvWorkoutTitle = view.findViewById(R.id.tvWorkoutTitle);
        recyclerViewExercises = view.findViewById(R.id.recyclerViewExercises);
        fabAddExercise = view.findViewById(R.id.fabAddExercise);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout);

        if (workoutTitle != null && !workoutTitle.isEmpty()) {
            tvWorkoutTitle.setText(workoutTitle);
        } else {
            tvWorkoutTitle.setText("Entraînement");
        }
    }

    private void setupRecyclerView() {
        exerciseList = new ArrayList<>();
        exerciseAdapter = new ExerciseAdapter(exerciseList, this);
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewExercises.setAdapter(exerciseAdapter);
    }

    private void setupListeners() {
        fabAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        btnStartWorkout.setOnClickListener(v -> startWorkoutSession());
    }

    private void loadWorkoutDetails() {
        if (workoutId == null || workoutId.isEmpty()) {
            Log.w(TAG, "Workout ID is null or empty");
            showError("ID d'entraînement manquant");
            // Create a new workout if ID is missing
            currentWorkout = new Workout(workoutTitle != null ? workoutTitle : "Nouvel entraînement", new Date());
            currentWorkout.setExercises(new ArrayList<>());
            updateEmptyState();
            return;
        }

        if (isLoading) {
            Log.d(TAG, "Already loading workout, skipping");
            return;
        }

        isLoading = true;
        Log.d(TAG, "Loading workout details for ID: " + workoutId);

        firebaseHelper.getWorkout(workoutId, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                isLoading = false;
                Log.d(TAG, "Workout loaded successfully: " + workout.getTitle() + " with " + workout.getExerciseCount() + " exercises");

                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "Fragment not attached, skipping UI update");
                    return;
                }

                currentWorkout = workout;
                exerciseList.clear();

                if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                    exerciseList.addAll(workout.getExercises());
                    Log.d(TAG, "Added " + exerciseList.size() + " exercises to list");
                }

                if (exerciseAdapter != null) {
                    exerciseAdapter.notifyDataSetChanged();
                }
                updateEmptyState();
            }

            @Override
            public void onFailure(String error) {
                isLoading = false;
                Log.e(TAG, "Failed to load workout: " + error);

                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "Fragment not attached, skipping error handling");
                    return;
                }

                showError("Erreur de chargement: " + error);
                // Create a new workout if loading fails
                if (currentWorkout == null) {
                    currentWorkout = new Workout(workoutId, workoutTitle != null ? workoutTitle : "Entraînement", new Date(), new ArrayList<>());
                    Log.d(TAG, "Created fallback workout");
                }
                updateEmptyState();
            }
        });
    }

    private void showAddExerciseDialog() {
        if (getContext() == null) return;

        Log.d(TAG, "Showing add exercise dialog");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_exercise, null);
        if (dialogView == null) {
            showError("Erreur lors du chargement du dialogue");
            return;
        }

        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Ajouter un exercice")
                .setView(dialogView)
                .setPositiveButton("Ajouter", null) // Set to null initially
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etExerciseName.getText().toString().trim();
                String setsStr = etSets.getText().toString().trim();
                String repsStr = etReps.getText().toString().trim();
                String weightStr = etWeight.getText().toString().trim();

                if (validateAndAddExercise(name, setsStr, repsStr, weightStr)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validateAndAddExercise(String name, String setsStr, String repsStr, String weightStr) {
        Log.d(TAG, "Validating and adding exercise: " + name);

        if (!validateExerciseInput(name, setsStr, repsStr, weightStr)) {
            return false;
        }

        try {
            int sets = Integer.parseInt(setsStr);
            int reps = Integer.parseInt(repsStr);
            double weight = weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr);

            Exercise exercise = new Exercise(name, sets, reps, weight);
            Log.d(TAG, "Created exercise: " + exercise.toString());

            addExerciseToWorkout(exercise);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format exception: " + e.getMessage());
            showError("Format numérique invalide");
            return false;
        }
    }

    private void showEditExerciseDialog(Exercise exercise, int position) {
        if (getContext() == null || exercise == null) return;

        Log.d(TAG, "Showing edit exercise dialog for position: " + position);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_exercise, null);
        if (dialogView == null) return;

        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);

        // Pre-fill with current values
        etExerciseName.setText(exercise.getName());
        etSets.setText(String.valueOf(exercise.getSets()));
        etReps.setText(String.valueOf(exercise.getReps()));
        if (exercise.getWeight() > 0) {
            etWeight.setText(String.valueOf(exercise.getWeight()));
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Modifier l'exercice")
                .setView(dialogView)
                .setPositiveButton("Modifier", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etExerciseName.getText().toString().trim();
                String setsStr = etSets.getText().toString().trim();
                String repsStr = etReps.getText().toString().trim();
                String weightStr = etWeight.getText().toString().trim();

                if (validateAndUpdateExercise(exercise, position, name, setsStr, repsStr, weightStr)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean validateAndUpdateExercise(Exercise exercise, int position, String name, String setsStr, String repsStr, String weightStr) {
        Log.d(TAG, "Validating and updating exercise at position: " + position);

        if (!validateExerciseInput(name, setsStr, repsStr, weightStr)) {
            return false;
        }

        try {
            int sets = Integer.parseInt(setsStr);
            int reps = Integer.parseInt(repsStr);
            double weight = weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr);

            exercise.setName(name);
            exercise.setSets(sets);
            exercise.setReps(reps);
            exercise.setWeight(weight);

            Log.d(TAG, "Updated exercise: " + exercise.toString());
            updateExerciseInWorkout(position);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format exception: " + e.getMessage());
            showError("Format numérique invalide");
            return false;
        }
    }

    private boolean validateExerciseInput(String name, String sets, String reps, String weight) {
        if (name.isEmpty()) {
            showError("Le nom de l'exercice est requis");
            return false;
        }

        if (sets.isEmpty()) {
            showError("Le nombre de séries est requis");
            return false;
        }

        if (reps.isEmpty()) {
            showError("Le nombre de répétitions est requis");
            return false;
        }

        try {
            int setsInt = Integer.parseInt(sets);
            int repsInt = Integer.parseInt(reps);

            if (setsInt <= 0) {
                showError("Le nombre de séries doit être positif");
                return false;
            }

            if (repsInt <= 0) {
                showError("Le nombre de répétitions doit être positif");
                return false;
            }

            if (!weight.isEmpty()) {
                double weightDouble = Double.parseDouble(weight);
                if (weightDouble < 0) {
                    showError("Le poids ne peut pas être négatif");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            showError("Format numérique invalide");
            return false;
        }

        return true;
    }

    private void addExerciseToWorkout(Exercise exercise) {
        Log.d(TAG, "Adding exercise to workout: " + exercise.getName());

        if (currentWorkout == null) {
            Log.d(TAG, "Creating new workout for exercise");
            currentWorkout = new Workout(workoutId, workoutTitle != null ? workoutTitle : "Entraînement", new Date(), new ArrayList<>());
        }

        if (currentWorkout.getExercises() == null) {
            currentWorkout.setExercises(new ArrayList<>());
        }

        currentWorkout.addExercise(exercise);
        Log.d(TAG, "Exercise added. Total exercises: " + currentWorkout.getExerciseCount());

        saveWorkout();
    }

    private void updateExerciseInWorkout(int position) {
        Log.d(TAG, "Updating exercise at position: " + position);

        if (currentWorkout == null || position < 0 || position >= exerciseList.size()) {
            showError("Erreur lors de la mise à jour de l'exercice");
            return;
        }

        List<Exercise> exercises = currentWorkout.getExercises();
        if (exercises != null && position < exercises.size()) {
            exercises.set(position, exerciseList.get(position));
            Log.d(TAG, "Exercise updated in workout");
            saveWorkout();
        } else {
            Log.e(TAG, "Exercise position out of bounds in workout");
            showError("Erreur: position d'exercice invalide");
        }
    }

    private void removeExerciseFromWorkout(int position) {
        Log.d(TAG, "Removing exercise at position: " + position);

        if (currentWorkout == null || position < 0 || position >= exerciseList.size()) {
            showError("Erreur lors de la suppression de l'exercice");
            return;
        }

        Exercise exerciseToRemove = exerciseList.get(position);
        currentWorkout.removeExercise(exerciseToRemove);
        Log.d(TAG, "Exercise removed. Remaining exercises: " + currentWorkout.getExerciseCount());

        saveWorkout();
    }

    private void saveWorkout() {
        if (currentWorkout == null) {
            Log.e(TAG, "Cannot save: workout is null");
            showError("Erreur: entraînement non trouvé");
            return;
        }

        if (isSaving) {
            Log.d(TAG, "Already saving workout, skipping");
            return;
        }

        isSaving = true;
        Log.d(TAG, "Saving workout: " + currentWorkout.getTitle() + " with " + currentWorkout.getExerciseCount() + " exercises");

        firebaseHelper.updateWorkout(currentWorkout, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                isSaving = false;
                Log.d(TAG, "Workout saved successfully: " + workout.getId());

                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "Fragment not attached, skipping UI update");
                    return;
                }

                currentWorkout = workout;
                exerciseList.clear();
                if (workout.getExercises() != null) {
                    exerciseList.addAll(workout.getExercises());
                }

                if (exerciseAdapter != null) {
                    exerciseAdapter.notifyDataSetChanged();
                }
                updateEmptyState();
                showSuccess("Exercice sauvegardé");
            }

            @Override
            public void onFailure(String error) {
                isSaving = false;
                Log.e(TAG, "Failed to save workout: " + error);

                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "Fragment not attached, skipping error handling");
                    return;
                }

                showError("Erreur de sauvegarde: " + error);
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = exerciseList == null || exerciseList.isEmpty();
        Log.d(TAG, "Updating empty state. Is empty: " + isEmpty);

        if (isEmpty) {
            if (emptyStateView != null) emptyStateView.setVisibility(View.VISIBLE);
            if (recyclerViewExercises != null) recyclerViewExercises.setVisibility(View.GONE);
            if (btnStartWorkout != null) btnStartWorkout.setEnabled(false);
        } else {
            if (emptyStateView != null) emptyStateView.setVisibility(View.GONE);
            if (recyclerViewExercises != null) recyclerViewExercises.setVisibility(View.VISIBLE);
            if (btnStartWorkout != null) btnStartWorkout.setEnabled(true);
        }
    }

    private void startWorkoutSession() {
        Log.d(TAG, "Starting workout session");
        showSuccess("Démarrage de l'entraînement...");
        // TODO: Implement workout session start
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Error: " + message);
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "Success: " + message);
    }

    @Override
    public void onExerciseEdit(Exercise exercise, int position) {
        Log.d(TAG, "Edit exercise requested for position: " + position);
        showEditExerciseDialog(exercise, position);
    }

    @Override
    public void onExerciseDelete(Exercise exercise, int position) {
        if (getContext() == null) return;

        Log.d(TAG, "Delete exercise requested for position: " + position);
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer l'exercice")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet exercice ?")
                .setPositiveButton("Supprimer", (dialog, which) -> removeExerciseFromWorkout(position))
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed");
        // Only reload if we have a workout ID and aren't currently loading/saving
        if (workoutId != null && !workoutId.isEmpty() && !isLoading && !isSaving) {
            loadWorkoutDetails();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment view destroyed");
        // Clean up references
        if (exerciseAdapter != null) {
            exerciseAdapter = null;
        }
        isLoading = false;
        isSaving = false;
    }
}