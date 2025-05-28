package com.emsi.fittracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fittracker.R;
import com.emsi.fittracker.models.Exercise;

import java.text.DecimalFormat;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private OnExerciseClickListener listener;
    private DecimalFormat weightFormat;

    public interface OnExerciseClickListener {
        void onExerciseEdit(Exercise exercise, int position);

        void onExerciseDelete(Exercise exercise, int position);
    }

    public ExerciseAdapter(List<Exercise> exerciseList, OnExerciseClickListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
        this.weightFormat = new DecimalFormat("0.#");
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.bind(exercise, position);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        private TextView tvExerciseName;
        private TextView tvSetsReps;
        private TextView tvWeight;
        private TextView tvTotalVolume;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private View cardView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvSetsReps = itemView.findViewById(R.id.tvSetsReps);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvTotalVolume = itemView.findViewById(R.id.tvTotalVolume);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Exercise exercise, int position) {
            tvExerciseName.setText(exercise.getName());

            // Sets x Reps format
            tvSetsReps.setText(exercise.getSets() + " × " + exercise.getReps());

            // Weight
            if (exercise.getWeight() > 0) {
                tvWeight.setText(weightFormat.format(exercise.getWeight()) + " kg");
            } else {
                tvWeight.setText("Poids corporel");
            }

            // Calculate total volume (Sets × Reps × Weight)
            double totalVolume = exercise.getSets() * exercise.getReps() * exercise.getWeight();
            if (totalVolume > 0) {
                tvTotalVolume.setText("Volume: " + weightFormat.format(totalVolume) + " kg");
                tvTotalVolume.setVisibility(View.VISIBLE);
            } else {
                tvTotalVolume.setVisibility(View.GONE);
            }

            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseEdit(exercise, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseDelete(exercise, position);
                }
            });

            // Optional: Add click listener to the whole card for quick edit
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseEdit(exercise, position);
                }
            });
        }
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exerciseList.clear();
        this.exerciseList.addAll(newExercises);
        notifyDataSetChanged();
    }

    public void addExercise(Exercise exercise) {
        this.exerciseList.add(exercise);
        notifyItemInserted(exerciseList.size() - 1);
    }

    public void removeExercise(int position) {
        if (position >= 0 && position < exerciseList.size()) {
            exerciseList.remove(position);
            notifyItemRemoved(position);
        }
    }
}