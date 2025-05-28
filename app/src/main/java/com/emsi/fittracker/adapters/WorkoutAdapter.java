package com.emsi.fittracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.models.Workout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private OnWorkoutClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onWorkoutEdit(Workout workout);
        void onWorkoutDelete(Workout workout);
    }

    public WorkoutAdapter(List<Workout> workoutList, OnWorkoutClickListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public class WorkoutViewHolder extends RecyclerView.ViewHolder {

        private TextView tvWorkoutTitle;
        private TextView tvWorkoutDate;
        private TextView tvExerciseCount;
        private TextView tvWorkoutDuration;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private View cardView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWorkoutTitle = itemView.findViewById(R.id.tvWorkoutTitle);
            tvWorkoutDate = itemView.findViewById(R.id.tvWorkoutDate);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            tvWorkoutDuration = itemView.findViewById(R.id.tvWorkoutDuration);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Workout workout) {
            tvWorkoutTitle.setText(workout.getTitle());

            if (workout.getDate() != null) {
                tvWorkoutDate.setText(dateFormat.format(workout.getDate()));
            } else {
                tvWorkoutDate.setText("Date non définie");
            }

            // Exercise count
            int exerciseCount = workout.getExercises() != null ? workout.getExercises().size() : 0;
            if (exerciseCount == 0) {
                tvExerciseCount.setText("Aucun exercice");
            } else if (exerciseCount == 1) {
                tvExerciseCount.setText("1 exercice");
            } else {
                tvExerciseCount.setText(exerciseCount + " exercices");
            }

            // Calculate estimated duration (assuming 3-4 minutes per exercise)
            int estimatedDuration = exerciseCount * 3;
            if (estimatedDuration > 0) {
                tvWorkoutDuration.setText("~" + estimatedDuration + " min");
            } else {
                tvWorkoutDuration.setText("Durée inconnue");
            }

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutClick(workout);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutEdit(workout);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutDelete(workout);
                }
            });
        }
    }

    public void updateWorkouts(List<Workout> newWorkouts) {
        this.workoutList.clear();
        this.workoutList.addAll(newWorkouts);
        notifyDataSetChanged();
    }

    public void addWorkout(Workout workout) {
        this.workoutList.add(0, workout); // Add to the beginning
        notifyItemInserted(0);
    }

    public void removeWorkout(int position) {
        if (position >= 0 && position < workoutList.size()) {
            workoutList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateWorkout(int position, Workout workout) {
        if (position >= 0 && position < workoutList.size()) {
            workoutList.set(position, workout);
            notifyItemChanged(position);
        }
    }
}