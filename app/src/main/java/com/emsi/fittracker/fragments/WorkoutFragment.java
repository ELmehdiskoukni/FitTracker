package com.emsi.fittracker.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emsi.fittracker.R;
import com.emsi.fittracker.AddEditWorkoutActivity;
import com.emsi.fittracker.adapters.WorkoutAdapter;
import com.emsi.fittracker.interfaces.DataCallback;
import com.emsi.fittracker.models.Workout;
import com.emsi.fittracker.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment implements WorkoutAdapter.OnWorkoutClickListener {

    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> workoutList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddWorkout;
    private View emptyStateView;

    private FirebaseHelper firebaseHelper;
    private String currentUserId;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        firebaseHelper = FirebaseHelper.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadWorkouts();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewWorkouts);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabAddWorkout = view.findViewById(R.id.fabAddWorkout);
        emptyStateView = view.findViewById(R.id.emptyStateView);
    }

    private void setupRecyclerView() {
        workoutList = new ArrayList<>();
        workoutAdapter = new WorkoutAdapter(workoutList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(workoutAdapter);
    }

    private void setupListeners() {
        fabAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditWorkoutActivity.class);
            startActivity(intent);
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadWorkouts);
    }

    private void loadWorkouts() {
        swipeRefreshLayout.setRefreshing(true);

        firebaseHelper.getUserWorkouts(currentUserId, new DataCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        workoutList.clear();
                        workoutList.addAll(workouts);
                        workoutAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);

                        // Show/hide empty state
                        if (workoutList.isEmpty()) {
                            emptyStateView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Erreur: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onWorkoutClick(Workout workout) {
        // Navigate to exercise activity
        Intent intent = new Intent(getContext(), com.emsi.fittracker.ExerciseActivity.class);
        intent.putExtra("workout_id", workout.getId());
        intent.putExtra("workout_title", workout.getTitle());
        startActivity(intent);
    }

    @Override
    public void onWorkoutEdit(Workout workout) {
        Intent intent = new Intent(getContext(), AddEditWorkoutActivity.class);
        intent.putExtra("workout_id", workout.getId());
        intent.putExtra("workout_title", workout.getTitle());
        startActivity(intent);
    }

    @Override
    public void onWorkoutDelete(Workout workout) {
        new AlertDialog.Builder(getContext())
                .setTitle("Supprimer l'entraînement")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet entraînement ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteWorkout(workout))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteWorkout(Workout workout) {
        firebaseHelper.deleteWorkout(workout.getId(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Entraînement supprimé", Toast.LENGTH_SHORT).show();
                        loadWorkouts(); // Refresh the list
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Erreur lors de la suppression: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts(); // Refresh data when returning to fragment
    }
}