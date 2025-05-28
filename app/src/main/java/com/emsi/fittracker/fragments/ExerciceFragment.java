package com.emsi.fittracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
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

        firebaseHelper = FirebaseHelper.getInstance();

        loadWorkoutDetails();
    }

    private void initViews(View view) {
        tvWorkoutTitle = view.findViewById(R.id.tvWorkoutTitle);
        recyclerViewExercises = view.findViewById(R.id.recyclerViewExercises);
        fabAddExercise = view.findViewById(R.id.fabAddExercise);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout);

        tvWorkoutTitle.setText(workoutTitle);
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
        // Load workout from Firestore
        firebaseHelper.getWorkout(workoutId, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentWorkout = workout;
                        exerciseList.clear();
                        if (workout.getExercises() != null) {
                            exerciseList.addAll(workout.getExercises());
                        }
                        exerciseAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showAddExerciseDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_exercise, null);

        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);

        new AlertDialog.Builder(getContext())
                .setTitle("Ajouter un exercice")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String name = etExerciseName.getText().toString().trim();
                    String setsStr = etSets.getText().toString().trim();
                    String repsStr = etReps.getText().toString().trim();
                    String weightStr = etWeight.getText().toString().trim();

                    if (validateExerciseInput(name, setsStr, repsStr, weightStr)) {
                        Exercise exercise = new Exercise(
                                name,
                                Integer.parseInt(setsStr),
                                Integer.parseInt(repsStr),
                                Double.parseDouble(weightStr)
                        );
                        addExerciseToWorkout(exercise);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditExerciseDialog(Exercise exercise, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_exercise, null);

        EditText etExerciseName = dialogView.findViewById(R.id.etExerciseName);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);

        // Pre-fill with current values
        etExerciseName.setText(exercise.getName());
        etSets.setText(String.valueOf(exercise.getSets()));
        etReps.setText(String.valueOf(exercise.getReps()));
        etWeight.setText(String.valueOf(exercise.getWeight()));

        new AlertDialog.Builder(getContext())
                .setTitle("Modifier l'exercice")
                .setView(dialogView)
                .setPositiveButton("Modifier", (dialog, which) -> {
                    String name = etExerciseName.getText().toString().trim();
                    String setsStr = etSets.getText().toString().trim();
                    String repsStr = etReps.getText().toString().trim();
                    String weightStr = etWeight.getText().toString().trim();

                    if (validateExerciseInput(name, setsStr, repsStr, weightStr)) {
                        exercise.setName(name);
                        exercise.setSets(Integer.parseInt(setsStr));
                        exercise.setReps(Integer.parseInt(repsStr));
                        exercise.setWeight(Double.parseDouble(weightStr));

                        updateExerciseInWorkout(position);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private boolean validateExerciseInput(String name, String sets, String reps, String weight) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Le nom de l'exercice est requis", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int setsInt = Integer.parseInt(sets);
            int repsInt = Integer.parseInt(reps);
            double weightDouble = Double.parseDouble(weight);

            if (setsInt <= 0 || repsInt <= 0 || weightDouble < 0) {
                Toast.makeText(getContext(), "Valeurs invalides", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Format numérique invalide", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addExerciseToWorkout(Exercise exercise) {
        if (currentWorkout == null) {
            currentWorkout = new Workout(workoutId, workoutTitle, new Date(), new ArrayList<>());
        }

        currentWorkout.addExercise(exercise);
        saveWorkout();
    }

    private void updateExerciseInWorkout(int position) {
        if (currentWorkout != null && position >= 0 && position < exerciseList.size()) {
            currentWorkout.getExercises().set(position, exerciseList.get(position));
            saveWorkout();
        }
    }

    private void removeExerciseFromWorkout(int position) {
        if (currentWorkout != null && position >= 0 && position < exerciseList.size()) {
            Exercise exerciseToRemove = exerciseList.get(position);
            currentWorkout.removeExercise(exerciseToRemove);
            saveWorkout();
        }
    }

    private void saveWorkout() {
        firebaseHelper.updateWorkout(currentWorkout, new DataCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentWorkout = workout;
                        exerciseList.clear();
                        if (workout.getExercises() != null) {
                            exerciseList.addAll(workout.getExercises());
                        }
                        exerciseAdapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(getContext(), "Exercice sauvegardé", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateEmptyState() {
        if (exerciseList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerViewExercises.setVisibility(View.GONE);
            btnStartWorkout.setEnabled(false);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerViewExercises.setVisibility(View.VISIBLE);
            btnStartWorkout.setEnabled(true);
        }
    }

    private void startWorkoutSession() {
        // TODO: Implement workout session start
        Toast.makeText(getContext(), "Démarrage de l'entraînement...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExerciseEdit(Exercise exercise, int position) {
        showEditExerciseDialog(exercise, position);
    }

    @Override
    public void onExerciseDelete(Exercise exercise, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer l'exercice")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet exercice ?")
                .setPositiveButton("Supprimer", (dialog, which) -> removeExerciseFromWorkout(position))
                .setNegativeButton("Annuler", null)
                .show();
    }
}